package com.parallelsymmetry.essence.transaction;

import com.parallelsymmetry.essence.data.TransactionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.ReentrantLock;

public class Txn {

	private static final Logger log = LoggerFactory.getLogger( Txn.class );

	private static final TxnEventComparator eventComparator = new TxnEventComparator();

	private static final ThreadLocal<Deque<Txn>> threadLocalTransactions = new ThreadLocal<Deque<Txn>>();

	private static final ReentrantLock commitLock = new ReentrantLock();

	private static Txn committingTransaction;

	private Queue<TxnOperation> operations;

	private int depth;

	static {
		threadLocalTransactions.set( new ArrayDeque<Txn>() );
	}

	public Txn() {
		operations = new ConcurrentLinkedQueue<TxnOperation>();
	}

	public static Txn create() {
		return create( false );
	}

	public static Txn create( boolean nest ) {
		Txn transaction = peekTransaction();
		if( transaction == null || nest ) transaction = pushTransaction();

		transaction.depth++;

		return transaction;
	}

	public static void submit( TxnOperation operation ) throws TxnException {
		Txn transaction = verifyActiveTransaction();
		transaction.doSubmit( operation );
		log.info( "Operation submitted: " + operation );
	}

	public static void commit() throws TxnException {
		Txn transaction = verifyActiveTransaction();
		if( --transaction.depth > 0 ) return;

		transaction.doCommit();
		pullTransaction();
	}

	public static final void reset() throws TxnException {
		Txn transaction = peekTransaction();
		if( transaction == null ) return;
		if( --transaction.depth > 0 ) return;

		while( transaction != null ) {
			transaction.doReset();
			transaction = pullTransaction();
		}
	}

	public static Txn getActiveTransaction() {
		return peekTransaction();
	}

	private static Txn verifyActiveTransaction() throws TxnException {
		Txn transaction = peekTransaction();
		if( transaction == null ) throw new TxnException( "No active transaction" );
		return transaction;
	}

	private static Txn peekTransaction() {
		Deque<Txn> deque = threadLocalTransactions.get();
		return deque == null ? null : deque.peekFirst();
	}

	private static Txn pushTransaction() {
		Deque<Txn> deque = threadLocalTransactions.get();
		if( deque == null ) threadLocalTransactions.set( deque = new ArrayDeque<Txn>() );
		Txn transaction = new Txn();
		deque.offerFirst( transaction );
		return transaction;
	}

	private static Txn pullTransaction() {
		Deque<Txn> deque = threadLocalTransactions.get();
		if( deque == null ) return null;
		Txn transaction = deque.pollFirst();
		if( deque.size() == 0 ) threadLocalTransactions.set( null );
		return transaction;
	}

	private void doSubmit( TxnOperation operation ) {
		if( commitLock.isLocked() ) throw new TransactionException( "Transaction steps cannot be added during a commit" );
		operations.offer( operation );
	}

	private void doCommit() throws TxnException {
		try {
			commitLock.lock();

			committingTransaction = this;

			//			// Store the current modified state of each data object.
			//			for( DataNode node : nodes.values() ) {
			//				node.putResource( PREVIOUS_MODIFIED_STATE, node.isModified() );
			//			}

			// Process the operations.
			List<TxnOperationResult> operationResults = new ArrayList<>();
			try {
				for( TxnOperation operation : operations ) {
					operation.callCommit();
					operationResults.add( operation.getResult() );
				}
			} catch( TxnException commitException ) {
				try {
					for( TxnOperation operation : operations ) {
						if( operation.getStatus() == TxnOperation.Status.COMMITTED ) operation.callRevert();
					}
				} catch( TxnException rollbackException ) {
					throw new TxnException( "Error rolling back transaction", rollbackException );
				}
			}

			// Go through each operation result and collect the events by dispatcher
			Map<TxnEventDispatcher, List<TxnEvent>> txnEvents = new HashMap<>();
			for( TxnOperationResult operationResult : operationResults ) {
				for( TxnEvent event : operationResult.getEvents() ) {
					TxnEventDispatcher dispatcher = event.getDispatcher();
					List<TxnEvent> events = txnEvents.computeIfAbsent( dispatcher, k -> new ArrayList<>() );
					events.add( event );
				}
			}

			// Sort the events for each dispatcher
			for( Map.Entry<TxnEventDispatcher, List<TxnEvent>> entry : txnEvents.entrySet() ) {
				TxnEventDispatcher dispatcher = entry.getKey();
				List<TxnEvent> events = entry.getValue();
				Collections.sort( events, eventComparator );
				for( TxnEvent event : events ) {
					try {
						dispatcher.dispatchEvent( event );
					} catch( Throwable throwable ) {
						log.error( "Error dispatching transaction event", throwable );
					}
				}
			}

			//				DataNode node = operationResult.getOperation().getData();
			//				// TODO Collect operation events
			//				//getResultCollector( node ).events.addAll( operationResult.getEvents() );
			//				//getResultCollector( node ).modified.addAll( operationResult.getMetaValueEvents() );
			//
			//			// Send the events for each data node.
			//			for( DataNode node : nodes.values() ) {
			//				boolean oldModified = (Boolean)node.getResource( PREVIOUS_MODIFIED_STATE );
			//				boolean newModified = node.isModified();
			//				node.putResource( PREVIOUS_MODIFIED_STATE, null );
			//				collectFinalEvents( node, node, oldModified, newModified );
			//			}

			//			dispatchTransactionEvents();
		} finally {
			committingTransaction = null;
			doReset();
			commitLock.unlock();
			log.trace( "Transaction[" + System.identityHashCode( this ) + "] committed!" );
		}
	}

	private void doReset() {
		operations.clear();
	}

}
