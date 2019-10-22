package com.avereon.xenon.transaction;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.fail;

class TxnTest {

	@BeforeEach
	void setup() throws Exception {
		Txn.reset();
	}

	@Test
	void testCommit() throws Exception {
		MockTransactionOperation step = new MockTransactionOperation();

		Txn.create();
		Txn.submit( step );
		Txn.commit();

		assertThat( step.getCommitCallCount(), is( 1 ) );
		assertThat( step.getRollbackCallCount(), is( 0 ) );
	}

	@Test
	void testRollback() throws Exception {
		MockTransactionOperation step1 = new MockTransactionOperation();
		MockTransactionOperation step2 = new MockTransactionOperation();
		MockTransactionOperation step3 = new MockTransactionOperation();
		MockTransactionOperation step4 = new MockTransactionOperation();
		MockTransactionOperation step5 = new MockTransactionOperation();

		step3.setThrowException( new NullPointerException() );

		Txn.create();
		Txn.submit( step1 );
		Txn.submit( step2 );
		Txn.submit( step3 );
		Txn.submit( step4 );
		Txn.submit( step5 );
		Txn.commit();

		assertThat( step1.getCommitCallCount(), is( 1 ) );
		assertThat( step1.getRollbackCallCount(), is( 1 ) );
		assertThat( step2.getCommitCallCount(), is( 1 ) );
		assertThat( step2.getRollbackCallCount(), is( 1 ) );
		assertThat( step3.getCommitCallCount(), is( 1 ) );
		assertThat( step3.getRollbackCallCount(), is( 0 ) );
		assertThat( step4.getCommitCallCount(), is( 0 ) );
		assertThat( step4.getRollbackCallCount(), is( 0 ) );
		assertThat( step5.getCommitCallCount(), is( 0 ) );
		assertThat( step5.getRollbackCallCount(), is( 0 ) );
	}

	@Test
	void testReset() throws Exception {
		MockTransactionOperation step = new MockTransactionOperation();

		Txn.create();
		Txn.submit( step );
		Txn.reset();

		assertThat( step.getCommitCallCount(), is( 0 ) );
		assertThat( step.getRollbackCallCount(), is( 0 ) );
	}

	@Test
	void testContinuedException() throws Exception {
		MockTransactionOperation step1 = new MockTransactionOperation();
		MockTransactionOperation step2 = new MockTransactionOperation();
		MockTransactionOperation step3 = new MockTransactionOperation();
		assertThat( step1.getCommitCallCount(), is( 0 ) );
		assertThat( step2.getCommitCallCount(), is( 0 ) );
		assertThat( step3.getCommitCallCount(), is( 0 ) );

		Txn.create();
		Txn.submit( step1 );

		Txn.create( false );
		Txn.submit( step2 );
		Txn.commit();

		assertThat( step1.getCommitCallCount(), is( 0 ) );
		assertThat( step2.getCommitCallCount(), is( 0 ) );
		assertThat( step3.getCommitCallCount(), is( 0 ) );

		Txn.submit( step3 );
		Txn.commit();

		assertThat( step1.getCommitCallCount(), is( 1 ) );
		assertThat( step2.getCommitCallCount(), is( 1 ) );
		assertThat( step3.getCommitCallCount(), is( 1 ) );
	}

	@Test
	void testNestedException() throws Exception {
		MockTransactionOperation step1 = new MockTransactionOperation();
		MockTransactionOperation step2 = new MockTransactionOperation();
		MockTransactionOperation step3 = new MockTransactionOperation();
		assertThat( step1.getCommitCallCount(), is( 0 ) );
		assertThat( step2.getCommitCallCount(), is( 0 ) );
		assertThat( step3.getCommitCallCount(), is( 0 ) );

		Txn.create();
		Txn.submit( step1 );

		Txn.create( true );
		Txn.submit( step2 );
		Txn.commit();

		assertThat( step1.getCommitCallCount(), is( 0 ) );
		assertThat( step2.getCommitCallCount(), is( 1 ) );
		assertThat( step3.getCommitCallCount(), is( 0 ) );

		Txn.submit( step3 );
		Txn.commit();

		assertThat( step1.getCommitCallCount(), is( 1 ) );
		assertThat( step2.getCommitCallCount(), is( 1 ) );
		assertThat( step3.getCommitCallCount(), is( 1 ) );
	}

	@Test
	void testCommitWithoutTransaction() {
		try {
			Txn.commit();
			fail( "Commit should throw an exception if there is not an active transaction" );
		} catch( TxnException exception ) {
			// Pass
		}
	}

	@Test
	void testSubmitWithoutTransaction() {
		MockTransactionOperation step = new MockTransactionOperation();
		try {
			Txn.submit( step );
			fail( "Submit should throw an exception if there is not an active transaction" );
		} catch( TxnException exception ) {
			// Pass
		}
	}

	//	@Test
	//	public void testResetWithoutTransaction() throws Exception {
	//		try {
	//			Txn.reset();
	//			Assert.fail( "Reset should throw an exception if there is not an active transaction" );
	//		} catch( TxnException exception ) {
	//			// Pass
	//		}
	//	}

	private static class MockTransactionOperation extends TxnOperation {

		private int commitCallCount;

		private int rollbackCallCount;

		private Throwable throwable;

		@Override
		protected void commit() throws TxnException {
			commitCallCount++;
			if( throwable != null ) throw new TxnException( throwable );
		}

		@Override
		protected void revert() {
			rollbackCallCount++;
		}

		int getCommitCallCount() {
			return commitCallCount;
		}

		int getRollbackCallCount() {
			return rollbackCallCount;
		}

		void setThrowException( Throwable throwable ) {
			this.throwable = throwable;
		}
	}

}
