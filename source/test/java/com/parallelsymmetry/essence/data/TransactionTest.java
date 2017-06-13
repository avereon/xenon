package com.parallelsymmetry.essence.data;

import com.parallelsymmetry.essence.data.event.DataAttributeEvent;

import java.util.Objects;

public class TransactionTest {
//public class TransactionTest extends DataTestCase {

//	@Override
//	public void tearDown() {
//		super.tearDown();
//		Transaction.reset();
//	}
//
//	@Test
//	public void testTransaction() {
//		MockDataNode data = new MockDataNode();
//		DataEventWatcher handler = new DataEventWatcher();
//		data.addDataListener( handler );
//		assertNodeState( data, false, 0 );
//		assertEventCounts( handler, 0, 0, 0 );
//
//		Transaction.create();
//		data.setAttribute( "attribute0", "value0" );
//		data.setAttribute( "attribute1", "value1" );
//		data.setAttribute( "attribute2", "value2" );
//		assertNodeState( data, false, 0 );
//		assertEventCounts( handler, 0, 0, 0 );
//
//		Transaction.commit();
//		assertNodeState( data, true, 3 );
//		assertEventCounts( handler, 1, 1, 3 );
//
//		int index = 0;
//		assertEventState( handler, index++, DataEvent.Type.DATA_ATTRIBUTE, DataEvent.Action.INSERT, data, data, "attribute0", null, "value0" );
//		assertEventState( handler, index++, DataEvent.Type.DATA_ATTRIBUTE, DataEvent.Action.INSERT, data, data, "attribute1", null, "value1" );
//		assertEventState( handler, index++, DataEvent.Type.DATA_ATTRIBUTE, DataEvent.Action.INSERT, data, data, "attribute2", null, "value2" );
//		assertEventState( handler, index++, DataEvent.Type.META_ATTRIBUTE, DataEvent.Action.MODIFY, data, DataNode.MODIFIED, false, true );
//		assertEventState( handler, index++, DataEvent.Type.DATA_CHANGED, DataEvent.Action.MODIFY, data );
//		assertEquals( index++, handler.getEvents().size() );
//	}
//
//	@Test
//	public void testNestedTransaction() {
//		MockDataNode data1 = new MockDataNode();
//		DataEventWatcher handler1 = new DataEventWatcher();
//		data1.addDataListener( handler1 );
//		assertNodeState( data1, false, 0 );
//		assertEventCounts( handler1, 0, 0, 0 );
//
//		MockDataNode data2 = new MockDataNode();
//		DataEventWatcher handler2 = new DataEventWatcher();
//		data2.addDataListener( handler2 );
//		assertNodeState( data2, false, 0 );
//		assertEventCounts( handler2, 0, 0, 0 );
//
//		Transaction.create();
//		data1.setAttribute( "key1", "value1" );
//
//		Transaction.create( true );
//		data2.setAttribute( "key2", "value2" );
//
//		assertNodeState( data1, false, 0 );
//		assertNodeState( data2, false, 0 );
//		Transaction.commit();
//
//		assertNodeState( data1, false, 0 );
//		assertNodeState( data2, true, 1 );
//
//		Transaction.commit();
//
//		assertNodeState( data1, true, 1 );
//		assertNodeState( data2, true, 1 );
//	}
//
//	@Test
//	public void testSubmitWithNoTransaction() throws Exception {
//		try {
//			Transaction.submit( null );
//			fail( "NullPointerException should be thrown." );
//		} catch( NullPointerException exception ) {
//			assertEquals( "Transaction must be created first.", exception.getMessage() );
//		}
//	}
//
//	@Test
//	public void testCommitWithNoTransaction() throws Exception {
//		try {
//			Transaction.commit();
//		} catch( NullPointerException exception ) {
//			assertEquals( "Transaction must be created first.", exception.getMessage() );
//		}
//	}
//
//	@Test
//	public void testRollbackWithNoTransaction() throws Exception {
//		try {
//			Transaction.revert();
//			fail( "NullPointerException should be thrown." );
//		} catch( NullPointerException exception ) {
//			assertEquals( "Transaction must be created first.", exception.getMessage() );
//		}
//	}
//
//	@Test
//	public void testOverlappingTransactions() throws Exception {
//		MockDataNode node = new MockDataNode();
//		DataEventWatcher watcher = node.getDataEventWatcher();
//
//		// Initial transaction.
//		Transaction.create();
//		node.setAttribute( "key1", "value1" );
//		node.setAttribute( "key2", "value2" );
//		assertEventCounts( watcher, 0, 0, 0, 0, 0 );
//		assertNull( node.getAttribute( "key1" ) );
//		assertNull( node.getAttribute( "key2" ) );
//		assertNull( node.getAttribute( "key3" ) );
//
//		// Overlapping transaction.
//		Transaction.create();
//		node.setAttribute( "key3", "value3" );
//		assertEventCounts( watcher, 0, 0, 0, 0, 0 );
//		assertNull( node.getAttribute( "key1" ) );
//		assertNull( node.getAttribute( "key2" ) );
//		assertNull( node.getAttribute( "key3" ) );
//
//		// Overlapping commit.
//		Transaction.commit();
//		assertEventCounts( watcher, 0, 0, 0, 0, 0 );
//		assertNull( node.getAttribute( "key1" ) );
//		assertNull( node.getAttribute( "key2" ) );
//		assertNull( node.getAttribute( "key3" ) );
//		watcher.reset();
//
//		// Final commit.
//		Transaction.commit();
//		assertEventCounts( watcher, 1, 1, 3, 0, 0 );
//		assertEquals( "value1", node.getAttribute( "key1" ) );
//		assertEquals( "value2", node.getAttribute( "key2" ) );
//		assertEquals( "value3", node.getAttribute( "key3" ) );
//		watcher.reset();
//	}
//
//	@Test
//	public void testSetAttributeToSameValue() {
//		MockDataNode data = new MockDataNode();
//		DataEventWatcher handler = new DataEventWatcher();
//		data.addDataListener( handler );
//		assertNodeState( data, false, 0 );
//		assertEventCounts( handler, 0, 0, 0 );
//
//		Transaction.create();
//
//		data.setAttribute( "attribute0", "value0" );
//		data.setAttribute( "attribute1", "value1" );
//		data.setAttribute( "attribute2", "value2" );
//		assertNodeState( data, false, 0 );
//		assertEventCounts( handler, 0, 0, 0 );
//
//		Transaction.commit();
//		assertNodeState( data, true, 3 );
//		assertEventCounts( handler, 1, 1, 3 );
//
//		int index = 0;
//		assertEventState( handler, index++, DataEvent.Type.DATA_ATTRIBUTE, DataEvent.Action.INSERT, data, data, "attribute0", null, "value0" );
//		assertEventState( handler, index++, DataEvent.Type.DATA_ATTRIBUTE, DataEvent.Action.INSERT, data, data, "attribute1", null, "value1" );
//		assertEventState( handler, index++, DataEvent.Type.DATA_ATTRIBUTE, DataEvent.Action.INSERT, data, data, "attribute2", null, "value2" );
//		assertEventState( handler, index++, DataEvent.Type.META_ATTRIBUTE, DataEvent.Action.MODIFY, data, DataNode.MODIFIED, false, true );
//		assertEventState( handler, index++, DataEvent.Type.DATA_CHANGED, DataEvent.Action.MODIFY, data );
//		assertEquals( index++, handler.getEvents().size() );
//
//		handler.reset();
//		Transaction.create();
//		data.setAttribute( "attribute0", "value0" );
//		data.setAttribute( "attribute1", "value1" );
//		data.setAttribute( "attribute2", "value2" );
//		assertNodeState( data, true, 3 );
//		assertEventCounts( handler, 0, 0, 0 );
//
//		Transaction.commit();
//		assertNodeState( data, true, 3 );
//		assertEventCounts( handler, 0, 0, 0 );
//	}
//
//	@Test
//	public void testTransactionWithEventModifingNode() {
//		MockDataNode node = new MockDataNode();
//		node.addDataListener( new ModifyingDataHandler( node, "time", System.nanoTime() ) );
//		Transaction.create();
//		try {
//			node.setAttribute( "fire", "event" );
//			Transaction.commit();
//			fail( "RuntimeException should be thrown due to modifying data listener." );
//		} catch( RuntimeException exception ) {
//			// Intentionally ignore exception.
//		}
//	}
//
//	@Test
//	public void testTransactionWithEventModifyingSeparateNode() {
//		Log.setLevel( Log.INFO );
//		MockDataNode node0 = new MockDataNode();
//		MockDataNode node1 = new MockDataNode();
//		node0.addDataListener( new ModifyingDataHandler( node1, "name", "node1" ) );
//		Transaction.create();
//		node0.setAttribute( "fire", "event" );
//		Transaction.commit();
//		assertEquals( "node1", node1.getAttribute( "name" ) );
//	}
//
//	@Test
//	public void testTransactionByModifyingChild() {
//		MockDataList parent = new MockDataList( "parent" );
//		MockDataNode child = new MockDataNode( "child" );
//		DataEventWatcher parentWatcher = parent.getDataEventWatcher();
//		DataEventWatcher childWatcher = child.getDataEventWatcher();
//
//		// Set up the data model.
//		parent.add( child );
//
//		// Set the parent unmodified.
//		parent.setModified( false );
//		assertFalse( parent.isModified() );
//		assertFalse( child.isModified() );
//		parentWatcher.reset();
//		childWatcher.reset();
//
//		// Start a transaction
//		Transaction.create();
//
//		// Set the child attribute but nothing should happen
//		// because the transaction has not been committed yet.
//		child.setAttribute( "key1", "value1" );
//		assertFalse( parent.isModified() );
//		assertFalse( child.isModified() );
//		assertEventCounts( parentWatcher, 0, 0, 0, 0, 0 );
//		assertEventCounts( childWatcher, 0, 0, 0, 0, 0 );
//		parentWatcher.reset();
//		childWatcher.reset();
//
//		// Commit the transaction.
//		Transaction.commit();
//		assertTrue( parent.isModified() );
//		assertTrue( child.isModified() );
//		assertEquals( "value1", child.getAttribute( "key1" ) );
//		assertEventCounts( parentWatcher, 1, 1, 1, 0, 0 );
//		assertEventState( parentWatcher, 0, DataEvent.Type.DATA_ATTRIBUTE, DataEvent.Action.INSERT, parent, child, "key1", null, "value1" );
//		assertEventState( parentWatcher, 1, DataEvent.Type.META_ATTRIBUTE, DataEvent.Action.MODIFY, parent, DataNode.MODIFIED, false, true );
//		assertEventState( parentWatcher, 2, DataEvent.Type.DATA_CHANGED, DataEvent.Action.MODIFY, parent );
//		assertEventCounts( childWatcher, 1, 1, 1, 0, 0 );
//		assertEventState( childWatcher, 0, DataEvent.Type.DATA_ATTRIBUTE, DataEvent.Action.INSERT, child, child, "key1", null, "value1" );
//		assertEventState( childWatcher, 1, DataEvent.Type.META_ATTRIBUTE, DataEvent.Action.MODIFY, child, DataNode.MODIFIED, false, true );
//		assertEventState( childWatcher, 2, DataEvent.Type.DATA_CHANGED, DataEvent.Action.MODIFY, child );
//		parentWatcher.reset();
//		childWatcher.reset();
//
//		// Unset the the child attribute but nothing should happen
//		// because the transaction has not been committed yet.
//		Transaction.create();
//		child.setAttribute( "key1", null );
//		assertEventCounts( parentWatcher, 0, 0, 0, 0, 0 );
//		assertEventCounts( childWatcher, 0, 0, 0, 0, 0 );
//		parentWatcher.reset();
//		childWatcher.reset();
//
//		// Commit the transaction.
//		Transaction.commit();
//		assertFalse( parent.isModified() );
//		assertFalse( child.isModified() );
//		assertEquals( null, child.getAttribute( "key1" ) );
//		assertEventCounts( parentWatcher, 1, 1, 1, 0, 0 );
//		assertEventState( parentWatcher, 0, DataEvent.Type.DATA_ATTRIBUTE, DataEvent.Action.REMOVE, parent, child, "key1", "value1", null );
//		assertEventState( parentWatcher, 1, DataEvent.Type.META_ATTRIBUTE, DataEvent.Action.MODIFY, parent, DataNode.MODIFIED, true, false );
//		assertEventState( parentWatcher, 2, DataEvent.Type.DATA_CHANGED, DataEvent.Action.MODIFY, parent );
//		assertEventCounts( childWatcher, 1, 1, 1, 0, 0 );
//		assertEventState( childWatcher, 0, DataEvent.Type.DATA_ATTRIBUTE, DataEvent.Action.REMOVE, child, child, "key1", "value1", null );
//		assertEventState( childWatcher, 1, DataEvent.Type.META_ATTRIBUTE, DataEvent.Action.MODIFY, child, DataNode.MODIFIED, true, false );
//		assertEventState( childWatcher, 2, DataEvent.Type.DATA_CHANGED, DataEvent.Action.MODIFY, child );
//	}
//
//	@Test
//	public void testTransactionByModifyingGrandChild() {
//		MockDataList parent = new MockDataList( "parent" );
//		MockDataList child = new MockDataList( "child" );
//		MockDataNode grandchild = new MockDataNode( "grandchild" );
//		DataEventWatcher watcher = parent.getDataEventWatcher();
//
//		parent.add( child );
//		child.add( grandchild );
//		parent.setModified( false );
//		assertFalse( parent.isModified() );
//		assertFalse( child.isModified() );
//		assertFalse( grandchild.isModified() );
//		watcher.reset();
//
//		Transaction.create();
//		grandchild.setAttribute( "key1", "value1" );
//		assertEventCounts( watcher, 0, 0, 0, 0, 0 );
//		watcher.reset();
//
//		Transaction.commit();
//		assertEventCounts( watcher, 1, 1, 1, 0, 0 );
//		watcher.reset();
//
//		Transaction.create();
//		grandchild.setAttribute( "key1", null );
//		assertEventCounts( watcher, 0, 0, 0, 0, 0 );
//		watcher.reset();
//
//		Transaction.commit();
//		assertEventCounts( watcher, 1, 1, 1, 0, 0 );
//		watcher.reset();
//	}
//
//	/**
//	 * This is a fairly complex test to ensure that the transaction handling can
//	 * handle two nodes that have overridden the equals() and hashCode() methods
//	 * to cause the two nodes to have the same hash code and are equal according
//	 * to the equals() method.
//	 */
//	@Test
//	public void testNodesThatOverrideHashcodeAndEqualsWithZeroHashcode() {
//		EqualHashOverrideNode node1 = new EqualHashOverrideNode();
//		EqualHashOverrideNode node2 = new EqualHashOverrideNode();
//		DataEventWatcher watcher1 = new DataEventWatcher();
//		DataEventWatcher watcher2 = new DataEventWatcher();
//		node1.addDataListener( watcher1 );
//		node2.addDataListener( watcher2 );
//
//		// Ensure that the nodes are in the correct state.
//		assertEquals( node1, node2 );
//		assertEquals( node1.hashCode(), node2.hashCode() );
//		assertFalse( node1 == node2 );
//		assertEventCounts( watcher1, 0, 0, 0 );
//		assertEventCounts( watcher2, 0, 0, 0 );
//
//		// Use a transaction to change both nodes at the same time.
//		Transaction.create();
//		node1.setAttribute( "name", "node1" );
//		node2.setAttribute( "name", "node2" );
//		Transaction.commit();
//
//		// Check the attributes.
//		assertEquals( "node1", node1.getAttribute( "name" ) );
//		assertEquals( "node2", node2.getAttribute( "name" ) );
//		// Check the equals() and hashCode() methods.
//		assertEquals( node1, node2 );
//		assertEquals( node1.hashCode(), node2.hashCode() );
//		assertFalse( node1 == node2 );
//		// Check the event counts.
//		assertEventCounts( watcher1, 1, 1, 1 );
//		assertEventCounts( watcher2, 1, 1, 1 );
//	}
//
//	/**
//	 * This is a fairly complex test to ensure that the transaction handling can
//	 * handle two nodes that have overridden the equals() and hashCode() methods
//	 * to cause the two nodes to have the same hash code and are equal according
//	 * to the equals() method.
//	 */
//	@Test
//	public void testNodesThatOverrideHashcodeAndEqualsWithNonZeroHashcode() {
//		EqualHashOverrideNode node1 = new EqualHashOverrideNode();
//		EqualHashOverrideNode node2 = new EqualHashOverrideNode();
//		DataEventWatcher watcher1 = new DataEventWatcher();
//		DataEventWatcher watcher2 = new DataEventWatcher();
//		node1.addDataListener( watcher1 );
//		node2.addDataListener( watcher2 );
//
//		// Ensure that the nodes are in the correct state.
//		assertEquals( node1, node2 );
//		assertEquals( node1.hashCode(), node2.hashCode() );
//		assertFalse( node1 == node2 );
//		assertEventCounts( watcher1, 0, 0, 0 );
//		assertEventCounts( watcher2, 0, 0, 0 );
//
//		node1.setKey( "value1" );
//		node2.setKey( "value1" );
//		node1.setModified( false );
//		node2.setModified( false );
//		watcher1.reset();
//		watcher2.reset();
//
//		// Use a transaction to change both nodes at the same time.
//		Transaction.create();
//		node1.setAttribute( "name", "node1" );
//		node2.setAttribute( "name", "node2" );
//		Transaction.commit();
//
//		// Check the attributes.
//		assertEquals( "node1", node1.getAttribute( "name" ) );
//		assertEquals( "node2", node2.getAttribute( "name" ) );
//		// Check the equals() and hashCode() methods.
//		assertEquals( node1, node2 );
//		assertEquals( node1.hashCode(), node2.hashCode() );
//		assertFalse( node1 == node2 );
//		// Check the event counts.
//		assertEventCounts( watcher1, 1, 1, 1 );
//		assertEventCounts( watcher2, 1, 1, 1 );
//	}
//
//	public void testThreadLocalTransaction() {
//		Log.setLevel( Log.TRACE );
//		ExecutorThread thread1 = new ExecutorThread( "Thread1" );
//		ExecutorThread thread2 = new ExecutorThread( "Thread2" );
//
//		thread1.start();
//		thread2.start();
//
//		try {
//			GetTransaction get = new GetTransaction();
//			StartTransaction start = new StartTransaction();
//			CommitTransaction commit = new CommitTransaction();
//
//			thread1.execute( get );
//			assertNull( get.getTransaction() );
//
//			thread1.execute( start );
//			Transaction transactionA = start.getTransaction();
//			assertNotNull( transactionA );
//
//			thread1.execute( get );
//			Transaction transactionB = get.getTransaction();
//			assertTrue( transactionA == transactionB );
//
//			thread2.execute( get );
//			assertNull( get.getTransaction() );
//
//			thread2.execute( start );
//			Transaction transactionC = start.getTransaction();
//			assertNotNull( transactionC );
//
//			thread2.execute( get );
//			Transaction transactionD = get.getTransaction();
//			assertTrue( transactionC == transactionD );
//			assertTrue( transactionA != transactionC );
//
//			thread1.execute( commit );
//			assertTrue( commit.getResult() );
//			thread1.execute( get );
//			assertNull( get.getTransaction() );
//
//			thread2.execute( commit );
//			assertTrue( commit.getResult() );
//			thread2.execute( get );
//			assertNull( get.getTransaction() );
//		} finally {
//			thread2.terminate();
//			thread1.terminate();
//
//			thread2.waitFor();
//			thread1.waitFor();
//		}
//	}

