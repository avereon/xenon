package com.parallelsymmetry.essence.node;

import com.parallelsymmetry.essence.data.CircularReferenceException;
import com.parallelsymmetry.essence.transaction.Txn;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class NodeTest {

	private Node data;

	@Before
	public void setup() throws Exception {
		data = new MockNode();
	}

	@Test
	public void testNewNodeModifiedState() {
		assertThat( data.isModified(), is( false ) );
	}

	@Test
	public void testModifyByFlagAndUnmodifyByFlag() {
		NodeWatcher watcher = new NodeWatcher();
		data.addNodeListener( watcher );
		assertThat( data.getModifiedValueCount(), is( 0 ) );
		assertThat( data.isModified(), is( false ) );
		assertThat( watcher, hasEventCounts( 0, 0, 0 ) );

		data.setModified( true );
		assertThat( data.getModifiedValueCount(), is( 0 ) );
		assertThat( data.isModified(), is( true ) );
		assertThat( watcher, hasEventCounts( 1, 1, 0 ) );

		data.setModified( false );
		assertThat( data.getModifiedValueCount(), is( 0 ) );
		assertThat( data.isModified(), is( false ) );
		assertThat( watcher, hasEventCounts( 2, 2, 0 ) );
	}

	@Test
	public void testModifyByValueAndUnmodifyByFlag() {
		NodeWatcher watcher = new NodeWatcher();
		data.addNodeListener( watcher );
		assertThat( data, hasStates( false, 0 ) );
		assertThat( watcher, hasEventCounts( 0, 0, 0 ) );

		data.setValue( "id", 423984 );
		assertThat( data, hasStates( true, 1 ) );
		assertThat( watcher, hasEventCounts( 1, 1, 1 ) );

		data.setModified( false );
		assertThat( data, hasStates( false, 0 ) );
		assertThat( watcher, hasEventCounts( 2, 2, 1 ) );
	}

	@Test
	public void testModifyByValueAndUnmodifyByValue() {
		NodeWatcher watcher = new NodeWatcher();
		data.addNodeListener( watcher );
		assertThat( data, hasStates( false, 0 ) );
		assertThat( watcher, hasEventCounts( 0, 0, 0 ) );

		data.setValue( "id", 423984 );
		assertThat( data, hasStates( true, 1 ) );
		assertThat( watcher, hasEventCounts( 1, 1, 1 ) );

		data.setValue( "id", null );
		assertThat( data, hasStates( false, 0 ) );
		assertThat( watcher, hasEventCounts( 2, 2, 2 ) );
	}

	@Test
	public void testValues() {
		String key = "key";
		Object value = "value";

		assertThat( data.getValue( key ), is( nullValue() ) );

		data.setValue( key, value );
		assertThat( data.getValue( key ), is( value ) );

		data.setValue( key, null );
		assertThat( data.getValue( key ), is( nullValue() ) );
	}

	@Test
	public void testObjectAttribute() {
		String key = "key";
		Object value = new Object();

		data.setValue( key, value );

		assertThat( data.getValue( key ), is( value ) );
	}

	@Test
	public void testStringAttribute() {
		String key = "key";
		String value = "value";

		data.setValue( key, value );

		assertThat( data.getValue( key ), is( value ) );
	}

	@Test
	public void testBooleanAttribute() {
		String key = "key";
		boolean value = true;

		data.setValue( key, value );

		assertThat( data.getValue( key ), is( value ) );
	}

	@Test
	public void testIntegerAttribute() {
		String key = "key";
		int value = 0;

		data.setValue( key, value );

		assertThat( data.getValue( key ), is( value ) );
	}

	@Test
	public void testSetNullAttributeToNull() {
		NodeWatcher watcher = new NodeWatcher();
		data.addNodeListener( watcher );

		data.setValue( "attribute", null );
		assertThat( watcher, hasEventCounts( 0, 0, 0 ) );
	}

	@Test
	public void testSetAttributeWithNullName() {
		NodeWatcher watcher = new NodeWatcher();
		data.addNodeListener( watcher );

		try {
			data.setValue( null, "value" );
			Assert.fail( "Null value keys are not allowed" );
		} catch( NullPointerException exception ) {
			assertThat( exception.getMessage(), is( "Value key cannot be null" ) );
		}
		assertThat( watcher, hasEventCounts( 0, 0, 0 ) );
	}

	@Test
	public void testGetAttributeWithNullName() {
		try {
			data.getValue( null );
			Assert.fail( "Null attribute names are not allowed." );
		} catch( NullPointerException exception ) {
			assertThat( exception.getMessage(), is( "Value key cannot be null" ) );
		}
	}

	@Test
	public void testNullAttributeValues() {
		NodeWatcher watcher = new NodeWatcher();
		data.addNodeListener( watcher );

		// Assert initial values
		assertThat( data.getValue( "attribute" ), is( nullValue() ) );
		assertThat( data, hasStates( false, 0 ) );
		assertThat( watcher, hasEventCounts( 0, 0, 0 ) );

		// Set the value to null and make sure nothing happens
		data.setValue( "attribute", null );
		assertThat( data.getValue( "attribute" ), is( nullValue() ) );
		assertThat( data, hasStates( false, 0 ) );
		assertThat( watcher, hasEventCounts( 0, 0, 0 ) );

		// Set the value
		data.setValue( "attribute", "value" );
		assertThat( data.getValue( "attribute" ), is( "value" ) );
		assertThat( data, hasStates( true, 1 ) );
		assertThat( watcher, hasEventCounts( 1, 1, 1 ) );

		// Set the value to the same value and make sure nothing happens
		data.setValue( "attribute", "value" );
		assertThat( data.getValue( "attribute" ), is( "value" ) );
		assertThat( data, hasStates( true, 1 ) );
		assertThat( watcher, hasEventCounts( 1, 1, 1 ) );

		// Set the value back to null
		data.setValue( "attribute", null );
		assertThat( data.getValue( "attribute" ), is( nullValue() ) );
		assertThat( data, hasStates( false, 0 ) );
		assertThat( watcher, hasEventCounts( 2, 2, 2 ) );

		// Set the value to null again and make sure nothing happens
		data.setValue( "attribute", null );
		assertThat( data.getValue( "attribute" ), is( nullValue() ) );
		assertThat( data, hasStates( false, 0 ) );
		assertThat( watcher, hasEventCounts( 2, 2, 2 ) );
	}

	@Test
	public void testGetAndSetValue() {
		NodeWatcher watcher = new NodeWatcher();
		data.addNodeListener( watcher );

		assertThat( data.getValue( "x" ), is( nullValue() ) );
		assertThat( data.getValue( "y" ), is( nullValue() ) );
		assertThat( data.getValue( "z" ), is( nullValue() ) );
		assertThat( data, hasStates( false, 0 ) );
		assertThat( watcher, hasEventCounts( 0, 0, 0 ) );

		data.setValue( "x", 1.0 );
		assertThat( data.getValue( "x" ), is( 1.0 ) );
		assertThat( data.getValue( "y" ), is( nullValue() ) );
		assertThat( data.getValue( "z" ), is( nullValue() ) );
		assertThat( data, hasStates( true, 1 ) );
		assertThat( watcher, hasEventCounts( 1, 1, 1 ) );

		data.setValue( "x", 0.0 );
		assertThat( data.getValue( "x" ), is( 0.0 ) );
		assertThat( data.getValue( "y" ), is( nullValue() ) );
		assertThat( data.getValue( "z" ), is( nullValue() ) );
		assertThat( data, hasStates( true, 1 ) );
		assertThat( watcher, hasEventCounts( 2, 1, 2 ) );
	}

	@Test
	public void testModifiedBySetAttribute() {
		NodeWatcher watcher = new NodeWatcher();
		data.addNodeListener( watcher );

		assertThat( data, hasStates( false, 0 ) );
		assertThat( watcher, hasEventCounts( 0, 0, 0 ) );

		data.setValue( "x", 1 );
		data.setValue( "y", 2 );
		data.setValue( "z", 3 );
		assertThat( data, hasStates( true, 3 ) );
		assertThat( watcher, hasEventCounts( 3, 1, 3 ) );
	}

	@Test
	public void testUnmodifiedByUnsetValue() {
		NodeWatcher watcher = new NodeWatcher();
		data.addNodeListener( watcher );

		assertThat( data, hasStates( false, 0 ) );
		assertThat( watcher, hasEventCounts( 0, 0, 0 ) );

		data.setValue( "x", 0 );
		data.setValue( "y", 0 );
		data.setValue( "z", 0 );
		data.setModified( false );
		assertThat( data, hasStates( false, 0 ) );
		assertThat( watcher, hasEventCounts( 4, 2, 3 ) );

		data.setValue( "x", 1 );
		data.setValue( "y", 2 );
		data.setValue( "z", 3 );
		assertThat( data, hasStates( true, 3 ) );
		assertThat( watcher, hasEventCounts( 7, 3, 6 ) );

		data.setValue( "x", 0 );
		data.setValue( "y", 0 );
		data.setValue( "z", 0 );
		assertThat( data, hasStates( false, 0 ) );
		assertThat( watcher, hasEventCounts( 10, 4, 9 ) );
	}

	@Test
	public void testModifiedAttributeCountResetByCommit() {
		NodeWatcher watcher = new NodeWatcher();
		data.addNodeListener( watcher );

		assertThat( data, hasStates( false, 0 ) );
		assertThat( watcher, hasEventCounts( 0, 0, 0 ) );

		data.setValue( "x", 1 );
		data.setValue( "y", 2 );
		data.setValue( "z", 3 );
		assertThat( data, hasStates( true, 3 ) );
		assertThat( watcher, hasEventCounts( 3, 1, 3 ) );

		data.setModified( false );
		assertThat( data, hasStates( false, 0 ) );
		assertThat( watcher, hasEventCounts( 4, 2, 3 ) );
	}

	@Test
	public void testTestMetaValueModified() {
		String key = "hidden";

		assertThat( data.getFlag( key ), is( false ) );
		data.setFlag( key, true );
		assertThat( data.getFlag( key ), is( true ) );
		data.setFlag( key, false );
		assertThat( data.getFlag( key ), is( false ) );
	}

	@Test
	public void testSetNullMetaValueToFalse() {
		NodeWatcher watcher = new NodeWatcher();
		data.addNodeListener( watcher );

		data.setFlag( "attribute", false );
		assertThat( watcher, hasEventCounts( 0, 0, 0 ) );
	}

	@Test
	public void testSetMetaValueWithNullName() {
		NodeWatcher watcher = new NodeWatcher();
		data.addNodeListener( watcher );

		try {
			data.setFlag( null, true );
			Assert.fail( "Null flag keys are not allowed" );
		} catch( NullPointerException exception ) {
			assertThat( exception.getMessage(), is( "Flag key cannot be null" ) );
		}
		assertThat( watcher, hasEventCounts( 0, 0, 0 ) );
	}

	@Test
	public void testGetMetaValueWithNullName() {
		NodeWatcher watcher = new NodeWatcher();
		data.addNodeListener( watcher );

		try {
			data.getFlag( null );
			Assert.fail( "Null flag keys are not allowed" );
		} catch( NullPointerException exception ) {
			assertThat( exception.getMessage(), is( "Flag key cannot be null" ) );
		}
		assertThat( watcher, hasEventCounts( 0, 0, 0 ) );
	}

	@Test
	public void testResources() {
		NodeWatcher watcher = new NodeWatcher();
		data.addNodeListener( watcher );

		assertThat( data, hasStates( false, 0 ) );
		assertThat( watcher, hasEventCounts( 0, 0, 0 ) );

		data.putResource( "name", "value" );
		assertThat( data.getResource( "name" ), is( "value" ) );
		assertThat( data, hasStates( false, 0 ) );
		assertThat( watcher, hasEventCounts( 0, 0, 0 ) );

		data.putResource( "name", null );
		assertThat( data.getResource( "name" ), is( nullValue() ) );
		assertThat( data, hasStates( false, 0 ) );
		assertThat( watcher, hasEventCounts( 0, 0, 0 ) );
	}

	@Test
	public void testDataEventNotification() {
		NodeWatcher watcher = new NodeWatcher();
		data.addNodeListener( watcher );

		assertThat( data, hasStates( false, 0 ) );
		assertThat( watcher, hasEventCounts( 0, 0, 0 ) );

		// Set a value
		data.setValue( "attribute", "value0" );
		assertThat( data, hasStates( true, 1 ) );
		assertThat( watcher, hasEventCounts( 1, 1, 1 ) );

		// Set the value to the same value
		data.setValue( "attribute", "value0" );
		assertThat( data, hasStates( true, 1 ) );
		assertThat( watcher, hasEventCounts( 1, 1, 1 ) );

		// Modify the value
		data.setValue( "attribute", "value1" );
		assertThat( data, hasStates( true, 1 ) );
		assertThat( watcher, hasEventCounts( 2, 1, 2 ) );

		// Remove the attribute.
		data.setValue( "attribute", null );
		assertThat( data, hasStates( false, 0 ) );
		assertThat( watcher, hasEventCounts( 3, 2, 3 ) );

		int index = 0;
		assertEventState( watcher, index++, NodeEvent.Type.VALUE_CHANGED, data, "attribute", null, "value0" );
		assertEventState( watcher, index++, NodeEvent.Type.FLAG_CHANGED, data, Node.MODIFIED, false, true );
		assertEventState( watcher, index++, NodeEvent.Type.NODE_CHANGED, data );
		assertEventState( watcher, index++, NodeEvent.Type.VALUE_CHANGED, data, "attribute", "value0", "value1" );
		assertEventState( watcher, index++, NodeEvent.Type.NODE_CHANGED, data );
		assertEventState( watcher, index++, NodeEvent.Type.VALUE_CHANGED, data, "attribute", "value1", null );
		assertEventState( watcher, index++, NodeEvent.Type.FLAG_CHANGED, data, Node.MODIFIED, true, false );
		assertEventState( watcher, index++, NodeEvent.Type.NODE_CHANGED, data );
		assertThat( watcher.getEvents().size(), is( index ) );
	}

	@Test
	public void testEventsWithModifiedFlagFalse() {
		NodeWatcher watcher = new NodeWatcher();
		data.addNodeListener( watcher );

		assertThat( data, hasStates( false, 0 ) );
		assertThat( watcher, hasEventCounts( 0, 0, 0 ) );

		// Change a value
		data.setValue( "attribute", "value0" );
		assertThat( data, hasStates( true, 1 ) );
		assertThat( watcher, hasEventCounts( 1, 1, 1 ) );

		// Set the modified flag to false
		data.setModified( false );
		assertThat( data, hasStates( false, 0 ) );
		assertThat( watcher, hasEventCounts( 2, 2, 1 ) );

		// Set the modified flag to false again
		data.setModified( false );
		assertThat( data, hasStates( false, 0 ) );
		assertThat( watcher, hasEventCounts( 2, 2, 1 ) );

		int index = 0;
		assertEventState( watcher, index++, NodeEvent.Type.VALUE_CHANGED, data, "attribute", null, "value0" );
		assertEventState( watcher, index++, NodeEvent.Type.FLAG_CHANGED, data, Node.MODIFIED, false, true );
		assertEventState( watcher, index++, NodeEvent.Type.NODE_CHANGED, data );
		assertEventState( watcher, index++, NodeEvent.Type.FLAG_CHANGED, data, Node.MODIFIED, true, false );
		assertEventState( watcher, index++, NodeEvent.Type.NODE_CHANGED, data );
		assertThat( watcher.getEvents().size(), is( index ) );
	}

	@Test
	public void testCollapsingEventsWithTransaction() throws Exception {
		NodeWatcher watcher = new NodeWatcher();
		data.addNodeListener( watcher );

		assertThat( data, hasStates( false, 0 ) );
		assertThat( watcher, hasEventCounts( 0, 0, 0 ) );

		Txn.create();
		data.setValue( "a", "1" );
		data.setValue( "a", "2" );
		data.setValue( "a", "3" );
		data.setValue( "a", "4" );
		data.setValue( "a", "5" );
		Txn.commit();

		assertThat( data.getValue( "a" ), is( "5" ) );
		assertThat( data, hasStates( true, 1 ) );
		assertThat( watcher, hasEventCounts( 1, 1, 1 ) );
	}

	@Test
	public void testGetParent() {
		MockNode parent = new MockNode();
		MockNode child = new MockNode();
		assertThat( child.getParent(), is( nullValue() ) );

		String key = "key";

		parent.setValue( key, child );
		assertThat( child.getParent(), is( parent ) );

		parent.setValue( key, null );
		assertThat( child.getParent(), is( nullValue() ) );
	}

	@Test
	public void testGetNodePath() {
		MockNode parent = new MockNode();
		MockNode child = new MockNode();

		parent.setValue( "child", child );

		List<Node> path = parent.getNodePath();
		assertThat( path.size(), is( 1 ) );
		assertThat( path.get( 0 ), is( parent ) );

		path = child.getNodePath();
		assertThat( path.size(), is( 2 ) );
		assertThat( path.get( 0 ), is( parent ) );
		assertThat( path.get( 1 ), is( child ) );
	}

	// NEXT Continue implementing tests

	@Test
	public void testParentDataEventNotification() {
		MockNode data = new MockNode( "parent" );
		MockNode child = new MockNode( "child" );

		NodeWatcher watcher = new NodeWatcher();
		data.addNodeListener( watcher );

		data.setValue( "child", child );

		assertThat( child, hasStates( false, 0 ) );
		assertThat( watcher, hasEventCounts( 1, 1, 1 ) );

		// Set a value
		child.setValue( "attribute", "value0" );
		assertThat( child, hasStates( true, 1 ) );
		assertThat( watcher, hasEventCounts( 2, 1, 2 ) );

		// Set the value to the same value. Should do nothing.
		child.setValue( "attribute", "value0" );
		assertThat( child, hasStates( true, 1 ) );
		assertThat( watcher, hasEventCounts( 2, 1, 2 ) );

		// Modify the attribute.
		child.setValue( "attribute", "value1" );
		assertThat( child, hasStates( true, 1 ) );
		assertThat( watcher, hasEventCounts( 3, 1, 3 ) );

		// Remove the attribute.
		child.setValue( "attribute", null );
		assertThat( child, hasStates( false, 0 ) );
		assertThat( watcher, hasEventCounts( 4, 1, 4 ) );

		int index = 0;
		assertEventState( watcher, index++, NodeEvent.Type.VALUE_CHANGED, data, "child", null, child );
		assertEventState( watcher, index++, NodeEvent.Type.FLAG_CHANGED, data, Node.MODIFIED, false, true );
		assertEventState( watcher, index++, NodeEvent.Type.NODE_CHANGED, data );

		assertEventState( watcher, index++, NodeEvent.Type.VALUE_CHANGED, data, child, "attribute", null, "value0" );
		assertEventState( watcher, index++, NodeEvent.Type.NODE_CHANGED, data );
		assertEventState( watcher, index++, NodeEvent.Type.VALUE_CHANGED, data, child, "attribute", "value0", "value1" );
		assertEventState( watcher, index++, NodeEvent.Type.NODE_CHANGED, data );
		assertEventState( watcher, index++, NodeEvent.Type.VALUE_CHANGED, data, child, "attribute", "value1", null );
		assertEventState( watcher, index++, NodeEvent.Type.NODE_CHANGED, data );
		assertThat( watcher.getEvents().size(), is( index ) );
	}

	@Test
	public void testParentModifiedByChildNodeAttributeChange() {
		MockNode parent = new MockNode("parent");
		MockNode child = new MockNode("child");
		NodeWatcher parentHandler = new NodeWatcher();
		NodeWatcher childHandler = new NodeWatcher();
		parent.addNodeListener( parentHandler );
		child.addNodeListener( childHandler );

		assertThat( parent, hasStates( false, 0 ) );
		assertThat( child, hasStates( false, 0 ) );

		parent.setValue( "child", child );
		assertThat( parent, hasStates( true, 1 ) );
		assertThat( parentHandler, hasEventCounts( 1, 1, 1 ) );
		assertThat( childHandler, hasEventCounts( 0, 0, 0 ) );

		parent.setModified( false );
		assertThat( parent, hasStates( false, 0 ) );
		assertThat( parentHandler, hasEventCounts( 2, 2, 1 ) );

		// Test setting a value on the child node modifies the parent
		System.out.println( "--start" );
		child.setValue( "attribute", "value" );
		assertThat( child.getParent(), is( parent ) );
		System.out.println( "--stop" );

		assertThat( child, hasStates( true, 1 ) );
		assertThat( parent, hasStates( true, 1 ) );
		assertThat( parentHandler, hasEventCounts( 3, 3, 2 ) );
//		assertThat( childHandler, hasEventCounts( 1, 1, 1 ) );
//
//		// Test unsetting the value on the child node unmodifies the parent
//		child.setValue( "attribute", null );
//		assertThat( child, hasStates( false, 0 ) );
//		assertThat( parent, hasStates( false, 0 ) );
//		assertThat( parentHandler, hasEventCounts( 4, 4, 3 ) );
//		assertThat( childHandler, hasEventCounts( 2, 2, 2 ) );
	}

	//	public void testParentModifiedByChildNodeAttributeRippleChange() {
	//		MockDataNode parent = new MockDataNode();
	//		MockDataNode child = new MockDataNode();
	//		DataEventWatcher parentHandler = parent.getDataEventWatcher();
	//		DataEventWatcher childHandler = child.getDataEventWatcher();
	//		assertNodeState( parent, false, 0 );
	//
	//		parent.setValue( "child", child );
	//		assertEquals( child.getParent(), parent );
	//		assertNodeState( parent, true, 1 );
	//		assertEventCounts( parentHandler, 1, 1, 1 );
	//		assertEventCounts( childHandler, 0, 0, 0 );
	//		parentHandler.reset();
	//
	//		parent.setModified( false );
	//		assertNodeState( parent, false, 0 );
	//		parentHandler.reset();
	//		childHandler.reset();
	//
	//		// Set the 'a' attribute to '1'.
	//		child.setValue( "a", "1" );
	//		assertEventCounts( parentHandler, 1, 1, 1 );
	//		assertEventCounts( childHandler, 1, 1, 1 );
	//		parentHandler.reset();
	//		childHandler.reset();
	//
	//		// Set the 'b' attribute to '1';
	//		child.setValue( "b", "1" );
	//		assertEventCounts( parentHandler, 1, 0, 1 );
	//		assertEventCounts( childHandler, 1, 0, 1 );
	//		parentHandler.reset();
	//		childHandler.reset();
	//
	//		// Set this state as the new unmodified state.
	//		child.setModified( false );
	//		assertNodeState( parent, false, 0 );
	//		assertNodeState( child, false, 0 );
	//		assertEventCounts( parentHandler, 1, 1, 0 );
	//		assertEventCounts( childHandler, 1, 1, 0 );
	//
	//		// The parent is already not modified no event should be sent.
	//		parent.setModified( false );
	//		assertNodeState( parent, false, 0 );
	//		assertNodeState( child, false, 0 );
	//		assertEventCounts( parentHandler, 1, 1, 0 );
	//		assertEventCounts( childHandler, 1, 1, 0 );
	//		parentHandler.reset();
	//		childHandler.reset();
	//
	//		// Test setting 'a' attribute on the child node modifies the parent.
	//		child.setValue( "a", "2" );
	//		assertNodeState( child, true, 1 );
	//		assertNodeState( parent, true, 1 );
	//		assertEventCounts( parentHandler, 1, 1, 1 );
	//		assertEventCounts( childHandler, 1, 1, 1 );
	//		parentHandler.reset();
	//		childHandler.reset();
	//
	//		// Test setting the 'b' attribute on the child leaves the parent modified.
	//		child.setValue( "b", "2" );
	//		assertNodeState( child, true, 2 );
	//		assertNodeState( parent, true, 1 );
	//		assertEventCounts( parentHandler, 1, 0, 1 );
	//		assertEventCounts( childHandler, 1, 0, 1 );
	//		parentHandler.reset();
	//		childHandler.reset();
	//
	//		// Test unsetting 'a' attribute on the child leaves the parent modified.
	//		child.setValue( "a", "1" );
	//		assertNodeState( child, true, 1 );
	//		assertNodeState( parent, true, 1 );
	//		assertEventCounts( parentHandler, 1, 0, 1 );
	//		assertEventCounts( childHandler, 1, 0, 1 );
	//		parentHandler.reset();
	//		childHandler.reset();
	//
	//		// Test unsetting the 'b' attribute on the child returns the parent to unmodified.
	//		child.setValue( "b", "1" );
	//		assertNodeState( child, false, 0 );
	//		assertNodeState( parent, false, 0 );
	//		assertEventCounts( parentHandler, 1, 1, 1 );
	//		assertEventCounts( childHandler, 1, 1, 1 );
	//	}
	//
	//	public void testParentModifiedByNodeAttributeClearModified() {
	//		MockDataNode node = new MockDataNode();
	//		MockDataNode attribute = new MockDataNode();
	//		DataEventWatcher nodeHandler = node.getDataEventWatcher();
	//		DataEventWatcher attributeHandler = attribute.getDataEventWatcher();
	//		assertNodeState( node, false, 0 );
	//		assertEventCounts( nodeHandler, 0, 0, 0 );
	//		assertEventCounts( attributeHandler, 0, 0, 0 );
	//
	//		node.setValue( "attribute", attribute );
	//		assertNodeState( node, true, 1 );
	//		assertEventCounts( nodeHandler, 1, 1, 1 );
	//		assertEventCounts( attributeHandler, 0, 0, 0 );
	//
	//		node.setModified( false );
	//		assertNodeState( node, false, 0 );
	//		assertEventCounts( nodeHandler, 2, 2, 1 );
	//
	//		// Test setting an attribute on the attribute node modifies the parent.
	//		attribute.setValue( "attribute", "value" );
	//		assertEquals( attribute.getParent(), node );
	//
	//		assertNodeState( attribute, true, 1 );
	//		assertNodeState( node, true, 1 );
	//		assertEventCounts( nodeHandler, 3, 3, 2 );
	//		assertEventCounts( attributeHandler, 1, 1, 1 );
	//
	//		// Test unsetting an attribute on the attribute node unmodified the parent.
	//		attribute.setModified( false );
	//		assertNodeState( attribute, false, 0 );
	//		assertNodeState( node, false, 0 );
	//		assertEventCounts( nodeHandler, 4, 4, 2 );
	//		assertEventCounts( attributeHandler, 2, 2, 1 );
	//	}
	//
	//	public void testChildNodeAttributesClearedByParentClearModified() {
	//		MockDataNode child = new MockDataNode( "child" );
	//		MockDataNode parent = new MockDataNode( "parent" );
	//		DataEventWatcher childHandler = child.getDataEventWatcher();
	//		DataEventWatcher parentHandler = parent.getDataEventWatcher();
	//		assertNodeState( child, false, 0 );
	//		assertEventCounts( childHandler, 0, 0, 0 );
	//		assertNodeState( parent, false, 0 );
	//		assertEventCounts( parentHandler, 0, 0, 0 );
	//
	//		parent.setValue( "child", child );
	//		assertNodeState( parent, true, 1 );
	//		assertEventCounts( parentHandler, 1, 1, 1 );
	//
	//		Log.setLevel( Log.DEBUG );
	//		parent.setModified( false );
	//		Log.setLevel(Log.NONE );
	//		assertNodeState( parent, false, 0 );
	//		assertEventCounts( parentHandler, 2, 2, 1 );
	//
	//		child.setValue( "attribute", "value" );
	//		assertNodeState( child, true, 1 );
	//		assertEventCounts( childHandler, 1, 1, 1 );
	//		assertNodeState( parent, true, 1 );
	//		assertEventCounts( parentHandler, 3, 3, 2 );
	//
	//		parent.setModified( false );
	//		assertNodeState( child, false, 0 );
	//		assertEventCounts( childHandler, 2, 2, 1 );
	//		assertNodeState( parent, false, 0 );
	//		assertEventCounts( parentHandler, 4, 4, 2 );
	//	}
	//
	//	public void testAddNodeAttributeToDifferentParent() {
	//		MockDataNode child = new MockDataNode();
	//		MockDataNode parent0 = new MockDataNode();
	//		MockDataNode parent1 = new MockDataNode();
	//		DataEventWatcher childHandler = child.getDataEventWatcher();
	//		DataEventWatcher parent0Handler = parent0.getDataEventWatcher();
	//		DataEventWatcher parent1Handler = parent1.getDataEventWatcher();
	//
	//		// Add the child attribute to parent 0.
	//		parent0.setValue( "child", child );
	//		assertNodeState( parent0, true, 1 );
	//		assertEventCounts( childHandler, 0, 0, 0 );
	//		assertEventCounts( parent0Handler, 1, 1, 1 );
	//		assertEventCounts( parent1Handler, 0, 0, 0 );
	//
	//		// Clear the modified flag of parent 0.
	//		parent0.setModified( false );
	//		assertNodeState( parent0, false, 0 );
	//		assertEventCounts( childHandler, 0, 0, 0 );
	//		assertEventCounts( parent0Handler, 2, 2, 1 );
	//		assertEventCounts( parent1Handler, 0, 0, 0 );
	//
	//		// Add the child attribute to parent 1.
	//		parent1.setValue( "child", child );
	//		assertEquals( child, parent0.getAttribute( "child" ) );
	//		assertEquals( child, parent1.getAttribute( "child" ) );
	//		assertNodeState( parent0, false, 0 );
	//		assertNodeState( parent1, true, 1 );
	//		assertEventCounts( childHandler, 0, 0, 0 );
	//		assertEventCounts( parent0Handler, 2, 2, 1 );
	//		assertEventCounts( parent1Handler, 1, 1, 1 );
	//	}

	@Test
	public void testAddDataListener() throws Exception {
		NodeWatcher watcher = new NodeWatcher();
		data.addNodeListener( watcher );

		Collection<NodeListener> listeners = data.getNodeListeners();

		Assert.assertNotNull( listeners );
		assertThat( listeners.size(), is( 1 ) );
		assertThat( listeners, contains( watcher ) );
	}

	@Test
	public void testRemoveDataListener() throws Exception {
		Collection<NodeListener> listeners;
		NodeWatcher watcher = new NodeWatcher();

		data.addNodeListener( watcher );
		listeners = data.getNodeListeners();
		Assert.assertNotNull( listeners );
		assertThat( listeners.size(), is( 1 ) );
		assertThat( listeners, contains( watcher ) );

		data.removeNodeListener( watcher );
		listeners = data.getNodeListeners();
		Assert.assertNotNull( listeners );
		assertThat( listeners.size(), is( 0 ) );
		assertThat( listeners, not( contains( watcher ) ) );

		data.addNodeListener( watcher );
		listeners = data.getNodeListeners();
		Assert.assertNotNull( listeners );
		assertThat( listeners.size(), is( 1 ) );
		assertThat( listeners, contains( watcher ) );

		data.removeNodeListener( watcher );
		listeners = data.getNodeListeners();
		Assert.assertNotNull( listeners );
		assertThat( listeners.size(), is( 0 ) );
		assertThat( listeners, not( contains( watcher ) ) );
	}

	@Test
	public void testCircularReferenceCheck() {
		MockNode node = new MockNode();
		try {
			node.setValue( "node", node );
			Assert.fail( "CircularReferenceException should be thrown" );
		} catch( CircularReferenceException exception ) {
			// Intentionally ignore exception.
			assertThat( exception.getMessage(), startsWith( "Circular reference detected" ) );
		}
	}

	@Test
	public void testToString() {
		data.defineBusinessKey( "firstName", "lastName", "birthDate" );
		assertThat( data.toString(), is( "MockNode[]" ) );

		data.setValue( "firstName", "Jane" );
		data.setValue( "birthDate", new Date( 0 ) );
		assertThat( data.toString(), is( "MockNode[firstName=Jane,birthDate=Wed Dec 31 17:00:00 MST 1969]" ) );

		data.setValue( "lastName", "Doe" );
		assertThat( data.toString(), is( "MockNode[firstName=Jane,lastName=Doe,birthDate=Wed Dec 31 17:00:00 MST 1969]" ) );
	}

	@Test
	public void testToStringWithAllValues() {
		assertThat( data.toString( true ), is( "MockNode[]" ) );

		data.setValue( "firstName", "Jane" );
		data.setValue( "lastName", "Doe" );
		assertThat( data.toString( true ), is( "MockNode[firstName=Jane,lastName=Doe]" ) );
	}

	@Test
	public void testHashCode() {
		data.defineBusinessKey( "firstName", "lastName", "birthDate" );
		assertThat( data.hashCode(), is( 0 ) );

		// Test the primary key
		data.setValue( "id", 2849234 );
		assertThat( data.hashCode(), is( 2849234 ) );

		// Test the business key
		data.setValue( "lastName", "Doe" );
		assertThat( data.hashCode(), is( 2782408 ) );
	}

	@Test
	public void testEquals() {
		MockNode data1 = new MockNode();
		data1.defineBusinessKey( "firstName", "lastName", "birthDate" );
		MockNode data2 = new MockNode();
		data2.defineBusinessKey( "firstName", "lastName", "birthDate" );
		assertThat( data1.equals( data2 ), is( true ) );

		// Test the primary key
		data1.setValue( "id", 2849234 );
		assertThat( data1.equals( data2 ), is( false ) );

		data2.setValue( "id", 2849234 );
		assertThat( data1.equals( data2 ), is( true ) );

		// Test the business key
		data1.setValue( "lastName", "Doe" );
		assertThat( data1.equals( data2 ), is( false ) );

		data2.setValue( "lastName", "Doe" );
		assertThat( data1.equals( data2 ), is( true ) );
	}

	//	@Test
	//	public void testLink() {
	//		Node target = new Node();
	//		Edge edge = data.add( target );
	//
	//		assertThat( edge.isDirected(), is( false ) );
	//		assertThat( data.getLinks(), contains( edge ) );
	//	}
	//
	//	@Test
	//	public void testUnlink() {
	//		Node target = new Node();
	//		Edge edge = data.add( target );
	//
	//		assertThat( edge.isDirected(), is( false ) );
	//		assertThat( data.getLinks(), contains( edge ) );
	//
	//		data.remove( edge.getTarget() );
	//
	//		assertThat( data.getLinks(), not( contains( edge ) ) );
	//	}

	private void listEvents( NodeWatcher watcher ) {
		for( NodeEvent event : watcher.getEvents() ) {
			System.out.println( "Event: " + event );
		}
	}

	private static Matcher<Node> hasStates( boolean modified, int modifiedValueCount ) {
		Matcher<Node> modifiedMatcher = modifiedFlag(  is( modified ) );
		Matcher<Node> modifiedValueCountMatcher = modifiedValueCount( is( modifiedValueCount ) );
		return allOf( modifiedMatcher, modifiedValueCountMatcher );
	}

	private static Matcher<Node> flag( String key, Matcher<? super Boolean> matcher ) {
		return new FeatureMatcher<Node, Boolean>( matcher, key, key ) {

			@Override
			protected Boolean featureValueOf( Node node ) {
				return node.getFlag( key );
			}

		};
	}

	private static Matcher<Node> modifiedFlag( Matcher<? super Boolean> matcher ) {
		return new FeatureMatcher<Node, Boolean>( matcher, "modified", "modified" ) {

			@Override
			protected Boolean featureValueOf( Node node ) {
				return node.isModified();
			}

		};
	}

	private static Matcher<Node> modifiedValueCount( Matcher<? super Integer> matcher ) {
		return new FeatureMatcher<Node, Integer>( matcher, "modified value count", "count" ) {

			@Override
			protected Integer featureValueOf( Node node ) {
				return node.getModifiedValueCount();
			}

		};
	}

	private static Matcher<NodeWatcher> hasEventCounts( int node, int flag, int value ) {
		Matcher<NodeWatcher> nodeEventCounter = eventCount( NodeEvent.Type.NODE_CHANGED, is( node ) );
		Matcher<NodeWatcher> valueEventCounter = eventCount( NodeEvent.Type.VALUE_CHANGED, is( value ) );
		Matcher<NodeWatcher> flagEventCounter = eventCount( NodeEvent.Type.FLAG_CHANGED, is( flag ) );
		return allOf( nodeEventCounter, flagEventCounter, valueEventCounter );
	}

	private static Matcher<NodeWatcher> eventCount( NodeEvent.Type type, Matcher<? super Integer> matcher ) {
		return new FeatureMatcher<NodeWatcher, Integer>( matcher, type + " event count", "count" ) {

			@Override
			protected Integer featureValueOf( NodeWatcher watcher ) {
				int count = 0;
				for( NodeEvent event : watcher.getEvents() ) {
					if( event.getType() == type ) count++;
				}

				return count;
			}

		};
	}

	private static void assertEventState( NodeWatcher watcher, int index, NodeEvent.Type type, Node node ) {
		assertThat( watcher.getEvents().get( index ), hasEventState( node, type ) );
	}

	private static void assertEventState( NodeWatcher watcher, int index, NodeEvent.Type type, Node node, String key, Object oldValue, Object newValue ) {
		assertThat( watcher.getEvents().get( index ), hasEventState( node, type, key, oldValue, newValue ) );
	}

	private static void assertEventState( NodeWatcher watcher, int index, NodeEvent.Type type, Node parent, Node child, String key, Object oldValue, Object newValue ) {
		assertThat( watcher.getEvents().get( index ), hasEventState( parent, child, type, key, oldValue, newValue ) );
	}

	private static Matcher<NodeEvent> hasEventState( Node node, NodeEvent.Type type ) {
		Matcher<NodeEvent> eventNode = eventNode( is( node ) );
		Matcher<NodeEvent> eventType = eventType( is( type ) );
		return allOf( eventNode, eventType );
	}

	private static Matcher<NodeEvent> hasEventState( Node node, NodeEvent.Type type, String key, Object oldValue, Object newValue ) {
		Matcher<NodeEvent> eventNode = eventNode( is( node ) );
		Matcher<NodeEvent> eventType = eventType( is( type ) );
		Matcher<NodeEvent> eventKey = eventKey( is( key ) );
		Matcher<NodeEvent> eventOldValue = eventOldValue( is( oldValue ) );
		Matcher<NodeEvent> eventNewValue = eventNewValue( is( newValue ) );
		return allOf( eventNode, eventType, eventKey, eventOldValue, eventNewValue );
	}

	private static Matcher<NodeEvent> hasEventState( Node node, Node child, NodeEvent.Type type, String key, Object oldValue, Object newValue ) {
		Matcher<NodeEvent> eventNode = eventNode( is( node ) );
		Matcher<NodeEvent> eventChild = eventChild( is( child ) );
		Matcher<NodeEvent> eventType = eventType( is( type ) );
		Matcher<NodeEvent> eventKey = eventKey( is( key ) );
		Matcher<NodeEvent> eventOldValue = eventOldValue( is( oldValue ) );
		Matcher<NodeEvent> eventNewValue = eventNewValue( is( newValue ) );
		return allOf( eventNode, eventType, eventKey, eventOldValue, eventNewValue );
	}

	private static Matcher<NodeEvent> eventNode( Matcher<? super Node> matcher ) {
		return new FeatureMatcher<NodeEvent, Node>( matcher, "node", "node" ) {

			@Override
			protected Node featureValueOf( NodeEvent event ) {
				return event.getNode();
			}

		};
	}

	private static Matcher<NodeEvent> eventChild( Matcher<? super Node> matcher ) {
		return new FeatureMatcher<NodeEvent, Node>( matcher, "node", "node" ) {

			@Override
			protected Node featureValueOf( NodeEvent event ) {
				return event.getChild();
			}

		};
	}

	private static Matcher<NodeEvent> eventType( Matcher<? super NodeEvent.Type> matcher ) {
		return new FeatureMatcher<NodeEvent, NodeEvent.Type>( matcher, "type", "type" ) {

			@Override
			protected NodeEvent.Type featureValueOf( NodeEvent event ) {
				return event.getType();
			}

		};
	}

	private static Matcher<NodeEvent> eventKey( Matcher<? super String> matcher ) {
		return new FeatureMatcher<NodeEvent, String>( matcher, "key", "key" ) {

			@Override
			protected String featureValueOf( NodeEvent event ) {
				return event.getKey();
			}

		};
	}

	private static Matcher<NodeEvent> eventOldValue( Matcher<? super Object> matcher ) {
		return new FeatureMatcher<NodeEvent, Object>( matcher, "oldValue", "oldValue" ) {

			@Override
			protected Object featureValueOf( NodeEvent event ) {
				return event.getOldValue();
			}

		};
	}

	private static Matcher<NodeEvent> eventNewValue( Matcher<? super Object> matcher ) {
		return new FeatureMatcher<NodeEvent, Object>( matcher, "newValue", "newValue" ) {

			@Override
			protected Object featureValueOf( NodeEvent event ) {
				return event.getNewValue();
			}

		};
	}

}
