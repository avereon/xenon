package com.parallelsymmetry.essence.data;

import com.parallelsymmetry.essence.data.event.DataChangedEvent;
import com.parallelsymmetry.essence.data.event.DataValueEvent;
import com.parallelsymmetry.essence.data.event.MetaAttributeEvent;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.locks.ReentrantLock;

public class Transaction {

	private static final String PREVIOUS_MODIFIED_STATE = Transaction.class.getName() + ".previousModifiedState";

	private static final ReentrantLock COMMIT_LOCK = new ReentrantLock();

	private static final ThreadLocal<Deque<Transaction>> threadLocalTransactions = new ThreadLocal<Deque<Transaction>>();

	private static Transaction committingTransaction;

	private Queue<Operation> operations;

	private Set<Integer> nodeKeys;

	private Map<Integer, DataNode> nodes;

	private Map<Integer, ResultCollector> collectors;

	private int depth;

	static {
		threadLocalTransactions.set( new ArrayDeque<Transaction>() );
	}

	private Transaction() {
		nodeKeys = new CopyOnWriteArraySet<Integer>();
		operations = new ConcurrentLinkedQueue<Operation>();
		nodes = new ConcurrentHashMap<Integer, DataNode>();
		collectors = new ConcurrentHashMap<Integer, ResultCollector>();
	}

	public static final Transaction create() {
		return create( false );
	}

	public static final Transaction create( boolean nest ) {
		Transaction transaction = peekTransaction();
		if( nest || transaction == null ) transaction = pushTransaction();

		++transaction.depth;

		return transaction;
	}

	public static final void submit( Operation operation ) {
		Transaction transaction = peekTransaction();
		if( transaction == null ) throw new NullPointerException( "Transaction must be created first." );

		transaction.doSubmit( operation );
	}

	public static final boolean commit() {
		Transaction transaction = peekTransaction();
		if( transaction == null ) throw new NullPointerException( "Transaction must be created first." );

		--transaction.depth;

		if( transaction.depth == 0 ) {
			pullTransaction();
			transaction.doCommit();
		}

		return true;
	}

	public static final void rollback() {
		Transaction transaction = peekTransaction();
		if( transaction == null ) throw new NullPointerException( "Transaction must be created first." );

		pullTransaction();
		transaction.doRollback();
	}

	public static final void reset() {
		Transaction transaction = peekTransaction();

		while( transaction != null ) {
			transaction.doReset();
			transaction = pullTransaction();
		}
	}

	public static final Transaction current() {
		return peekTransaction();
	}

	public static final int depth() {
		Transaction transaction = peekTransaction();
		return transaction == null ? 0 : transaction.depth;
	}

	public static final int count() {
		Deque<Transaction> deque = threadLocalTransactions.get();
		return deque == null ? 0 : deque.size();
	}

	@Override
	public String toString() {
		return String.valueOf( "transaction[" + System.identityHashCode( this ) + "]" );
	}

	private static Transaction peekTransaction() {
		Deque<Transaction> deque = threadLocalTransactions.get();
		return deque == null ? null : deque.peekFirst();
	}

	private static Transaction pushTransaction() {
		Deque<Transaction> deque = threadLocalTransactions.get();
		if( deque == null ) threadLocalTransactions.set( deque = new ArrayDeque<Transaction>() );
		Transaction transaction = new Transaction();
		deque.offerFirst( transaction );
		return transaction;
	}

	private static Transaction pullTransaction() {
		Deque<Transaction> deque = threadLocalTransactions.get();
		if( deque == null ) return null;
		Transaction transaction = deque.pollFirst();
		if( deque.size() == 0 ) threadLocalTransactions.set( null );
		return transaction;
	}

	private void doSubmit( Operation operation ) {
		if( COMMIT_LOCK.isLocked() && inCommittingTransaction( operation.getData() ) ) throw new TransactionException( "Data should not be modified from data listeners." );
		addOperationNode( operation.getData() );
		operations.offer( operation );
	}

