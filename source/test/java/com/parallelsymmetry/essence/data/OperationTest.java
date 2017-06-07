package com.parallelsymmetry.essence.data;

import org.junit.Test;

import java.util.EventObject;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class OperationTest {

	@Test
	public void testGetSource() throws Exception {
		//Object source = new Object();
		DataNode source = new MockDataNode();
		MockOperation operation = new MockOperation( source );
		assertThat( operation.getData(), is( source ) );
	}

	@Test
	public void testProcess() throws Exception {
		//Object source = new Object();
		DataNode source = new MockDataNode();
		MockOperation operation = new MockOperation( source );
		OperationResult result = operation.process();

		assertThat( result.getOperation(), is( operation ) );
		assertThat( result.getEvents().size(), is( 1 ) );
	}

	private class MockOperation extends Operation {

		boolean processed;

		public MockOperation( DataNode source ) {
			super( source );
		}

		@Override
		protected OperationResult process() {
			processed = true;
			OperationResult result = new OperationResult( this );
			result.addEvent( new MockEvent( source ) );
			return result;
		}

	}

	private class MockEvent extends EventObject {

		public MockEvent( Object source ) {
			super( source );
		}

	}

}
