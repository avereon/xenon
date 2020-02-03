package com.avereon.xenon.transaction;

import com.avereon.event.EventType;
import com.avereon.util.Log;
import org.slf4j.Logger;

import java.lang.invoke.MethodHandles;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Txn is a thread scoped transaction utility for creating and processing
 * transactions on a specific thread.
 */
public class Txn {

	private static final Logger log = Log.get( MethodHandles.lookup().lookupClass() );

	private static final ThreadLocal<Deque<Txn>> threadLocalTransactions = new ThreadLocal<>();

	private final ReentrantLock commitLock = new ReentrantLock();

	private Queue<TxnOperation> operations;

	private int depth;

	static {
		threadLocalTransactions.set( new ArrayDeque<>() );
	}

	private Txn() {
		operations = new ConcurrentLinkedQueue<>();
	}

	/**
	 * Create a transaction for this thread if there is not already an active
	 * transaction. If there is already an active transaction then the active
	 * transaction is returned, otherwise a new transaction is created and
	 * returned.
	 *
	 * @return The transaction
	 */
	public static Txn create() {
		return create( false );
	}

	/**
	 * Create a transaction for this thread if there is not already an active
	 * transaction or the nest flag is set to intentionally start a nested
	 * transaction instead of using an existing transaction.
	 * <p/>
	 * Note that the nested transaction becomes the new active transaction until
	 * it is completed (committed or reset) and therefore nested transactions must
	 * be completed before the outer transaction can be completed.
	 *
	 * @param nest Set to true if this transaction should not be part of an
	 * existing transaction.
	 * @return The transaction
	 */
	public static Txn create( boolean nest ) {
		Txn transaction = peekTransaction();
		if( transaction == null || nest ) transaction = pushTransaction();
		transaction.depth++;

		return transaction;
	}

	public static void submit( TxnOperation operation ) throws TxnException {
		verifyActiveTransaction().operations.offer( operation );
	}

	public static void commit() throws TxnException {
		Txn transaction = verifyActiveTransaction();
		if( --transaction.depth > 0 ) return;

		transaction.doCommit();
		pullTransaction();
	}

	public static void reset() {
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
		if( deque == null ) threadLocalTransactions.set( deque = new ArrayDeque<>() );
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

	private void doCommit() throws TxnException {
		Set<TxnOperation> operations = new HashSet<>( this.operations );

		try {
			commitLock.lock();
			log.trace( System.identityHashCode( this ) + " locked by: " + Thread.currentThread() );

			// Send a commit begin event to all unique targets
			sendEvent( TxnEvent.COMMIT_BEGIN, operations );

			// Process all the operations
			List<TxnOperationResult> operationResults = new ArrayList<>( processOperations() );

			// Go through each operation result and collect the events by dispatcher
			Map<TxnEventTarget, List<TxnEvent>> txnEvents = new HashMap<>();
			for( TxnOperationResult operationResult : operationResults ) {
				for( TxnEvent event : operationResult.getEvents() ) {
					TxnEventTarget target = event.getTarget();
					List<TxnEvent> events = txnEvents.computeIfAbsent( target, k -> new ArrayList<>() );
					int index = events.indexOf( event );
					if( index > -1 ) events.remove( index );
					events.add( event );
				}
			}

			for( Map.Entry<TxnEventTarget, List<TxnEvent>> entry : txnEvents.entrySet() ) {
				TxnEventTarget target = entry.getKey();
				List<TxnEvent> events = entry.getValue();

				for( TxnEvent event : events ) {
					try {
						target.dispatch( event );
					} catch( Throwable throwable ) {
						log.error( "Error dispatching transaction event", throwable );
					}
				}
			}
		} finally {
			sendEvent( TxnEvent.COMMIT_END, operations );
			doReset();
			commitLock.unlock();
			log.trace( "Transaction[" + System.identityHashCode( this ) + "] committed!" );
		}
	}

	/**
	 * Send an event to all unique targets.
	 *
	 * @param type The event type
	 */
	private void sendEvent( EventType<? extends TxnEvent> type, Collection<TxnOperation> operations ) {
		operations.stream().map( TxnOperation::getTarget ).distinct().forEach( t -> t.dispatch( new TxnEvent( t, type ) ) );
	}

	private List<TxnOperationResult> processOperations() throws TxnException {
		// Process the operations.
		List<TxnOperationResult> operationResults = new ArrayList<>();
		List<TxnOperation> completedOperations = new ArrayList<>();
		try {
			TxnOperation operation;
			while( (operation = operations.poll()) != null ) {
				operationResults.add( operation.callCommit() );
				completedOperations.add( operation );
			}
		} catch( TxnException commitException ) {
			try {
				for( TxnOperation operation : completedOperations ) {
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
