package com.parallelsymmetry.essence.transaction;

import com.parallelsymmetry.essence.data.TransactionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.ReentrantLock;

public class Txn {

	private enum Phase {
		BEFORE,
		DURING,
		AFTER
	}

	private static final Logger log = LoggerFactory.getLogger( Txn.class );

	private static final TxnEventComparator eventComparator = new TxnEventComparator();

	private static final ThreadLocal<Deque<Txn>> threadLocalTransactions = new ThreadLocal<Deque<Txn>>();

	private static final ReentrantLock commitLock = new ReentrantLock();

	private static Txn committingTransaction;

	private Map<Phase, Queue<TxnOperation>> operations;

	private int depth;

	static {
		threadLocalTransactions.set( new ArrayDeque<Txn>() );
	}

	public Txn() {
		operations = new ConcurrentHashMap<>();
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

	public static void submitBefore( TxnOperation operation ) throws TxnException {
		verifyActiveTransaction().doSubmit( Phase.BEFORE, operation );
	}

	public static void submit( TxnOperation operation ) throws TxnException {
		verifyActiveTransaction().doSubmit( Phase.DURING, operation );
	}

	public static void submitAfter( TxnOperation operation ) throws TxnException {
		verifyActiveTransaction().doSubmit( Phase.AFTER, operation );
	}

	public static void commit() throws TxnException {
		Txn transaction = verifyActiveTransaction();
		if( --transaction.depth > 0 ) return;

		transaction.doCommit();
		pullTransaction();
	}

	public static void reset() throws TxnException {
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

	private void doSubmit( Phase phase, TxnOperation operation ) {
		if( commitLock.isLocked() ) throw new TransactionException( "Transaction steps cannot be added during a commit" );
		Queue<TxnOperation> phaseOperations = operations.computeIfAbsent( phase, key -> new ConcurrentLinkedQueue<TxnOperation>() );
		phaseOperations.offer( operation );
	}

	private void doCommit() throws TxnException {
		try {
			commitLock.lock();

			committingTransaction = this;

			// Process all the operations
			List<TxnOperationResult> operationResults = new ArrayList<TxnOperationResult>();
			operationResults.addAll( processOperations( Phase.BEFORE ) );
			operationResults.addAll( processOperations( Phase.DURING ) );
			operationResults.addAll( processOperations( Phase.AFTER ) );

			// Go through each operation result and collect the events by dispatcher
			Map<TxnEventDispatcher, List<TxnEvent>> txnEvents = new HashMap<>();
			for( TxnOperationResult operationResult : operationResults ) {
				for( TxnEvent event : operationResult.getEvents() ) {
					TxnEventDispatcher dispatcher = event.getDispatcher();
					List<TxnEvent> events = txnEvents.computeIfAbsent( dispatcher, k -> new ArrayList<>() );
					int index = events.indexOf( event );
					if( index > -1 ) events.remove( index );
					events.add( event );
				}
			}

			for( Map.Entry<TxnEventDispatcher, List<TxnEvent>> entry : txnEvents.entrySet() ) {
				TxnEventDispatcher dispatcher = entry.getKey();
				List<TxnEvent> events = entry.getValue();
				// Sort the events for each dispatcher
				events.sort( eventComparator.reversed() );
				for( TxnEvent event : events ) {
					//System.out.println( "Producer=" + dispatcher + " event=" + event );
					try {
						dispatcher.dispatchEvent( event );
					} catch( Throwable throwable ) {
						log.error( "Error dispatching transaction event", throwable );
					}
				}
			}
		} finally {
			committingTransaction = null;
			doReset();
			commitLock.unlock();
			log.trace( "Transaction[" + System.identityHashCode( this ) + "] committed!" );
		}

		//System.out.println( "Commit complete!" );
	}

	private List<TxnOperationResult> processOperations( Phase phase ) throws TxnException {
		// Process the operations.
		List<TxnOperationResult> operationResults = new ArrayList<>();
		try {
			Queue<TxnOperation> phaseOperations = operations.get( phase );
			if( phaseOperations != null ) {
				for( TxnOperation operation : phaseOperations ) {
					operation.callCommit();
					operationResults.add( operation.getResult() );
				}
			}
		} catch( TxnException commitException ) {
			try {
				for( TxnOperation operation : operations.get( phase ) ) {
					if( operation.getStatus() == TxnOperation.Status.COMMITTED ) operation.callRevert();
				}
			} catch( TxnException rollbackException ) {
				throw new TxnException( "Error rolling back transaction", rollbackException );
			}
		}
		return operationResults;
	}

	private void doReset() {
		operations.clear();
	}

}