	private class ExecutorThread extends Thread {

		private boolean execute = true;

		private Runnable runnable;

		private boolean running;

		public ExecutorThread( String name ) {
			super( name );
		}

		public synchronized void execute( Runnable runnable ) {
			//Log.write( getName(), ".execute()" );
			this.runnable = runnable;
			notifyAll();
			while( this.runnable != null ) {
				try {
					wait( 1000 );
				} catch( InterruptedException exception ) {
					break;
				}
			}
		}

		@Override
		public synchronized void run() {
			//Log.write( getName(), ".run()" );
			while( execute ) {
				running = true;
				while( runnable == null ) {
					try {
						wait( 1000 );
					} catch( InterruptedException exception ) {
						break;
					}
				}
				if( runnable != null ) {
					//Log.write( getName(), ": Runnable.run()" );
					runnable.run();
				}
				runnable = null;
				notifyAll();
			}
			running = false;
		}

		public void terminate() {
			this.execute = false;
			interrupt();
		}

		public synchronized void waitFor() {
			while( running ) {
				try {
					wait( 1000 );
				} catch( InterruptedException exception ) {
					break;
				}
			};
		}

	}

	private class StartTransaction implements Runnable {

		private Transaction transaction;

		@Override
		public void run() {
			transaction = Transaction.create();
			//Log.write( Thread.currentThread().getName(), ": Srt Transaction: ", transaction == null ? "null" : transaction.hashCode() );
		}

