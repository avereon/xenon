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

	private TxnOperation.Status status;

	private TxnOperationResult result;

	protected TxnOperation() {
		status = Status.WAITING;
		result = new TxnOperationResult( this );
	}

	protected abstract void commit() throws TxnException;

	protected abstract void revert() throws TxnException;

	Status getStatus() {
		return status;
	}

	protected TxnOperationResult getResult() {
		return result;
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
