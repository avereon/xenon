package com.parallelsymmetry.essence.transaction;

// May also be named unit, task, step, etc.
public abstract class TxnOperation {

	public enum Status {
		WAITING, COMMITTING, COMMITTED, REVERTING, REVERTED, FAILED
	}

	private TxnOperation.Status status;

	private TxnOperationResult result;

	protected TxnOperation() {
		status = Status.WAITING;
	}

	protected abstract void commit() throws TxnException;

	protected abstract void revert() throws TxnException;

	/**
	 * Return the result of the operation or null if the operation failed or has not been processed.
	 *
	 * @return
	 */
	protected TxnOperationResult getResult() {
		return result;
	}

	/**
	 * Set the operation result. This should not be set if the operation fails.
	 *
	 * @param result
	 */
	protected void setResult( TxnOperationResult result ) {
		this.result = result;
	}

	void callCommit() throws TxnException	{
		try {
			status = Status.COMMITTING;
			commit();
			status = Status.COMMITTED;
		} catch( TxnException exception ) {
			status = Status.FAILED;
			throw exception;
		}
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

	Status getStatus() {
		return status;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + System.identityHashCode( this ) + "]";
	}

}
