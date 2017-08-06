package com.xeomar.xenon.node;

import com.xeomar.xenon.transaction.Txn;
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
		assertThat( data, hasStates( false, 0, 0 ) );
		assertThat( watcher, hasEventCounts( 0, 0, 0 ) );

		data.setValue( "id", 423984 );
		assertThat( data, hasStates( true, 1, 0 ) );
		assertThat( watcher, hasEventCounts( 1, 1, 1 ) );

		data.setModified( false );
		assertThat( data, hasStates( false, 0, 0 ) );
		assertThat( watcher, hasEventCounts( 2, 2, 1 ) );
	}

	@Test
	public void testModifyByValueAndUnmodifyByValue() {
		NodeWatcher watcher = new NodeWatcher();
		data.addNodeListener( watcher );
		assertThat( data, hasStates( false, 0, 0 ) );
		assertThat( watcher, hasEventCounts( 0, 0, 0 ) );

		data.setValue( "id", 423984 );
		assertThat( data, hasStates( true, 1, 0 ) );
		assertThat( watcher, hasEventCounts( 1, 1, 1 ) );

		data.setValue( "id", null );
		assertThat( data, hasStates( false, 0, 0 ) );
		assertThat( watcher, hasEventCounts( 2, 2, 2 ) );
	}

	@Test
	public void testGetValueKeys() {
		data.setValue( "key", "value" );
		assertThat( data.getValueKeys(), containsInAnyOrder( "key" ) );
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
		assertThat( data, hasStates( false, 0, 0 ) );
		assertThat( watcher, hasEventCounts( 0, 0, 0 ) );

		// Set the value to null and make sure nothing happens
		data.setValue( "attribute", null );
		assertThat( data.getValue( "attribute" ), is( nullValue() ) );
		assertThat( data, hasStates( false, 0, 0 ) );
		assertThat( watcher, hasEventCounts( 0, 0, 0 ) );

		// Set the value
		data.setValue( "attribute", "value" );
		assertThat( data.getValue( "attribute" ), is( "value" ) );
		assertThat( data, hasStates( true, 1, 0 ) );
		assertThat( watcher, hasEventCounts( 1, 1, 1 ) );

		// Set the value to the same value and make sure nothing happens
		data.setValue( "attribute", "value" );
		assertThat( data.getValue( "attribute" ), is( "value" ) );
		assertThat( data, hasStates( true, 1, 0 ) );
		assertThat( watcher, hasEventCounts( 1, 1, 1 ) );

		// Set the value back to null
		data.setValue( "attribute", null );
		assertThat( data.getValue( "attribute" ), is( nullValue() ) );
		assertThat( data, hasStates( false, 0, 0 ) );
		assertThat( watcher, hasEventCounts( 2, 2, 2 ) );

		// Set the value to null again and make sure nothing happens
		data.setValue( "attribute", null );
		assertThat( data.getValue( "attribute" ), is( nullValue() ) );
		assertThat( data, hasStates( false, 0, 0 ) );
		assertThat( watcher, hasEventCounts( 2, 2, 2 ) );
	}

	@Test
	public void testGetAndSetValue() {
		NodeWatcher watcher = new NodeWatcher();
		data.addNodeListener( watcher );

		assertThat( data.getValue( "x" ), is( nullValue() ) );
		assertThat( data.getValue( "y" ), is( nullValue() ) );
		assertThat( data.getValue( "z" ), is( nullValue() ) );
		assertThat( data, hasStates( false, 0, 0 ) );
		assertThat( watcher, hasEventCounts( 0, 0, 0 ) );

		data.setValue( "x", 1.0 );
		assertThat( data.getValue( "x" ), is( 1.0 ) );
		assertThat( data.getValue( "y" ), is( nullValue() ) );
		assertThat( data.getValue( "z" ), is( nullValue() ) );
		assertThat( data, hasStates( true, 1, 0 ) );
		assertThat( watcher, hasEventCounts( 1, 1, 1 ) );

		data.setValue( "x", 0.0 );
		assertThat( data.getValue( "x" ), is( 0.0 ) );
		assertThat( data.getValue( "y" ), is( nullValue() ) );
		assertThat( data.getValue( "z" ), is( nullValue() ) );
		assertThat( data, hasStates( true, 1, 0 ) );
		assertThat( watcher, hasEventCounts( 2, 1, 2 ) );
	}

	@Test
	public void testModifiedBySetAttribute() {
		NodeWatcher watcher = new NodeWatcher();
		data.addNodeListener( watcher );

		assertThat( data, hasStates( false, 0, 0 ) );
		assertThat( watcher, hasEventCounts( 0, 0, 0 ) );

		data.setValue( "x", 1 );
		data.setValue( "y", 2 );
		data.setValue( "z", 3 );
		assertThat( data, hasStates( true, 3, 0 ) );
		assertThat( watcher, hasEventCounts( 3, 1, 3 ) );
	}

	@Test
	public void testUnmodifiedByUnsetValue() {
		NodeWatcher watcher = new NodeWatcher();
		data.addNodeListener( watcher );

		assertThat( data, hasStates( false, 0, 0 ) );
		assertThat( watcher, hasEventCounts( 0, 0, 0 ) );

		data.setValue( "x", 0 );
		data.setValue( "y", 0 );
		data.setValue( "z", 0 );
		data.setModified( false );
		assertThat( data, hasStates( false, 0, 0 ) );
		assertThat( watcher, hasEventCounts( 4, 2, 3 ) );

		data.setValue( "x", 1 );
		data.setValue( "y", 2 );
		data.setValue( "z", 3 );
		assertThat( data, hasStates( true, 3, 0 ) );
		assertThat( watcher, hasEventCounts( 7, 3, 6 ) );

		data.setValue( "x", 0 );
		data.setValue( "y", 0 );
		data.setValue( "z", 0 );
		assertThat( data, hasStates( false, 0, 0 ) );
		assertThat( watcher, hasEventCounts( 10, 4, 9 ) );
	}

	@Test
	public void testModifiedAttributeCountResetByCommit() {
		NodeWatcher watcher = new NodeWatcher();
		data.addNodeListener( watcher );

		assertThat( data, hasStates( false, 0, 0 ) );
		assertThat( watcher, hasEventCounts( 0, 0, 0 ) );

		data.setValue( "x", 1 );
		data.setValue( "y", 2 );
		data.setValue( "z", 3 );
		assertThat( data, hasStates( true, 3, 0 ) );
		assertThat( watcher, hasEventCounts( 3, 1, 3 ) );

		data.setModified( false );
		assertThat( data, hasStates( false, 0, 0 ) );
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
	public void testResourceKeys() {
		data.putResource( "key", "value" );
		assertThat( data.getResourceKeys(), containsInAnyOrder( "key" ) );
	}

	@Test
	public void testResources() {
		NodeWatcher watcher = new NodeWatcher();
		data.addNodeListener( watcher );

		assertThat( data, hasStates( false, 0, 0 ) );
		assertThat( watcher, hasEventCounts( 0, 0, 0 ) );

		data.putResource( "name", "value" );
		assertThat( data.getResource( "name" ), is( "value" ) );
		assertThat( data, hasStates( false, 0, 0 ) );
		assertThat( watcher, hasEventCounts( 0, 0, 0 ) );

		data.putResource( "name", null );
		assertThat( data.getResource( "name" ), is( nullValue() ) );
		assertThat( data, hasStates( false, 0, 0 ) );
		assertThat( watcher, hasEventCounts( 0, 0, 0 ) );
	}

	@Test
	public void testDataEventNotification() {
		NodeWatcher watcher = new NodeWatcher();
		data.addNodeListener( watcher );

		assertThat( data, hasStates( false, 0, 0 ) );
		assertThat( watcher, hasEventCounts( 0, 0, 0 ) );

		// Set a value
		data.setValue( "attribute", "value0" );
		assertThat( data, hasStates( true, 1, 0 ) );
		assertThat( watcher, hasEventCounts( 1, 1, 1 ) );

		// Set the value to the same value
		data.setValue( "attribute", "value0" );
		assertThat( data, hasStates( true, 1, 0 ) );
		assertThat( watcher, hasEventCounts( 1, 1, 1 ) );

		// Modify the value
		data.setValue( "attribute", "value1" );
		assertThat( data, hasStates( true, 1, 0 ) );
		assertThat( watcher, hasEventCounts( 2, 1, 2 ) );

		// Remove the attribute.
		data.setValue( "attribute", null );
		assertThat( data, hasStates( false, 0, 0 ) );
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

		assertThat( data, hasStates( false, 0, 0 ) );
		assertThat( watcher, hasEventCounts( 0, 0, 0 ) );

		// Change a value
		data.setValue( "attribute", "value0" );
		assertThat( data, hasStates( true, 1, 0 ) );
		assertThat( watcher, hasEventCounts( 1, 1, 1 ) );

		// Set the modified flag to false
		data.setModified( false );
		assertThat( data, hasStates( false, 0, 0 ) );
		assertThat( watcher, hasEventCounts( 2, 2, 1 ) );

		// Set the modified flag to false again
		data.setModified( false );
		assertThat( data, hasStates( false, 0, 0 ) );
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

		assertThat( data, hasStates( false, 0, 0 ) );
		assertThat( watcher, hasEventCounts( 0, 0, 0 ) );

		Txn.create();
		data.setValue( "a", "1" );
		data.setValue( "a", "2" );
		data.setValue( "a", "3" );
		data.setValue( "a", "4" );
		data.setValue( "a", "5" );
		Txn.commit();

		assertThat( data.getValue( "a" ), is( "5" ) );
		assertThat( data, hasStates( true, 1, 0 ) );
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

	@Test
	public void testParentDataEventNotification() {
		MockNode parent = new MockNode( "parent" );
		MockNode child = new MockNode( "child" );

		NodeWatcher parentWatcher = new NodeWatcher();
		parent.addNodeListener( parentWatcher );

		// Add the child to the parent
		parent.setValue( "child", child );
		assertThat( child, hasStates( false, 0, 0 ) );
		assertThat( parentWatcher, hasEventCounts( 1, 1, 1 ) );

		// Set a value on the child
		child.setValue( "attribute", "value0" );
		assertThat( child, hasStates( true, 1, 0 ) );
		assertThat( parentWatcher, hasEventCounts( 2, 1, 2 ) );

		// Set the value to the same value. Should do nothing.
		child.setValue( "attribute", "value0" );
		assertThat( child, hasStates( true, 1, 0 ) );
		assertThat( parentWatcher, hasEventCounts( 2, 1, 2 ) );

		// Modify the attribute.
		child.setValue( "attribute", "value1" );
		assertThat( child, hasStates( true, 1, 0 ) );
		assertThat( parentWatcher, hasEventCounts( 3, 1, 3 ) );

		// Remove the attribute.
		child.setValue( "attribute", null );
		assertThat( child, hasStates( false, 0, 0 ) );
		assertThat( parentWatcher, hasEventCounts( 4, 1, 4 ) );

		int index = 0;
		assertEventState( parentWatcher, index++, NodeEvent.Type.VALUE_CHANGED, parent, "child", null, child );
		assertEventState( parentWatcher, index++, NodeEvent.Type.FLAG_CHANGED, parent, Node.MODIFIED, false, true );
		assertEventState( parentWatcher, index++, NodeEvent.Type.NODE_CHANGED, parent );

		assertEventState( parentWatcher, index++, NodeEvent.Type.VALUE_CHANGED, parent, child, "attribute", null, "value0" );
		assertEventState( parentWatcher, index++, NodeEvent.Type.NODE_CHANGED, parent );

		assertEventState( parentWatcher, index++, NodeEvent.Type.VALUE_CHANGED, parent, child, "attribute", "value0", "value1" );
		assertEventState( parentWatcher, index++, NodeEvent.Type.NODE_CHANGED, parent );

		assertEventState( parentWatcher, index++, NodeEvent.Type.VALUE_CHANGED, parent, child, "attribute", "value1", null );
		assertEventState( parentWatcher, index++, NodeEvent.Type.NODE_CHANGED, parent );
		assertThat( parentWatcher.getEvents().size(), is( index ) );
	}

	@Test
	public void testParentModifiedByChildNodeAttributeChange() {
		MockNode parent = new MockNode( "parent" );
		MockNode child = new MockNode( "child" );
		NodeWatcher parentWatcher = new NodeWatcher();
		NodeWatcher childWatcher = new NodeWatcher();
		parent.addNodeListener( parentWatcher );
		child.addNodeListener( childWatcher );

		assertThat( parent, hasStates( false, 0, 0 ) );
		assertThat( child, hasStates( false, 0, 0 ) );

		parent.setValue( "child", child );
		assertThat( child.getParent(), is( parent ) );
		assertThat( parent, hasStates( true, 1, 0 ) );
		assertThat( child, hasStates( false, 0, 0 ) );
		assertThat( parentWatcher, hasEventCounts( 1, 1, 1 ) );
		assertThat( childWatcher, hasEventCounts( 0, 0, 0 ) );

		parent.setModified( false );
		assertThat( parent, hasStates( false, 0, 0 ) );
		assertThat( child, hasStates( false, 0, 0 ) );
		assertThat( parentWatcher, hasEventCounts( 2, 2, 1 ) );
		assertThat( childWatcher, hasEventCounts( 0, 0, 0 ) );

		// Test setting a value on the child node modifies the parent
		child.setValue( "attribute", "value" );
		assertThat( child, hasStates( true, 1, 0 ) );
		assertThat( parent, hasStates( true, 0, 1 ) );
		assertThat( parentWatcher, hasEventCounts( 3, 3, 2 ) );
		assertThat( childWatcher, hasEventCounts( 1, 1, 1 ) );

		// Test unsetting the value on the child node unmodifies the parent
		child.setValue( "attribute", null );
		assertThat( child, hasStates( false, 0, 0 ) );
		assertThat( parent, hasStates( false, 0, 0 ) );
		assertThat( parentWatcher, hasEventCounts( 4, 4, 3 ) );
		assertThat( childWatcher, hasEventCounts( 2, 2, 2 ) );
	}

	@Test
	public void testGrandparentModifiedByChildNodeAttributeChange() {
		MockNode grand = new MockNode( "grand" );
		MockNode parent = new MockNode( "parent" );
		MockNode child = new MockNode( "child" );
		NodeWatcher grandWatcher = new NodeWatcher();
		NodeWatcher parentWatcher = new NodeWatcher();
		NodeWatcher childWatcher = new NodeWatcher();
		grand.addNodeListener( grandWatcher );
		parent.addNodeListener( parentWatcher );
		child.addNodeListener( childWatcher );

		assertThat( grand, hasStates( false, 0, 0 ) );
		assertThat( parent, hasStates( false, 0, 0 ) );
		assertThat( child, hasStates( false, 0, 0 ) );

		grand.setValue( "child", parent );
		assertThat( parent.getParent(), is( grand ) );
		assertThat( grand, hasStates( true, 1, 0 ) );
		assertThat( parent, hasStates( false, 0, 0 ) );
		assertThat( child, hasStates( false, 0, 0 ) );
		assertThat( grandWatcher, hasEventCounts( 1, 1, 1 ) );
		assertThat( parentWatcher, hasEventCounts( 0, 0, 0 ) );
		assertThat( childWatcher, hasEventCounts( 0, 0, 0 ) );

		parent.setValue( "child", child );
		assertThat( child.getParent(), is( parent ) );
		assertThat( grand, hasStates( true, 1, 1 ) );
		assertThat( parent, hasStates( true, 1, 0 ) );
		assertThat( child, hasStates( false, 0, 0 ) );
		assertThat( grandWatcher, hasEventCounts( 2, 1, 2 ) );
		assertThat( parentWatcher, hasEventCounts( 1, 1, 1 ) );
		assertThat( childWatcher, hasEventCounts( 0, 0, 0 ) );

		grand.setModified( false );
		assertThat( grand, hasStates( false, 0, 0 ) );
		assertThat( parent, hasStates( false, 0, 0 ) );
		assertThat( child, hasStates( false, 0, 0 ) );
		assertThat( grandWatcher, hasEventCounts( 3, 2, 2 ) );
		assertThat( parentWatcher, hasEventCounts( 2, 2, 1 ) );
		assertThat( childWatcher, hasEventCounts( 0, 0, 0 ) );

		// Test setting a value on the child node modifies the parents
		child.setValue( "attribute", "value" );
		assertThat( grand, hasStates( true, 0, 1 ) );
		assertThat( parent, hasStates( true, 0, 1 ) );
		assertThat( child, hasStates( true, 1, 0 ) );
		assertThat( grandWatcher, hasEventCounts( 4, 3, 2 ) );
		assertThat( parentWatcher, hasEventCounts( 3, 3, 2 ) );
		assertThat( childWatcher, hasEventCounts( 1, 1, 1 ) );

		// Test unsetting the value on the child node unmodifies the parents
		child.setValue( "attribute", null );
		assertThat( grand, hasStates( false, 0, 0 ) );
		assertThat( child, hasStates( false, 0, 0 ) );
		assertThat( parent, hasStates( false, 0, 0 ) );
		assertThat( grandWatcher, hasEventCounts( 5, 4, 2 ) );
		assertThat( parentWatcher, hasEventCounts( 4, 4, 3 ) );
		assertThat( childWatcher, hasEventCounts( 2, 2, 2 ) );
	}

	@Test
	public void testParentModifiedByChildNodeClearedByFlag() {
		MockNode parent = new MockNode( "parent" );
		MockNode child = new MockNode( "child" );
		NodeWatcher parentWatcher = new NodeWatcher();
		NodeWatcher childWatcher = new NodeWatcher();
		parent.addNodeListener( parentWatcher );
		child.addNodeListener( childWatcher );
		assertThat( parent, hasStates( false, 0, 0 ) );
		assertThat( child, hasStates( false, 0, 0 ) );

		parent.setValue( "child", child );
		assertThat( child.getParent(), is( parent ) );
		assertThat( parent, hasStates( true, 1, 0 ) );
		assertThat( child, hasStates( false, 0, 0 ) );
		assertThat( parentWatcher, hasEventCounts( 1, 1, 1 ) );
		assertThat( childWatcher, hasEventCounts( 0, 0, 0 ) );
		parentWatcher.reset();
		childWatcher.reset();

		parent.setModified( false );
		assertThat( child, hasStates( false, 0, 0 ) );
		assertThat( parent, hasStates( false, 0, 0 ) );
		assertThat( parentWatcher, hasEventCounts( 1, 1, 0 ) );
		assertThat( childWatcher, hasEventCounts( 0, 0, 0 ) );
		parentWatcher.reset();
		childWatcher.reset();

		// Test setting the 'a' value on the child modifies the parent
		child.setValue( "a", "1" );
		assertThat( child, hasStates( true, 1, 0 ) );
		assertThat( parent, hasStates( true, 0, 1 ) );
		assertThat( parentWatcher, hasEventCounts( 1, 1, 1 ) );
		assertThat( childWatcher, hasEventCounts( 1, 1, 1 ) );
		parentWatcher.reset();
		childWatcher.reset();

		// Test setting the 'b' value on the child leaves the parent modified
		child.setValue( "b", "1" );
		assertThat( child, hasStates( true, 2, 0 ) );
		assertThat( parent, hasStates( true, 0, 1 ) );
		assertThat( parentWatcher, hasEventCounts( 1, 0, 1 ) );
		assertThat( childWatcher, hasEventCounts( 1, 0, 1 ) );
		parentWatcher.reset();
		childWatcher.reset();

		// Set this state as the new unmodified state
		child.setModified( false );
		assertThat( child, hasStates( false, 0, 0 ) );
		assertThat( parent, hasStates( false, 0, 0 ) );
		assertThat( parentWatcher, hasEventCounts( 1, 1, 0 ) );
		assertThat( childWatcher, hasEventCounts( 1, 1, 0 ) );
		parentWatcher.reset();
		childWatcher.reset();
	}

	@Test
	public void testParentModifiedByChildNodeClearedByValue() {
		MockNode parent = new MockNode( "parent" );
		MockNode child = new MockNode( "child" );
		NodeWatcher parentWatcher = new NodeWatcher();
		NodeWatcher childWatcher = new NodeWatcher();
		parent.addNodeListener( parentWatcher );
		child.addNodeListener( childWatcher );
		assertThat( parent, hasStates( false, 0, 0 ) );
		assertThat( child, hasStates( false, 0, 0 ) );

		parent.setValue( "child", child );
		assertThat( child.getParent(), is( parent ) );
		assertThat( parent, hasStates( true, 1, 0 ) );
		assertThat( child, hasStates( false, 0, 0 ) );
		assertThat( parentWatcher, hasEventCounts( 1, 1, 1 ) );
		assertThat( childWatcher, hasEventCounts( 0, 0, 0 ) );
		parentWatcher.reset();
		childWatcher.reset();

		parent.setModified( false );
		assertThat( child, hasStates( false, 0, 0 ) );
		assertThat( parent, hasStates( false, 0, 0 ) );
		assertThat( parentWatcher, hasEventCounts( 1, 1, 0 ) );
		assertThat( childWatcher, hasEventCounts( 0, 0, 0 ) );
		parentWatcher.reset();
		childWatcher.reset();

		// Test setting the 'a' value on the child modifies the parent
		child.setValue( "a", "2" );
		assertThat( child, hasStates( true, 1, 0 ) );
		assertThat( parent, hasStates( true, 0, 1 ) );
		assertThat( parentWatcher, hasEventCounts( 1, 1, 1 ) );
		assertThat( childWatcher, hasEventCounts( 1, 1, 1 ) );
		parentWatcher.reset();
		childWatcher.reset();

		// Test setting the 'b' value on the child leaves the parent modified
		child.setValue( "b", "2" );
		assertThat( child, hasStates( true, 2, 0 ) );
		assertThat( parent, hasStates( true, 0, 1 ) );
		assertThat( parentWatcher, hasEventCounts( 1, 0, 1 ) );
		assertThat( childWatcher, hasEventCounts( 1, 0, 1 ) );
		parentWatcher.reset();
		childWatcher.reset();

		// Test unsetting the 'a' value on the child leaves the parent modified
		child.setValue( "a", null );
		assertThat( child, hasStates( true, 1, 0 ) );
		assertThat( parent, hasStates( true, 0, 1 ) );
		assertThat( parentWatcher, hasEventCounts( 1, 0, 1 ) );
		assertThat( childWatcher, hasEventCounts( 1, 0, 1 ) );
		parentWatcher.reset();
		childWatcher.reset();

		// Test unsetting the value 'b' on the child returns the parent to unmodified
		child.setValue( "b", null );
		assertThat( child, hasStates( false, 0, 0 ) );
		assertThat( parent, hasStates( false, 0, 0 ) );
		assertThat( parentWatcher, hasEventCounts( 1, 1, 1 ) );
		assertThat( childWatcher, hasEventCounts( 1, 1, 1 ) );
	}

	@Test
	public void testAddNodeAttributeToDifferentParent() {
		MockNode parent0 = new MockNode( "parent0" );
		MockNode parent1 = new MockNode( "parent1" );
		MockNode child = new MockNode( "child" );
		NodeWatcher parent0Watcher = new NodeWatcher();
		NodeWatcher parent1Watcher = new NodeWatcher();
		NodeWatcher childWatcher = new NodeWatcher();
		parent0.addNodeListener( parent0Watcher );
		parent1.addNodeListener( parent1Watcher );
		child.addNodeListener( childWatcher );
		assertThat( parent0, hasStates( false, 0, 0 ) );
		assertThat( parent1, hasStates( false, 0, 0 ) );
		assertThat( child, hasStates( false, 0, 0 ) );
		assertThat( parent0Watcher, hasEventCounts( 0, 0, 0 ) );
		assertThat( parent1Watcher, hasEventCounts( 0, 0, 0 ) );
		assertThat( childWatcher, hasEventCounts( 0, 0, 0 ) );

		// Add the child attribute to parent 0
		parent0.setValue( "child", child );
		assertThat( parent0, hasStates( true, 1, 0 ) );
		assertThat( parent0Watcher, hasEventCounts( 1, 1, 1 ) );
		assertThat( parent1Watcher, hasEventCounts( 0, 0, 0 ) );
		assertThat( childWatcher, hasEventCounts( 0, 0, 0 ) );

		// Clear the modified flag of parent 0.
		parent0.setModified( false );
		assertThat( child.getParent(), is( parent0 ) );
		assertThat( parent0, hasStates( false, 0, 0 ) );
		assertThat( parent1, hasStates( false, 0, 0 ) );
		assertThat( child, hasStates( false, 0, 0 ) );
		assertThat( parent0Watcher, hasEventCounts( 2, 2, 1 ) );
		assertThat( parent1Watcher, hasEventCounts( 0, 0, 0 ) );
		assertThat( childWatcher, hasEventCounts( 0, 0, 0 ) );

		// Add the child attribute to parent 1.
		parent1.setValue( "child", child );
		assertThat( child.getParent(), is( parent1 ) );

		// Oddly both parents have the child as a value...
		assertThat( parent0.getValue( "child" ), is( child ) );
		assertThat( parent1.getValue( "child" ), is( child ) );
		assertThat( parent0, hasStates( false, 0, 0 ) );
		assertThat( parent1, hasStates( true, 1, 0 ) );
		assertThat( child, hasStates( false, 0, 0 ) );
		assertThat( parent0Watcher, hasEventCounts( 2, 2, 1 ) );
		assertThat( parent1Watcher, hasEventCounts( 1, 1, 1 ) );
		assertThat( childWatcher, hasEventCounts( 0, 0, 0 ) );
	}

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
	public void testCopyFrom() {
		Node node1 = new Node();
		node1.setValue( "key1", "value1" );
		node1.setValue( "key2", "value2" );

		Node node2 = new Node();
		node2.setValue( "key2", "valueB" );

		node2.copyFrom( node1 );
		assertThat( node1.getValue( "key1"), is( "value1" ));
		assertThat( node1.getValue( "key2"), is( "value2" ));
		assertThat( node2.getValue( "key1"), is( "value1" ));
		assertThat( node2.getValue( "key2"), is( "valueB" ));
	}

	@Test
	public void testCopyFromWithOverwrite() {
		Node node1 = new Node();
		node1.setValue( "key1", "value1" );
		node1.setValue( "key2", "value2" );

		Node node2 = new Node();
		node2.setValue( "key2", "valueB" );

		node2.copyFrom( node1, true );
		assertThat( node1.getValue( "key1"), is( "value1" ));
		assertThat( node1.getValue( "key2"), is( "value2" ));
		assertThat( node2.getValue( "key1"), is( "value1" ));
		assertThat( node2.getValue( "key2"), is( "value2" ));
	}

	@Test
	public void testCopyFromUsingResources() {
		Node node1 = new Node();
		node1.putResource( "key1", "value1" );
		node1.putResource( "key2", "value2" );

		Node node2 = new Node();
		node2.putResource( "key2", "valueB" );

		node2.copyFrom( node1 );
		assertThat( node1.getResource( "key1"), is( "value1" ));
		assertThat( node1.getResource( "key2"), is( "value2" ));
		assertThat( node2.getResource( "key1"), is( "value1" ));
		assertThat( node2.getResource( "key2"), is( "valueB" ));
	}

	@Test
	public void testCopyFromWithOverwriteUsingResources() {
		Node node1 = new Node();
		node1.putResource( "key1", "value1" );
		node1.putResource( "key2", "value2" );

		Node node2 = new Node();
		node2.putResource( "key2", "valueB" );

		node2.copyFrom( node1, true );
		assertThat( node1.getResource( "key1"), is( "value1" ));
		assertThat( node1.getResource( "key2"), is( "value2" ));
		assertThat( node2.getResource( "key1"), is( "value1" ));
		assertThat( node2.getResource( "key2"), is( "value2" ));
	}

	@Test
	public void testToString() {
		data.defineBusinessKey( "firstName", "lastName", "birthDate" );
		assertThat( data.toString(), is( "MockNode[]" ) );

		Date birthDate = new Date( 0 );
		data.setValue( "firstName", "Jane" );
		data.setValue( "birthDate", birthDate );
		assertThat( data.toString(), is( "MockNode[firstName=Jane,birthDate=" + birthDate.toString() + "]" ) );

		data.setValue( "lastName", "Doe" );
		assertThat( data.toString(), is( "MockNode[firstName=Jane,lastName=Doe,birthDate=" + birthDate.toString() + "]" ) );
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

	private static Matcher<Node> hasStates( boolean modified, int modifiedValueCount, int modifiedChildCount ) {
		Matcher<Node> modifiedMatcher = modifiedFlag( is( modified ) );
		Matcher<Node> modifiedValueCountMatcher = modifiedValueCount( is( modifiedValueCount ) );
		Matcher<Node> modifiedChildCountMatcher = modifiedChildCount( is( modifiedChildCount ) );
		return allOf( modifiedMatcher, modifiedValueCountMatcher, modifiedChildCountMatcher );
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

	private static Matcher<Node> modifiedChildCount( Matcher<? super Integer> matcher ) {
		return new FeatureMatcher<Node, Integer>( matcher, "modified child count", "count" ) {

			@Override
			protected Integer featureValueOf( Node node ) {
				return node.getModifiedChildCount();
			}

		};
	}

	private static Matcher<NodeWatcher> hasEventCounts( int node, int flag, int value ) {
		Matcher<NodeWatcher> nodeEventCounter = eventCount( NodeEvent.Type.NODE_CHANGED, is( node ) );
		Matcher<NodeWatcher> flagEventCounter = eventCount( NodeEvent.Type.FLAG_CHANGED, is( flag ) );
		Matcher<NodeWatcher> valueEventCounter = eventCount( NodeEvent.Type.VALUE_CHANGED, is( value ) );
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
