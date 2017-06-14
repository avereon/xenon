package com.parallelsymmetry.essence.transaction;

import org.junit.Assert;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class TxnTest {

	@Test
	public void testCommit() throws Exception {
		MockTransactionOperation step = new MockTransactionOperation();

		Txn.create();
		Txn.submit( step );
		Txn.commit();

		assertThat( step.getCommitCallCount(), is( 1 ) );
		assertThat( step.getRollbackCallCount(), is( 0 ) );
	}

	@Test
	public void testCommitWithoutTransaction() throws Exception {
		try {
			Txn.commit();
			Assert.fail( "Commit should throw an exception if there is not an active transaction" );
		} catch( TxnException exception ) {
			// Pass
		}
	}

	@Test
	public void testSubmitWithoutTransaction() throws Exception {
		MockTransactionOperation step = new MockTransactionOperation();
		try {
			Txn.submit( step );
			Assert.fail( "Submit should throw an exception if there is not an active transaction" );
		} catch( TxnException exception ) {
			// Pass
		}
	}

	@Test
	public void testRollbackWithoutTransaction() throws Exception {
		try {
			Txn.rollback();
			Assert.fail( "Rollback should throw an exception if there is not an active transaction" );
		} catch( TxnException exception ) {
			// Pass
		}
	}

	private class MockTransactionOperation extends TxnOperation {

		private int commitCallCount;

		private int rollbackCallCount;

		@Override
		protected void commit() throws TxnException {
			TxnOperationResult result = new TxnOperationResult( this );
			commitCallCount++;
			setResult( result );
		}

		@Override
		protected void revert() throws TxnException {
			rollbackCallCount++;
		}

		int getCommitCallCount() {
			return commitCallCount;
		}

		int getRollbackCallCount() {
			return rollbackCallCount;
		}

	}

}