	private void doCommit() {
		try {
			COMMIT_LOCK.lock();

			committingTransaction = this;

			// Store the current modified state of each data object.
			for( DataNode node : nodes.values() ) {
				node.putResource( PREVIOUS_MODIFIED_STATE, node.isModified() );
			}

			// Process the operations.
			List<OperationResult> operationResults = new ArrayList<OperationResult>();
			for( Operation operation : operations ) {
				operationResults.add( operation.process() );
			}

			// Go through each operation result and collect the events for each node.
			for( OperationResult operationResult : operationResults ) {
				DataNode node = operationResult.getOperation().getData();
				// TODO Collect operation events
				//getResultCollector( node ).events.addAll( operationResult.getEvents() );
				//getResultCollector( node ).modified.addAll( operationResult.getMetaValueEvents() );
			}

			// Send the events for each data node.
			for( DataNode node : nodes.values() ) {
				boolean oldModified = (Boolean)node.getResource( PREVIOUS_MODIFIED_STATE );
				boolean newModified = node.isModified();
				node.putResource( PREVIOUS_MODIFIED_STATE, null );
				collectFinalEvents( node, node, oldModified, newModified );
			}

			dispatchTransactionEvents();
		} finally {
			doReset();
			committingTransaction = null;
			COMMIT_LOCK.unlock();
			//Log.write( Log.DETAIL, "Transaction[" + System.identityHashCode( this ) + "] committed!" );
		}
	}

	private void doRollback() {
		//throw new UnsupportedOperationException( "Transaction.rollback() not implemented yet." );

		doReset();
	}

	private void doReset() {
		collectors.clear();
		operations.clear();
		nodeKeys.clear();
		nodes.clear();
	}

	private void addOperationNode( DataNode node ) {
		Integer key = System.identityHashCode( node );
		synchronized( nodeKeys ) {
			if( nodeKeys.contains( key ) ) return;
			nodeKeys.add( key );
			nodes.put( key, node );
		}
	}

	private boolean inCommittingTransaction( DataNode node ) {
		return committingTransaction != null && committingTransaction.nodeKeys.contains( System.identityHashCode( node ) );
	}

	private ResultCollector getResultCollector( DataNode node ) {
		Integer key = System.identityHashCode( node );
		ResultCollector collector = collectors.computeIfAbsent( key, k -> new ResultCollector() );
		return collector;
	}

	private void collectFinalEvents( DataNode sender, DataNode cause, boolean oldModified, boolean newModified ) {
		// Post the modified event.
		boolean modifiedChanged = oldModified != newModified;
		if( modifiedChanged ) storeModifiedEvent( new MetaAttributeEvent( DataEvent.Action.MODIFY, sender, DataNode.MODIFIED, oldModified, newModified ) );

		// Update the parent node.
		DataNode parent = sender.getParent();
		if( parent != null ) {
			boolean parentOldModified = parent.isModified();
			if( modifiedChanged ) {
				// TODO Set modified flag on data node and/or children
				//if( parent instanceof DataList ) {
				//	( (DataList<?>)parent ).listNodeChildModified( newModified );
				//} else {
				parent.dataNodeModified( newModified );
				//}
			}
			boolean parentNewModified = parent.isModified();

			collectFinalEvents( parent, cause, parentOldModified, parentNewModified );
		}

		// Post the changed event.
		storeChangedEvent( new DataChangedEvent( DataEvent.Action.MODIFY, sender ) );
	}

	private void storeModifiedEvent( MetaAttributeEvent event ) {
		// Remove any previously added modified events.
		List<MetaAttributeEvent> events = getResultCollector( event.getSender() ).modified;
		events.removeIf( metaValueEvent -> DataNode.MODIFIED.equals( metaValueEvent.getAttributeName() ) );

		// Add the new modified event.
		getResultCollector( event.getSender() ).modified.add( event );
	}

	private void storeChangedEvent( DataChangedEvent event ) {
		getResultCollector( event.getSender() ).changed = event;
	}

	private void dispatchTransactionEvents() {
		// Fire the data value events first
		for( Integer key : collectors.keySet() ) {
			ResultCollector collector = collectors.get( key );
			for( DataValueEvent event : collector.events ) {
				dispatchValueEvent( event );
			}
		}

		// Fire the meta value events next.
		for( Integer key : collectors.keySet() ) {
			ResultCollector collector = collectors.get( key );
			for( MetaAttributeEvent event : collector.modified ) {
				dispatchEvent( event );
			}
		}

		// Fire the data changed events last.
		for( Integer key : collectors.keySet() ) {
			ResultCollector collector = collectors.get( key );
			if( collector.changed != null ) dispatchEvent( collector.changed );
		}
	}

	private void dispatchValueEvent( DataEvent event ) {
		DataNode sender = event.getSender();

		sender.dispatchEvent( event );

		DataNode parent = sender.getParent();
		if( parent != null ) dispatchValueEvent( event.cloneWithNewSender( parent ) );
	}

	private void dispatchEvent( DataEvent event ) {
		event.getSender().dispatchEvent( event );
	}

	private class ResultCollector {

		public List<DataValueEvent> events = new ArrayList<DataValueEvent>();

		public List<MetaAttributeEvent> modified = new ArrayList<MetaAttributeEvent>();

		public DataChangedEvent changed;

	}

}