		public Transaction getTransaction() {
			return transaction;
		}

	}

	private class GetTransaction implements Runnable {

		private Transaction transaction;

		@Override
		public void run() {
			transaction = Transaction.current();

			//Log.write( Thread.currentThread().getName(), ": Get Transaction: ", transaction == null ? "null" : transaction.hashCode() );
		}

		public Transaction getTransaction() {
			return transaction;
		}

	}

	private class CommitTransaction implements Runnable {

		private boolean result;

		@Override
		public void run() {
			result = Transaction.commit();
		}

		public boolean getResult() {
			return result;
		}

	}

	private class ModifyingDataHandler extends DataAdapter {

		private DataNode node;

		private String name;

		private Object value;

		public ModifyingDataHandler( DataNode node, String name, Object value ) {
			this.node = node;
			this.name = name;
			this.value = value;
		}

		@Override
		public void dataAttributeChanged( DataAttributeEvent event ) {
			node.setAttribute( name, value );
		}

	}

	private class EqualHashOverrideNode extends DataNode {

		private static final String KEY = "key";

		public String getKey() {
			return (String)getAttribute( KEY );
		}

		public void setKey( String key ) {
			setAttribute( KEY, key );
		}

		@Override
		public int hashCode() {
			String key = getKey();
			return key == null ? 0 : key.hashCode();
		}

		@Override
		public boolean equals( Object object ) {
			if( !( object instanceof EqualHashOverrideNode ) ) return false;
			String key = getKey();
			return Objects.equals( key, ( (EqualHashOverrideNode)object ).getKey() );
		}

	}

}
