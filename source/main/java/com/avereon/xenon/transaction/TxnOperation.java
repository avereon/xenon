package com.avereon.xenon.transaction;

// May also be named unit, task, step, etc.
public abstract class TxnOperation {

	public enum Status {
		WAITING,
		COMMITTING,
		COMMITTED,
		REVERTING,
		REVERTED,
		FAILED
	}

	private TxnEventTarget target;

	private TxnOperationResult result;

	private TxnOperation.Status status;

	protected TxnOperation( TxnEventTarget target ) {
		this.target = target;
		result = new TxnOperationResult( this );
		status = Status.WAITING;
	}

	protected abstract void commit() throws TxnException;

	protected abstract void revert() throws TxnException;

	public TxnEventTarget getTarget() {
		return target;
	}

	protected TxnOperationResult getResult() {
		return result;
	}

	Status getStatus() {
		return status;
	}

	TxnOperationResult callCommit() throws TxnException {
		try {
			status = Status.COMMITTING;
			commit();
			status = Status.COMMITTED;
		} catch( TxnException exception ) {
			status = Status.FAILED;
			throw exception;
		}
		return getResult();
	}

	void callRevert() throws TxnException {
		try {
			status = Status.REVERTING;
			revert();
			status = Status.REVERTED;
		} catch( TxnException exception ) {
			status = Status.FAILED;
			throw exception;
		}
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + System.identityHashCode( this ) + "]";
	}

}
