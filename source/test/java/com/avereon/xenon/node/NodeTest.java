package com.avereon.xenon.node;

import com.avereon.event.EventType;
import com.avereon.xenon.transaction.Txn;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

class NodeTest {

	private MockNode data;

	@BeforeEach
	void setup() {
		data = new MockNode();
	}

	@Test
	void testNewNodeModifiedState() {
		assertThat( data.isModified(), is( false ) );
	}

	@Test
	void testModifyByFlagAndUnmodifyByFlag() {
		int index = 0;
		assertThat( data.isModifiedByValue(), is( false ) );
		assertThat( data.isModifiedByChild(), is( false ) );
		assertThat( data.isModified(), is( false ) );
		assertThat( data.getEventCount(), is( index ) );

		data.setModified( true );
		assertThat( data.isModifiedByValue(), is( false ) );
		assertThat( data.isModifiedByChild(), is( false ) );
		assertThat( data.isModified(), is( true ) );
		assertEventState( data, index++, NodeEvent.MODIFIED );
		assertEventState( data, index++, NodeEvent.NODE_CHANGED );
		assertThat( data.getEventCount(), is( index ) );

		data.setModified( false );
		assertThat( data.isModifiedByValue(), is( false ) );
		assertThat( data.isModifiedByChild(), is( false ) );
		assertThat( data.isModified(), is( false ) );
		assertEventState( data, index++, NodeEvent.UNMODIFIED );
		assertEventState( data, index++, NodeEvent.NODE_CHANGED );
		assertThat( data.getEventCount(), is( index ) );
	}

	@Test
	void testModifyByValueAndUnmodifyByFlag() {
		int index = 0;
		assertThat( data, hasStates( false, 0, 0 ) );
		assertThat( data.getEventCount(), is( index ) );

		data.setValue( "id", 423984 );
		assertThat( data, hasStates( true, 1, 0 ) );
		assertEventState( data, index++, NodeEvent.VALUE_CHANGED, "id", null, 423984 );
		assertEventState( data, index++, NodeEvent.MODIFIED );
		assertEventState( data, index++, NodeEvent.NODE_CHANGED );
		assertThat( data.getEventCount(), is( index ) );

		data.setModified( false );
		assertThat( data, hasStates( false, 0, 0 ) );
		assertEventState( data, index++, NodeEvent.UNMODIFIED );
		assertEventState( data, index++, NodeEvent.NODE_CHANGED );
		assertThat( data.getEventCount(), is( index ) );
	}

	@Test
	void testModifyByValueAndUnmodifyByValue() {
		int index = 0;
		assertThat( data, hasStates( false, 0, 0 ) );
		assertThat( data.getEventCount(), is( index ) );

		data.setValue( "id", 423984 );
		assertThat( data, hasStates( true, 1, 0 ) );
		assertEventState( data, index++, NodeEvent.VALUE_CHANGED, "id", null, 423984 );
		assertEventState( data, index++, NodeEvent.MODIFIED );
		assertEventState( data, index++, NodeEvent.NODE_CHANGED );
		assertThat( data.getEventCount(), is( index ) );

		data.setValue( "id", null );
		assertThat( data, hasStates( false, 0, 0 ) );
		assertEventState( data, index++, NodeEvent.VALUE_CHANGED, "id", 423984, null );
		assertEventState( data, index++, NodeEvent.UNMODIFIED );
		assertEventState( data, index++, NodeEvent.NODE_CHANGED );
		assertThat( data.getEventCount(), is( index ) );
	}

	@Test
	void testGetValueKeys() {
		data.setValue( "key", "value" );
		assertThat( data.getValueKeys(), containsInAnyOrder( "key" ) );
	}

	@Test
	void testValues() {
		String key = "key";
		Object value = "value";
		assertThat( data.getValue( key ), is( nullValue() ) );

		data.setValue( key, value );
		assertThat( data.getValue( key ), is( value ) );

		data.setValue( key, null );
		assertThat( data.getValue( key ), is( nullValue() ) );
	}

	@Test
	void testObjectValue() {
		String key = "key";
		Object value = new Object();
		assertThat( data.getValue( key ), is( nullValue() ) );

		data.setValue( key, value );
		assertThat( data.getValue( key ), is( value ) );
	}

	@Test
	void testStringValue() {
		String key = "key";
		String value = "value";
		assertThat( data.getValue( key ), is( nullValue() ) );

		data.setValue( key, value );
		assertThat( data.getValue( key ), is( value ) );
	}

	@Test
	void testBooleanValue() {
		String key = "key";
		assertThat( data.getValue( key ), is( nullValue() ) );
		data.setValue( key, true );
		assertThat( data.getValue( key ), is( true ) );
	}

	@Test
	void testIntegerValue() {
		String key = "key";
		int value = 0;
		assertThat( data.getValue( key ), is( nullValue() ) );

		data.setValue( key, value );
		assertThat( data.getValue( key ), is( value ) );
	}

	@Test
	void testSetNullValueToNull() {
		String key = "value";
		assertThat( data.getValue( key ), is( nullValue() ) );
		data.setValue( key, null );
		assertThat( data.getEventCount(), is( 0 ) );
	}

	@Test
	void testSetValueWithNullName() {
		try {
			data.setValue( null, "value" );
			fail( "Null value keys are not allowed" );
		} catch( NullPointerException exception ) {
			assertThat( exception.getMessage(), is( "Value key cannot be null" ) );
		}
		assertThat( data.getEventCount(), is( 0 ) );
	}

	@Test
	void testGetValueWithNullKey() {
		try {
			data.getValue( null );
			fail( "Null value keys are not allowed." );
		} catch( NullPointerException exception ) {
			assertThat( exception.getMessage(), is( "Value key cannot be null" ) );
		}
	}

	@Test
	void testNullValueValues() {
		int index = 0;

		// Assert initial values
		assertThat( data.getValue( "attribute" ), is( nullValue() ) );
		assertThat( data, hasStates( false, 0, 0 ) );
		assertThat( data.getEventCount(), is( index ) );

		// Set the value to null and make sure nothing happens
		data.setValue( "attribute", null );
		assertThat( data.getValue( "attribute" ), is( nullValue() ) );
		assertThat( data, hasStates( false, 0, 0 ) );
		assertThat( data.getEventCount(), is( index ) );

		// Set the value
		data.setValue( "attribute", "value" );
		assertThat( data.getValue( "attribute" ), is( "value" ) );
		assertThat( data, hasStates( true, 1, 0 ) );
		assertEventState( data, index++, NodeEvent.VALUE_CHANGED, "attribute", null, "value" );
		assertEventState( data, index++, NodeEvent.MODIFIED );
		assertEventState( data, index++, NodeEvent.NODE_CHANGED );
		assertThat( data.getEventCount(), is( index ) );

		// Set the value to the same value and make sure nothing happens
		data.setValue( "attribute", "value" );
		assertThat( data.getValue( "attribute" ), is( "value" ) );
		assertThat( data, hasStates( true, 1, 0 ) );
		assertThat( data.getEventCount(), is( index ) );

		// Set the value back to null
		data.setValue( "attribute", null );
		assertThat( data.getValue( "attribute" ), is( nullValue() ) );
		assertThat( data, hasStates( false, 0, 0 ) );
		assertEventState( data, index++, NodeEvent.VALUE_CHANGED, "attribute", "value", null );
		assertEventState( data, index++, NodeEvent.UNMODIFIED );
		assertEventState( data, index++, NodeEvent.NODE_CHANGED );
		assertThat( data.getEventCount(), is( index ) );

		// Set the value to null again and make sure nothing happens
		data.setValue( "attribute", null );
		assertThat( data.getValue( "attribute" ), is( nullValue() ) );
		assertThat( data, hasStates( false, 0, 0 ) );
		assertThat( data.getEventCount(), is( index ) );
	}

	@Test
	void testGetAndSetValue() {
		int index = 0;
		assertThat( data.getValue( "x" ), is( nullValue() ) );
		assertThat( data.getValue( "y" ), is( nullValue() ) );
		assertThat( data.getValue( "z" ), is( nullValue() ) );
		assertThat( data, hasStates( false, 0, 0 ) );
		assertThat( data.getEventCount(), is( index ) );

		data.setValue( "x", 1.0 );
		assertThat( data.getValue( "x" ), is( 1.0 ) );
		assertThat( data.getValue( "y" ), is( nullValue() ) );
		assertThat( data.getValue( "z" ), is( nullValue() ) );
		assertThat( data, hasStates( true, 1, 0 ) );
		assertEventState( data, index++, NodeEvent.VALUE_CHANGED, "x", null, 1.0 );
		assertEventState( data, index++, NodeEvent.MODIFIED );
		assertEventState( data, index++, NodeEvent.NODE_CHANGED );
		assertThat( data.getEventCount(), is( index ) );

		data.setValue( "x", 0.0 );
		assertThat( data.getValue( "x" ), is( 0.0 ) );
		assertThat( data.getValue( "y" ), is( nullValue() ) );
		assertThat( data.getValue( "z" ), is( nullValue() ) );
		assertThat( data, hasStates( true, 1, 0 ) );
		assertEventState( data, index++, NodeEvent.VALUE_CHANGED, "x", 1.0, 0.0 );
		assertEventState( data, index++, NodeEvent.NODE_CHANGED );
		assertThat( data.getEventCount(), is( index ) );
	}

	@Test
	void testGetValueWithDefault() {
		assertThat( data.getValue( "key" ), is( nullValue() ) );
		assertThat( data.getValue( "key", "default" ), is( "default" ) );
	}

	@Test
	void testModifiedBySetAttribute() {
		int index = 0;
		assertThat( data, hasStates( false, 0, 0 ) );
		assertThat( data.getEventCount(), is( index ) );

		data.setValue( "x", 1 );
		data.setValue( "y", 2 );
		data.setValue( "z", 3 );
		assertThat( data, hasStates( true, 3, 0 ) );
		assertEventState( data, index++, NodeEvent.VALUE_CHANGED, "x", null, 1 );
		assertEventState( data, index++, NodeEvent.MODIFIED );
		assertEventState( data, index++, NodeEvent.NODE_CHANGED );
		assertEventState( data, index++, NodeEvent.VALUE_CHANGED, "y", null, 2 );
		assertEventState( data, index++, NodeEvent.NODE_CHANGED );
		assertEventState( data, index++, NodeEvent.VALUE_CHANGED, "z", null, 3 );
		assertEventState( data, index++, NodeEvent.NODE_CHANGED );
		assertThat( data.getEventCount(), is( index ) );
	}

	@Test
	void testUnmodifiedByUnsetValue() {
		int index = 0;
		assertThat( data, hasStates( false, 0, 0 ) );
		assertThat( data.getEventCount(), is( index ) );

		data.setValue( "x", 0 );
		data.setValue( "y", 0 );
		data.setValue( "z", 0 );
		data.setModified( false );
		assertThat( data, hasStates( false, 0, 0 ) );
		assertEventState( data, index++, NodeEvent.VALUE_CHANGED, "x", null, 0 );
		assertEventState( data, index++, NodeEvent.MODIFIED );
		assertEventState( data, index++, NodeEvent.NODE_CHANGED );
		assertEventState( data, index++, NodeEvent.VALUE_CHANGED, "y", null, 0 );
		assertEventState( data, index++, NodeEvent.NODE_CHANGED );
		assertEventState( data, index++, NodeEvent.VALUE_CHANGED, "z", null, 0 );
		assertEventState( data, index++, NodeEvent.NODE_CHANGED );
		assertEventState( data, index++, NodeEvent.UNMODIFIED );
		assertEventState( data, index++, NodeEvent.NODE_CHANGED );
		assertThat( data.getEventCount(), is( index ) );

		data.setValue( "x", 1 );
		data.setValue( "y", 2 );
		data.setValue( "z", 3 );
		assertThat( data, hasStates( true, 3, 0 ) );
		assertEventState( data, index++, NodeEvent.VALUE_CHANGED, "x", 0, 1 );
		assertEventState( data, index++, NodeEvent.MODIFIED );
		assertEventState( data, index++, NodeEvent.NODE_CHANGED );
		assertEventState( data, index++, NodeEvent.VALUE_CHANGED, "y", 0, 2 );
		assertEventState( data, index++, NodeEvent.NODE_CHANGED );
		assertEventState( data, index++, NodeEvent.VALUE_CHANGED, "z", 0, 3 );
		assertEventState( data, index++, NodeEvent.NODE_CHANGED );
		assertThat( data.getEventCount(), is( index ) );

		data.setValue( "x", 0 );
		data.setValue( "y", 0 );
		data.setValue( "z", 0 );
		assertThat( data, hasStates( false, 0, 0 ) );
		assertEventState( data, index++, NodeEvent.VALUE_CHANGED, "x", 1, 0 );
		assertEventState( data, index++, NodeEvent.NODE_CHANGED );
		assertEventState( data, index++, NodeEvent.VALUE_CHANGED, "y", 2, 0 );
		assertEventState( data, index++, NodeEvent.NODE_CHANGED );
		assertEventState( data, index++, NodeEvent.VALUE_CHANGED, "z", 3, 0 );
		assertEventState( data, index++, NodeEvent.UNMODIFIED );
		assertEventState( data, index++, NodeEvent.NODE_CHANGED );
		assertThat( data.getEventCount(), is( index ) );
	}

	@Test
	void testModifiedAttributeCountResetByClearingModifiedFlag() {
		int index = 0;
		assertThat( data, hasStates( false, 0, 0 ) );
		assertThat( data.getEventCount(), is( index ) );

		data.setValue( "x", 1 );
		data.setValue( "y", 2 );
		data.setValue( "z", 3 );
		assertThat( data, hasStates( true, 3, 0 ) );
		assertEventState( data, index++, NodeEvent.VALUE_CHANGED, "x", null, 1 );
		assertEventState( data, index++, NodeEvent.MODIFIED );
		assertEventState( data, index++, NodeEvent.NODE_CHANGED );
		assertEventState( data, index++, NodeEvent.VALUE_CHANGED, "y", null, 2 );
		assertEventState( data, index++, NodeEvent.NODE_CHANGED );
		assertEventState( data, index++, NodeEvent.VALUE_CHANGED, "z", null, 3 );
		assertEventState( data, index++, NodeEvent.NODE_CHANGED );
		assertThat( data.getEventCount(), is( index ) );

		data.setModified( false );
		assertThat( data, hasStates( false, 0, 0 ) );
		assertEventState( data, index++, NodeEvent.UNMODIFIED );
		assertEventState( data, index++, NodeEvent.NODE_CHANGED );
		assertThat( data.getEventCount(), is( index ) );
	}

	@Test
	void testClear() {
		int index = 0;
		assertThat( data, hasStates( false, 0, 0 ) );
		assertThat( data.getEventCount(), is( index ) );

		data.setValue( "x", 1 );
		data.setValue( "y", 2 );
		data.setValue( "z", 3 );
		assertThat( data, hasStates( true, 3, 0 ) );
		assertEventState( data, index++, NodeEvent.VALUE_CHANGED, "x", null, 1 );
		assertEventState( data, index++, NodeEvent.MODIFIED );
		assertEventState( data, index++, NodeEvent.NODE_CHANGED );
		assertEventState( data, index++, NodeEvent.VALUE_CHANGED, "y", null, 2 );
		assertEventState( data, index++, NodeEvent.NODE_CHANGED );
		assertEventState( data, index++, NodeEvent.VALUE_CHANGED, "z", null, 3 );
		assertEventState( data, index++, NodeEvent.NODE_CHANGED );
		assertThat( data.getEventCount(), is( index ) );

		data.clear();
		assertThat( data, hasStates( false, 0, 0 ) );
		assertEventState( data, index++, NodeEvent.VALUE_CHANGED, "x", 1, null );
		assertEventState( data, index++, NodeEvent.VALUE_CHANGED, "y", 2, null );
		assertEventState( data, index++, NodeEvent.VALUE_CHANGED, "z", 3, null );
		assertEventState( data, index++, NodeEvent.UNMODIFIED );
		assertEventState( data, index++, NodeEvent.NODE_CHANGED );
		assertThat( data.getEventCount(), is( index ) );
	}

	@Test
	void testResourceKeys() {
		data.putResource( "key", "value" );
		assertThat( data.getResourceKeys(), containsInAnyOrder( "key" ) );
	}

	@Test
	void testResources() {
		int index = 0;
		assertThat( data, hasStates( false, 0, 0 ) );
		assertThat( data.getEventCount(), is( index ) );

		data.putResource( "name", "value" );
		assertThat( data.getResource( "name" ), is( "value" ) );
		assertThat( data, hasStates( false, 0, 0 ) );
		assertThat( data.getEventCount(), is( index ) );

		data.putResource( "name", null );
		assertThat( data.getResource( "name" ), is( nullValue() ) );
		assertThat( data, hasStates( false, 0, 0 ) );
		assertThat( data.getEventCount(), is( index ) );
	}

	@Test
	void testDataEventNotification() {
		int index = 0;
		assertThat( data, hasStates( false, 0, 0 ) );
		assertThat( data.getEventCount(), is( index ) );

		// Set a value
		data.setValue( "attribute", "value0" );
		assertThat( data, hasStates( true, 1, 0 ) );
		assertEventState( data, index++, NodeEvent.VALUE_CHANGED, "attribute", null, "value0" );
		assertEventState( data, index++, NodeEvent.MODIFIED );
		assertEventState( data, index++, NodeEvent.NODE_CHANGED );
		assertThat( data.getEventCount(), is( index ) );

		// Set the value to the same value
		data.setValue( "attribute", "value0" );
		assertThat( data, hasStates( true, 1, 0 ) );
		assertThat( data.getEventCount(), is( index ) );

		// Modify the value
		data.setValue( "attribute", "value1" );
		assertThat( data, hasStates( true, 1, 0 ) );
		assertEventState( data, index++, NodeEvent.VALUE_CHANGED, "attribute", "value0", "value1" );
		assertEventState( data, index++, NodeEvent.NODE_CHANGED );
		assertThat( data.getEventCount(), is( index ) );

		// Remove the attribute.
		data.setValue( "attribute", null );
		assertThat( data, hasStates( false, 0, 0 ) );
		assertEventState( data, index++, NodeEvent.VALUE_CHANGED, "attribute", "value1", null );
		assertEventState( data, index++, NodeEvent.UNMODIFIED );
		assertEventState( data, index++, NodeEvent.NODE_CHANGED );
		assertThat( data.getEventCount(), is( index ) );
	}

	@Test
	void testEventsWithModifiedFlagFalse() {
		int index = 0;
		assertThat( data, hasStates( false, 0, 0 ) );
		assertThat( data.getEventCount(), is( index ) );

		// Change a value
		data.setValue( "attribute", "value0" );
		assertThat( data, hasStates( true, 1, 0 ) );
		assertEventState( data, index++, NodeEvent.VALUE_CHANGED, "attribute", null, "value0" );
		assertEventState( data, index++, NodeEvent.MODIFIED );
		assertEventState( data, index++, NodeEvent.NODE_CHANGED );
		assertThat( data.getEventCount(), is( index ) );

		// Set the modified flag to false
		data.setModified( false );
		assertThat( data, hasStates( false, 0, 0 ) );
		assertEventState( data, index++, NodeEvent.UNMODIFIED );
		assertEventState( data, index++, NodeEvent.NODE_CHANGED );
		assertThat( data.getEventCount(), is( index ) );

		// Set the modified flag to false again
		data.setModified( false );
		assertThat( data, hasStates( false, 0, 0 ) );
		assertThat( data.getEventCount(), is( index ) );
	}

	@Test
	void testSetClearSetValueInTransaction() throws Exception {
		data.setValue( "x", 1 );
		Txn.create();
		data.setValue( "x", null );
		data.setValue( "x", 1 );
		Txn.commit();
		assertThat( data.getValue( "x" ), is( 1 ) );
	}

	@Test
	void testCollapsingEventsWithTransaction() throws Exception {
		int index = 0;
		assertThat( data, hasStates( false, 0, 0 ) );
		assertThat( data.getEventCount(), is( index ) );

		Txn.create();
		data.setValue( "a", "1" );
		data.setValue( "a", "2" );
		data.setValue( "a", "3" );
		data.setValue( "a", "4" );
		data.setValue( "a", "5" );
		Txn.commit();

		assertThat( data.getValue( "a" ), is( "5" ) );
		assertThat( data, hasStates( true, 1, 0 ) );
		assertEventState( data, index++, NodeEvent.VALUE_CHANGED, "a", null, "5" );
		assertEventState( data, index++, NodeEvent.MODIFIED );
		assertEventState( data, index++, NodeEvent.NODE_CHANGED );
		assertThat( data.getEventCount(), is( index ) );
	}

	@Test
	void testGetParent() {
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
	void testGetNodePath() {
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
	void testParentGetsModifiedEventsWhenChildModifiedAndUnmodified() {
		// Start with a standard parent/child model
		MockNode parent = new MockNode( "parent" );
		MockNode child = new MockNode( "child" );
		parent.setValue( "child", child );
		parent.setModified( false );
		child.getWatcher().reset();
		parent.getWatcher().reset();
		assertThat( child, hasStates( false, 0, 0 ) );
		assertThat( parent, hasStates( false, 0, 0 ) );
		int index = 0;

		// Set an attribute on the child to modify the child and parent
		child.setValue( "attribute", "value0" );
		assertThat( child, hasStates( true, 1, 0 ) );
		assertThat( parent, hasStates( true, 0, 1 ) );
		assertEventState( parent, index++, NodeEvent.VALUE_CHANGED, child, "attribute", null, "value0" );
		assertEventState( parent, index++, NodeEvent.MODIFIED );
		assertEventState( parent, index++, NodeEvent.NODE_CHANGED );
		assertThat( parent.getEventCount(), is( index ) );

		// Change the attribute value on the child
		child.setValue( "attribute", "value1" );
		assertThat( child, hasStates( true, 1, 0 ) );
		assertThat( parent, hasStates( true, 0, 1 ) );
		assertEventState( parent, index++, NodeEvent.VALUE_CHANGED, child, "attribute", "value0", "value1" );
		// The parent is already modified so there should not be a modified event here
		assertEventState( parent, index++, NodeEvent.NODE_CHANGED );
		assertThat( parent.getEventCount(), is( index ) );

		// Set the child attribute back to null to unmodify the child and parent
		child.setValue( "attribute", null );
		assertThat( child, hasStates( false, 0, 0 ) );
		assertThat( parent, hasStates( false, 0, 0 ) );
		assertEventState( parent, index++, NodeEvent.VALUE_CHANGED, child, "attribute", "value1", null );
		assertEventState( parent, index++, NodeEvent.UNMODIFIED );
		assertEventState( parent, index++, NodeEvent.NODE_CHANGED );
		assertThat( parent.getEventCount(), is( index ) );
	}

	@Test
	void testChildModifiedClearedWhenParentModifiedCleared() {
		// Start with a standard parent/child model
		MockNode parent = new MockNode( "parent" );
		MockNode child = new MockNode( "child" );
		parent.setValue( "child", child );
		parent.setModified( false );
		child.getWatcher().reset();
		parent.getWatcher().reset();
		assertThat( child, hasStates( false, 0, 0 ) );
		assertThat( parent, hasStates( false, 0, 0 ) );
		int index = 0;

		// Set an attribute on the child to modify the child and parent
		child.setValue( "attribute", "value0" );
		assertThat( child, hasStates( true, 1, 0 ) );
		assertThat( parent, hasStates( true, 0, 1 ) );
		assertEventState( parent, index++, NodeEvent.VALUE_CHANGED, child, "attribute", null, "value0" );
		assertEventState( parent, index++, NodeEvent.MODIFIED );
		assertEventState( parent, index++, NodeEvent.NODE_CHANGED );
		assertThat( parent.getEventCount(), is( index ) );

		// Clear the parent modified flag
		parent.setModified( false );
		assertThat( child, hasStates( false, 0, 0 ) );
		assertThat( parent, hasStates( false, 0, 0 ) );
		assertEventState( parent, index++, NodeEvent.UNMODIFIED );
		assertEventState( parent, index++, NodeEvent.NODE_CHANGED );
		assertThat( parent.getEventCount(), is( index ) );
	}

	@Test
	void testParentModifiedAndUnmodifiedByChildNodeAttributeChangeWithNullStartValue() {
		// Start with a standard parent/child model
		MockNode parent = new MockNode( "parent" );
		MockNode child = new MockNode( "child" );
		parent.setValue( "child", child );
		parent.setModified( false );
		child.getWatcher().reset();
		parent.getWatcher().reset();
		assertThat( child, hasStates( false, 0, 0 ) );
		assertThat( parent, hasStates( false, 0, 0 ) );
		int index = 0;

		// Test setting a value on the child node modifies the parent
		child.setValue( "attribute", "value" );
		assertThat( child, hasStates( true, 1, 0 ) );
		assertThat( parent, hasStates( true, 0, 1 ) );
		assertEventState( parent, index++, NodeEvent.VALUE_CHANGED, child, "attribute", null, "value" );
		assertEventState( parent, index++, NodeEvent.MODIFIED );
		assertEventState( parent, index++, NodeEvent.NODE_CHANGED );
		assertThat( parent.getEventCount(), is( index ) );

		// Test clear the value on the child node unmodifies the parent
		child.setValue( "attribute", null );
		assertThat( child, hasStates( false, 0, 0 ) );
		assertThat( parent, hasStates( false, 0, 0 ) );
		assertEventState( parent, index++, NodeEvent.VALUE_CHANGED, child, "attribute", "value", null );
		assertEventState( parent, index++, NodeEvent.UNMODIFIED );
		assertEventState( parent, index++, NodeEvent.NODE_CHANGED );
		assertThat( parent.getEventCount(), is( index ) );
	}

	@Test
	void testParentModifiedAndUnmodifiedByChildNodeAttributeChangeWithNonNullStartValue() {
		// Start with a standard parent/child model
		MockNode parent = new MockNode( "parent" );
		MockNode child = new MockNode( "child" );
		parent.setValue( "child", child );
		parent.setModified( false );
		child.getWatcher().reset();
		parent.getWatcher().reset();
		assertThat( child, hasStates( false, 0, 0 ) );
		assertThat( parent, hasStates( false, 0, 0 ) );
		int index = 0;

		// Set an attribute on the child to a non-null value and clear the modified flags
		child.setValue( "attribute", "value0" );
		parent.setModified( false );
		assertThat( child, hasStates( false, 0, 0 ) );
		assertThat( parent, hasStates( false, 0, 0 ) );
		assertEventState( parent, index++, NodeEvent.VALUE_CHANGED, child, "attribute", null, "value0" );
		assertEventState( parent, index++, NodeEvent.MODIFIED );
		assertEventState( parent, index++, NodeEvent.NODE_CHANGED );
		assertEventState( parent, index++, NodeEvent.UNMODIFIED );
		assertEventState( parent, index++, NodeEvent.NODE_CHANGED );

		// Change the attribute value on the child
		child.setValue( "attribute", "value1" );
		assertThat( child, hasStates( true, 1, 0 ) );
		assertThat( parent, hasStates( true, 0, 1 ) );
		assertEventState( parent, index++, NodeEvent.VALUE_CHANGED, child, "attribute", "value0", "value1" );
		assertEventState( parent, index++, NodeEvent.MODIFIED );
		assertEventState( parent, index++, NodeEvent.NODE_CHANGED );
		assertThat( parent.getEventCount(), is( index ) );

		// Set the child attribute to the same value, should do nothing
		child.setValue( "attribute", "value1" );
		assertThat( child, hasStates( true, 1, 0 ) );
		assertThat( parent, hasStates( true, 0, 1 ) );
		assertThat( parent.getEventCount(), is( index ) );

		// Set the child attribute back to value0
		child.setValue( "attribute", "value0" );
		assertThat( child, hasStates( false, 0, 0 ) );
		assertThat( parent, hasStates( false, 0, 0 ) );
		assertEventState( parent, index++, NodeEvent.VALUE_CHANGED, child, "attribute", "value1", "value0" );
		assertEventState( parent, index++, NodeEvent.UNMODIFIED );
		assertEventState( parent, index++, NodeEvent.NODE_CHANGED );
		assertThat( parent.getEventCount(), is( index ) );
	}

	@Test
	void testGrandparentModifiedByChildNodeAttributeChange() {
		int parentIndex = 0;
		int childIndex = 0;
		int grandChildIndex = 0;
		MockNode parent = new MockNode( "parent" );
		MockNode child = new MockNode( "child" );
		MockNode grandChild = new MockNode( "grandChild" );
		assertThat( parent, hasStates( false, 0, 0 ) );
		assertThat( child, hasStates( false, 0, 0 ) );
		assertThat( grandChild, hasStates( false, 0, 0 ) );
		assertThat( parent.getEventCount(), is( parentIndex ) );
		assertThat( child.getEventCount(), is( childIndex ) );
		assertThat( grandChild.getEventCount(), is( grandChildIndex ) );

		parent.setValue( "child", child );
		assertThat( child.getParent(), is( parent ) );
		assertThat( parent, hasStates( true, 1, 0 ) );
		assertThat( child, hasStates( false, 0, 0 ) );
		assertThat( grandChild, hasStates( false, 0, 0 ) );
		assertEventState( parent, parentIndex++, NodeEvent.VALUE_CHANGED, "child", null, child );
		assertEventState( parent, parentIndex++, NodeEvent.MODIFIED );
		assertEventState( parent, parentIndex++, NodeEvent.NODE_CHANGED );
		assertThat( parent.getEventCount(), is( parentIndex ) );
		assertThat( child.getEventCount(), is( childIndex ) );
		assertThat( grandChild.getEventCount(), is( grandChildIndex ) );

		child.setValue( "child", grandChild );
		assertThat( grandChild.getParent(), is( child ) );
		assertThat( parent, hasStates( true, 1, 1 ) );
		assertThat( child, hasStates( true, 1, 0 ) );
		assertThat( grandChild, hasStates( false, 0, 0 ) );
		assertEventState( parent, parentIndex++, NodeEvent.VALUE_CHANGED, child, "child", null, grandChild );
		assertEventState( parent, parentIndex++, NodeEvent.NODE_CHANGED );
		assertEventState( child, childIndex++, NodeEvent.VALUE_CHANGED, "child", null, grandChild );
		assertEventState( child, childIndex++, NodeEvent.MODIFIED );
		assertEventState( child, childIndex++, NodeEvent.NODE_CHANGED );
		assertThat( parent.getEventCount(), is( parentIndex ) );
		assertThat( child.getEventCount(), is( childIndex ) );
		assertThat( grandChild.getEventCount(), is( grandChildIndex ) );

		parent.setModified( false );
		assertThat( parent, hasStates( false, 0, 0 ) );
		assertThat( child, hasStates( false, 0, 0 ) );
		assertThat( grandChild, hasStates( false, 0, 0 ) );
		assertEventState( parent, parentIndex++, NodeEvent.UNMODIFIED );
		assertEventState( parent, parentIndex++, NodeEvent.NODE_CHANGED );
		assertEventState( child, childIndex++, NodeEvent.UNMODIFIED );
		assertEventState( child, childIndex++, NodeEvent.NODE_CHANGED );
		assertThat( parent.getEventCount(), is( parentIndex ) );
		assertThat( child.getEventCount(), is( childIndex ) );
		assertThat( grandChild.getEventCount(), is( grandChildIndex ) );

		// Test setting a value on the child node modifies the parents
		grandChild.setValue( "attribute", "value" );
		assertThat( parent, hasStates( true, 0, 1 ) );
		assertThat( child, hasStates( true, 0, 1 ) );
		assertThat( grandChild, hasStates( true, 1, 0 ) );
		assertEventState( parent, parentIndex++, NodeEvent.MODIFIED, child, null, null, null );
		assertEventState( parent, parentIndex++, NodeEvent.NODE_CHANGED );
		assertEventState( child, childIndex++, NodeEvent.VALUE_CHANGED, grandChild, "attribute", null, "value" );
		assertEventState( child, childIndex++, NodeEvent.MODIFIED, grandChild, null, null, null );
		assertEventState( child, childIndex++, NodeEvent.NODE_CHANGED );
		assertEventState( grandChild, grandChildIndex++, NodeEvent.VALUE_CHANGED, "attribute", null, "value" );
		assertEventState( grandChild, grandChildIndex++, NodeEvent.MODIFIED );
		assertEventState( grandChild, grandChildIndex++, NodeEvent.NODE_CHANGED );
		assertThat( parent.getEventCount(), is( parentIndex ) );
		assertThat( child.getEventCount(), is( childIndex ) );
		assertThat( grandChild.getEventCount(), is( grandChildIndex ) );

		// Test unsetting the value on the child node unmodifies the parents
		grandChild.setValue( "attribute", null );
		assertThat( parent, hasStates( false, 0, 0 ) );
		assertThat( grandChild, hasStates( false, 0, 0 ) );
		assertThat( child, hasStates( false, 0, 0 ) );
		assertEventState( parent, parentIndex++, NodeEvent.UNMODIFIED );
		assertEventState( parent, parentIndex++, NodeEvent.NODE_CHANGED );
		assertEventState( child, childIndex++, NodeEvent.VALUE_CHANGED, grandChild, "attribute", "value", null );
		assertEventState( child, childIndex++, NodeEvent.UNMODIFIED );
		assertEventState( child, childIndex++, NodeEvent.NODE_CHANGED );
		assertEventState( grandChild, grandChildIndex++, NodeEvent.VALUE_CHANGED, "attribute", "value", null );
		assertEventState( grandChild, grandChildIndex++, NodeEvent.UNMODIFIED );
		assertEventState( grandChild, grandChildIndex++, NodeEvent.NODE_CHANGED );
		assertThat( parent.getEventCount(), is( parentIndex ) );
		assertThat( child.getEventCount(), is( childIndex ) );
		assertThat( grandChild.getEventCount(), is( grandChildIndex ) );
	}

	@Test
	void testParentModifiedByChildNodeClearedByFlag() {
		MockNode parent = new MockNode( "parent" );
		MockNode child = new MockNode( "child" );
		int parentIndex = 0;
		int childIndex = 0;
		assertThat( parent, hasStates( false, 0, 0 ) );
		assertThat( child, hasStates( false, 0, 0 ) );
		assertThat( parent.getEventCount(), is( parentIndex ) );
		assertThat( child.getEventCount(), is( childIndex ) );

		parent.setValue( "child", child );
		assertThat( child.getParent(), is( parent ) );
		assertThat( parent, hasStates( true, 1, 0 ) );
		assertThat( child, hasStates( false, 0, 0 ) );
		assertEventState( parent, parentIndex++, NodeEvent.VALUE_CHANGED, "child", null, child );
		assertEventState( parent, parentIndex++, NodeEvent.MODIFIED );
		assertEventState( parent, parentIndex++, NodeEvent.NODE_CHANGED );
		assertThat( parent.getEventCount(), is( parentIndex ) );
		assertThat( child.getEventCount(), is( childIndex ) );

		parent.setModified( false );
		assertThat( parent, hasStates( false, 0, 0 ) );
		assertThat( child, hasStates( false, 0, 0 ) );
		assertEventState( parent, parentIndex++, NodeEvent.UNMODIFIED );
		assertEventState( parent, parentIndex++, NodeEvent.NODE_CHANGED );
		assertThat( parent.getEventCount(), is( parentIndex ) );
		assertThat( child.getEventCount(), is( childIndex ) );

		// Test setting the 'a' value on the child modifies the parent
		child.setValue( "a", "1" );
		assertThat( parent, hasStates( true, 0, 1 ) );
		assertThat( child, hasStates( true, 1, 0 ) );
		assertEventState( parent, parentIndex++, NodeEvent.VALUE_CHANGED, child, "a", null, "1" );
		assertEventState( parent, parentIndex++, NodeEvent.MODIFIED );
		assertEventState( parent, parentIndex++, NodeEvent.NODE_CHANGED );
		assertEventState( child, childIndex++, NodeEvent.VALUE_CHANGED, "a", null, "1" );
		assertEventState( child, childIndex++, NodeEvent.MODIFIED );
		assertEventState( child, childIndex++, NodeEvent.NODE_CHANGED );
		assertThat( parent.getEventCount(), is( parentIndex ) );
		assertThat( child.getEventCount(), is( childIndex ) );

		// Test setting the 'b' value on the child leaves the parent modified
		child.setValue( "b", "1" );
		assertThat( parent, hasStates( true, 0, 1 ) );
		assertThat( child, hasStates( true, 2, 0 ) );
		assertEventState( parent, parentIndex++, NodeEvent.VALUE_CHANGED, child, "b", null, "1" );
		assertEventState( parent, parentIndex++, NodeEvent.NODE_CHANGED );
		assertEventState( child, childIndex++, NodeEvent.VALUE_CHANGED, "b", null, "1" );
		assertEventState( child, childIndex++, NodeEvent.NODE_CHANGED );
		assertThat( parent.getEventCount(), is( parentIndex ) );
		assertThat( child.getEventCount(), is( childIndex ) );

		// Set this state as the new unmodified state
		child.setModified( false );
		assertThat( parent, hasStates( false, 0, 0 ) );
		assertThat( child, hasStates( false, 0, 0 ) );
		assertEventState( parent, parentIndex++, NodeEvent.UNMODIFIED );
		assertEventState( parent, parentIndex++, NodeEvent.NODE_CHANGED );
		assertEventState( child, childIndex++, NodeEvent.UNMODIFIED );
		assertEventState( child, childIndex++, NodeEvent.NODE_CHANGED );
		assertThat( parent.getEventCount(), is( parentIndex ) );
		assertThat( child.getEventCount(), is( childIndex ) );
	}

	@Test
	void testParentModifiedByChildNodeClearedByValue() {
		MockNode parent = new MockNode( "parent" );
		MockNode child = new MockNode( "child" );
		int parentIndex = 0;
		int childIndex = 0;
		assertThat( parent, hasStates( false, 0, 0 ) );
		assertThat( child, hasStates( false, 0, 0 ) );
		assertThat( parent.getEventCount(), is( parentIndex ) );
		assertThat( child.getEventCount(), is( childIndex ) );

		parent.setValue( "child", child );
		assertThat( child.getParent(), is( parent ) );
		assertThat( parent, hasStates( true, 1, 0 ) );
		assertThat( child, hasStates( false, 0, 0 ) );
		assertEventState( parent, parentIndex++, NodeEvent.VALUE_CHANGED, "child", null, child );
		assertEventState( parent, parentIndex++, NodeEvent.MODIFIED );
		assertEventState( parent, parentIndex++, NodeEvent.NODE_CHANGED );
		assertThat( parent.getEventCount(), is( parentIndex ) );
		assertThat( child.getEventCount(), is( childIndex ) );

		parent.setModified( false );
		assertThat( child, hasStates( false, 0, 0 ) );
		assertThat( parent, hasStates( false, 0, 0 ) );
		assertEventState( parent, parentIndex++, NodeEvent.UNMODIFIED );
		assertEventState( parent, parentIndex++, NodeEvent.NODE_CHANGED );
		assertThat( parent.getEventCount(), is( parentIndex ) );
		assertThat( child.getEventCount(), is( childIndex ) );

		// Test setting the 'a' value on the child modifies the parent
		child.setValue( "a", "2" );
		assertThat( child, hasStates( true, 1, 0 ) );
		assertThat( parent, hasStates( true, 0, 1 ) );
		assertEventState( parent, parentIndex++, NodeEvent.VALUE_CHANGED, child, "a", null, "2" );
		assertEventState( parent, parentIndex++, NodeEvent.MODIFIED );
		assertEventState( parent, parentIndex++, NodeEvent.NODE_CHANGED );
		assertEventState( child, childIndex++, NodeEvent.VALUE_CHANGED, "a", null, "2" );
		assertEventState( child, childIndex++, NodeEvent.MODIFIED );
		assertEventState( child, childIndex++, NodeEvent.NODE_CHANGED );
		assertThat( parent.getEventCount(), is( parentIndex ) );
		assertThat( child.getEventCount(), is( childIndex ) );

		// Test setting the 'b' value on the child leaves the parent modified
		child.setValue( "b", "2" );
		assertThat( child, hasStates( true, 2, 0 ) );
		assertThat( parent, hasStates( true, 0, 1 ) );
		assertEventState( parent, parentIndex++, NodeEvent.VALUE_CHANGED, child, "b", null, "2" );
		assertEventState( parent, parentIndex++, NodeEvent.NODE_CHANGED );
		assertEventState( child, childIndex++, NodeEvent.VALUE_CHANGED, "b", null, "2" );
		assertEventState( child, childIndex++, NodeEvent.NODE_CHANGED );
		assertThat( parent.getEventCount(), is( parentIndex ) );
		assertThat( child.getEventCount(), is( childIndex ) );

		// Test unsetting the 'a' value on the child leaves the parent modified
		child.setValue( "a", null );
		assertThat( child, hasStates( true, 1, 0 ) );
		assertThat( parent, hasStates( true, 0, 1 ) );
		assertEventState( parent, parentIndex++, NodeEvent.VALUE_CHANGED, child, "a", "2", null );
		assertEventState( parent, parentIndex++, NodeEvent.NODE_CHANGED );
		assertEventState( child, childIndex++, NodeEvent.VALUE_CHANGED, "a", "2", null );
		assertEventState( child, childIndex++, NodeEvent.NODE_CHANGED );
		assertThat( parent.getEventCount(), is( parentIndex ) );
		assertThat( child.getEventCount(), is( childIndex ) );

		// Test unsetting the value 'b' on the child returns the parent to unmodified
		child.setValue( "b", null );
		assertThat( child, hasStates( false, 0, 0 ) );
		assertThat( parent, hasStates( false, 0, 0 ) );
		assertEventState( parent, parentIndex++, NodeEvent.VALUE_CHANGED, child, "b", "2", null );
		assertEventState( parent, parentIndex++, NodeEvent.UNMODIFIED );
		assertEventState( parent, parentIndex++, NodeEvent.NODE_CHANGED );
		assertEventState( child, childIndex++, NodeEvent.VALUE_CHANGED, "b", "2", null );
		assertEventState( child, childIndex++, NodeEvent.UNMODIFIED );
		assertEventState( child, childIndex++, NodeEvent.NODE_CHANGED );
		assertThat( parent.getEventCount(), is( parentIndex ) );
		assertThat( child.getEventCount(), is( childIndex ) );
	}

	@Test
	void testChildModifiedClearedByParentSetModifiedFalse() {
		MockNode parent = new MockNode( "parent" );
		MockNode child = new MockNode( "child" );
		int parentIndex = 0;
		int childIndex = 0;
		assertThat( parent, hasStates( false, 0, 0 ) );
		assertThat( child, hasStates( false, 0, 0 ) );
		assertThat( parent.getEventCount(), is( parentIndex ) );
		assertThat( child.getEventCount(), is( childIndex ) );

		parent.setValue( "child", child );
		assertThat( child.getParent(), is( parent ) );
		assertThat( parent, hasStates( true, 1, 0 ) );
		assertThat( child, hasStates( false, 0, 0 ) );
		assertEventState( parent, parentIndex++, NodeEvent.VALUE_CHANGED, "child", null, child );
		assertEventState( parent, parentIndex++, NodeEvent.MODIFIED );
		assertEventState( parent, parentIndex++, NodeEvent.NODE_CHANGED );
		assertThat( parent.getEventCount(), is( parentIndex ) );
		assertThat( child.getEventCount(), is( childIndex ) );

		parent.setModified( false );
		assertThat( child, hasStates( false, 0, 0 ) );
		assertThat( parent, hasStates( false, 0, 0 ) );
		assertEventState( parent, parentIndex++, NodeEvent.UNMODIFIED );
		assertEventState( parent, parentIndex++, NodeEvent.NODE_CHANGED );
		assertThat( parent.getEventCount(), is( parentIndex ) );
		assertThat( child.getEventCount(), is( childIndex ) );

		// Test setting the 'a' value on the child modifies the parent
		child.setValue( "x", "2" );
		assertThat( child, hasStates( true, 1, 0 ) );
		assertThat( parent, hasStates( true, 0, 1 ) );
		assertEventState( parent, parentIndex++, NodeEvent.VALUE_CHANGED, child, "x", null, "2" );
		assertEventState( parent, parentIndex++, NodeEvent.MODIFIED );
		assertEventState( parent, parentIndex++, NodeEvent.NODE_CHANGED );
		assertEventState( child, childIndex++, NodeEvent.VALUE_CHANGED, "x", null, "2" );
		assertEventState( child, childIndex++, NodeEvent.MODIFIED );
		assertEventState( child, childIndex++, NodeEvent.NODE_CHANGED );
		assertThat( parent.getEventCount(), is( parentIndex ) );
		assertThat( child.getEventCount(), is( childIndex ) );

		parent.setModified( false );
		assertFalse( parent.isModified() );
		assertFalse( child.isModified() );
		assertEventState( parent, parentIndex++, NodeEvent.UNMODIFIED );
		assertEventState( parent, parentIndex++, NodeEvent.NODE_CHANGED );
		assertEventState( child, childIndex++, NodeEvent.UNMODIFIED );
		assertEventState( child, childIndex++, NodeEvent.NODE_CHANGED );
		assertThat( parent.getEventCount(), is( parentIndex ) );
		assertThat( child.getEventCount(), is( childIndex ) );
	}

	@Test
	void testAddNodeAttributeToDifferentParent() {
		MockNode parent0 = new MockNode( "parent0" );
		MockNode parent1 = new MockNode( "parent1" );
		MockNode child = new MockNode( "child" );
		int parent0Index = 0;
		int parent1Index = 0;
		int childIndex = 0;
		assertThat( parent0, hasStates( false, 0, 0 ) );
		assertThat( parent1, hasStates( false, 0, 0 ) );
		assertThat( child, hasStates( false, 0, 0 ) );
		assertThat( parent0.getEventCount(), is( parent0Index ) );
		assertThat( parent1.getEventCount(), is( parent1Index ) );
		assertThat( child.getEventCount(), is( childIndex ) );

		// Add the child attribute to parent 0
		parent0.setValue( "child", child );
		assertThat( parent0, hasStates( true, 1, 0 ) );
		assertThat( parent1, hasStates( false, 0, 0 ) );
		assertThat( child, hasStates( false, 0, 0 ) );
		assertEventState( parent0, parent0Index++, NodeEvent.VALUE_CHANGED, "child", null, child );
		assertEventState( parent0, parent0Index++, NodeEvent.MODIFIED );
		assertEventState( parent0, parent0Index++, NodeEvent.NODE_CHANGED );
		assertThat( parent0.getEventCount(), is( parent0Index ) );
		assertThat( parent1.getEventCount(), is( parent1Index ) );
		assertThat( child.getEventCount(), is( childIndex ) );

		// Clear the modified flag of parent 0.
		parent0.setModified( false );
		assertThat( child.getParent(), is( parent0 ) );
		assertThat( parent0, hasStates( false, 0, 0 ) );
		assertThat( parent1, hasStates( false, 0, 0 ) );
		assertThat( child, hasStates( false, 0, 0 ) );
		assertEventState( parent0, parent0Index++, NodeEvent.UNMODIFIED );
		assertEventState( parent0, parent0Index++, NodeEvent.NODE_CHANGED );
		assertThat( parent0.getEventCount(), is( parent0Index ) );
		assertThat( parent1.getEventCount(), is( parent1Index ) );
		assertThat( child.getEventCount(), is( childIndex ) );

		// Add the child attribute to parent 1.
		parent1.setValue( "child", child );
		assertThat( child.getParent(), is( parent1 ) );

		// Oddly both parents have the child as a value...
		// FIXME Should both parents have the child?
		// FIXME If both parents should have the child, what should getParent return?
		assertThat( parent0.getValue( "child" ), is( child ) );
		assertThat( parent1.getValue( "child" ), is( child ) );

		assertThat( parent0, hasStates( false, 0, 0 ) );
		assertThat( parent1, hasStates( true, 1, 0 ) );
		assertThat( child, hasStates( false, 0, 0 ) );
		assertEventState( parent1, parent1Index++, NodeEvent.VALUE_CHANGED, "child", null, child );
		assertEventState( parent1, parent1Index++, NodeEvent.MODIFIED );
		assertEventState( parent1, parent1Index++, NodeEvent.NODE_CHANGED );
		assertThat( parent0.getEventCount(), is( parent0Index ) );
		assertThat( parent1.getEventCount(), is( parent1Index ) );
		assertThat( child.getEventCount(), is( childIndex ) );
	}

	@Test
	void testAddDataListener() {
		// Remove the default watcher
		data.removeNodeListener( data.getWatcher() );

		NodeListener listener = e -> {};
		data.addNodeListener( listener );

		Collection<NodeListener> listeners = data.getNodeListeners();

		assertNotNull( listeners );
		assertThat( listeners.size(), is( 1 ) );
		assertThat( listeners, contains( listener ) );
	}

	@Test
	void testRemoveDataListener() {
		// Remove the default watcher
		data.removeNodeListener( data.getWatcher() );

		Collection<NodeListener> listeners;
		NodeListener listener = e -> {};

		data.addNodeListener( listener );
		listeners = data.getNodeListeners();
		assertNotNull( listeners );
		assertThat( listeners.size(), is( 1 ) );
		assertThat( listeners, contains( listener ) );

		data.removeNodeListener( listener );
		listeners = data.getNodeListeners();
		assertNotNull( listeners );
		assertThat( listeners.size(), is( 0 ) );
		assertThat( listeners, not( contains( listener ) ) );

		data.addNodeListener( listener );
		listeners = data.getNodeListeners();
		assertNotNull( listeners );
		assertThat( listeners.size(), is( 1 ) );
		assertThat( listeners, contains( listener ) );

		data.removeNodeListener( listener );
		listeners = data.getNodeListeners();
		assertNotNull( listeners );
		assertThat( listeners.size(), is( 0 ) );
		assertThat( listeners, not( contains( listener ) ) );
	}

	@Test
	void testCircularReferenceCheck() {
		MockNode node = new MockNode();
		try {
			node.setValue( "node", node );
			fail( "CircularReferenceException should be thrown" );
		} catch( CircularReferenceException exception ) {
			// Intentionally ignore exception.
			assertThat( exception.getMessage(), startsWith( "Circular reference detected" ) );
		}
	}

	@Test
	void testCopyFrom() {
		Node node1 = new Node();
		node1.setValue( "key1", "value1" );
		node1.setValue( "key2", "value2" );

		Node node2 = new Node();
		node2.setValue( "key2", "valueB" );

		node2.copyFrom( node1 );
		assertThat( node1.getValue( "key1" ), is( "value1" ) );
		assertThat( node1.getValue( "key2" ), is( "value2" ) );
		assertThat( node2.getValue( "key1" ), is( "value1" ) );
		assertThat( node2.getValue( "key2" ), is( "valueB" ) );
	}

	@Test
	void testCopyFromWithOverwrite() {
		Node node1 = new Node();
		node1.setValue( "key1", "value1" );
		node1.setValue( "key2", "value2" );

		Node node2 = new Node();
		node2.setValue( "key2", "valueB" );

		node2.copyFrom( node1, true );
		assertThat( node1.getValue( "key1" ), is( "value1" ) );
		assertThat( node1.getValue( "key2" ), is( "value2" ) );
		assertThat( node2.getValue( "key1" ), is( "value1" ) );
		assertThat( node2.getValue( "key2" ), is( "value2" ) );
	}

	@Test
	void testCopyFromUsingResources() {
		Node node1 = new Node();
		node1.putResource( "key1", "value1" );
		node1.putResource( "key2", "value2" );

		Node node2 = new Node();
		node2.putResource( "key2", "valueB" );

		node2.copyFrom( node1 );
		assertThat( node1.getResource( "key1" ), is( "value1" ) );
		assertThat( node1.getResource( "key2" ), is( "value2" ) );
		assertThat( node2.getResource( "key1" ), is( "value1" ) );
		assertThat( node2.getResource( "key2" ), is( "valueB" ) );
	}

	@Test
	void testCopyFromWithOverwriteUsingResources() {
		Node node1 = new Node();
		node1.putResource( "key1", "value1" );
		node1.putResource( "key2", "value2" );

		Node node2 = new Node();
		node2.putResource( "key2", "valueB" );

		node2.copyFrom( node1, true );
		assertThat( node1.getResource( "key1" ), is( "value1" ) );
		assertThat( node1.getResource( "key2" ), is( "value2" ) );
		assertThat( node2.getResource( "key1" ), is( "value1" ) );
		assertThat( node2.getResource( "key2" ), is( "value2" ) );
	}

	@Test
	void testToString() {
		data.defineNaturalKey( "firstName", "lastName", "birthDate" );
		assertThat( data.toString(), is( "MockNode[]" ) );

		Date birthDate = new Date( 0 );
		data.setValue( "firstName", "Jane" );
		data.setValue( "birthDate", birthDate );
		assertThat( data.toString(), is( "MockNode[firstName=Jane,birthDate=" + birthDate.toString() + "]" ) );

		data.setValue( "lastName", "Doe" );
		assertThat( data.toString(), is( "MockNode[firstName=Jane,lastName=Doe,birthDate=" + birthDate.toString() + "]" ) );
	}

	@Test
	void testToStringWithSomeValues() {
		data.defineNaturalKey( "firstName", "lastName", "birthDate" );
		assertThat( data.toString(), is( "MockNode[]" ) );

		data.setValue( "firstName", "Jane" );
		data.setValue( "lastName", "Doe" );
		assertThat( data.toString("firstName"), is( "MockNode[firstName=Jane]" ) );
		assertThat( data.toString("lastName"), is( "MockNode[lastName=Doe]" ) );
	}

	@Test
	void testToStringWithAllValues() {
		assertThat( data.toString( true ), is( "MockNode[]" ) );

		data.setValue( "firstName", "Jane" );
		data.setValue( "lastName", "Doe" );
		assertThat( data.toString( true ), is( "MockNode[firstName=Jane,lastName=Doe]" ) );
	}

	@Test
	void testReadOnly() {
		data.setValue( "id", "123456789" );
		data.defineReadOnly( "id" );
		assertThat( data.isReadOnly( "id" ), is( true ) );
		assertThat( data.getValue( "id" ), is( "123456789" ) );

		try {
			data.setValue( "id", "987654321" );
			fail( "Should throw an IllegalStateException" );
		} catch( IllegalStateException exception ) {
			// Intentially ignore exception
		}
		assertThat( data.getValue( "id" ), is( "123456789" ) );
	}

	@Test
	void testHashCode() {
		data.defineNaturalKey( "firstName", "lastName", "birthDate" );
		assertThat( data.hashCode(), is( 0 ) );

		// Test the primary key
		data.setValue( "id", 2849234 );
		assertThat( data.hashCode(), is( 2849234 ) );

		// Test the natural key
		data.setValue( "lastName", "Doe" );
		assertThat( data.hashCode(), is( 2782408 ) );
	}

	@Test
	void testEquals() {
		MockNode data1 = new MockNode();
		data1.defineNaturalKey( "firstName", "lastName", "birthDate" );
		MockNode data2 = new MockNode();
		data2.defineNaturalKey( "firstName", "lastName", "birthDate" );
		assertThat( data1.equals( data2 ), is( true ) );

		// Test the primary key
		data1.setValue( "id", 2849234 );
		assertThat( data1.equals( data2 ), is( false ) );

		data2.setValue( "id", 2849234 );
		assertThat( data1.equals( data2 ), is( true ) );

		// Test the natural key
		data1.setValue( "lastName", "Doe" );
		assertThat( data1.equals( data2 ), is( false ) );

		data2.setValue( "lastName", "Doe" );
		assertThat( data1.equals( data2 ), is( true ) );
	}

	//	@Test
	//	void testLink() {
	//		Node target = new Node();
	//		Edge edge = data.add( target );
	//
	//		assertThat( edge.isDirected(), is( false ) );
	//		assertThat( data.getLinks(), contains( edge ) );
	//	}
	//
	//	@Test
	//	void testUnlink() {
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

	@SuppressWarnings( "unused" )
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

	private static Matcher<Node> modifiedFlag( Matcher<? super Boolean> matcher ) {
		return new FeatureMatcher<Node, Boolean>( matcher, "the modified flag", "modified" ) {

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

	@SuppressWarnings( "SameParameterValue" )
	private static void assertEventState( MockNode node, int index, EventType<? extends NodeEvent> type ) {
		assertThat( node.getWatcher().getEvents().get( index ), hasEventState( node, type ) );
	}

	private static void assertEventState(
		MockNode node, int index, EventType<? extends NodeEvent> type, String key, Object oldValue, Object newValue
	) {
		assertThat( node.getWatcher().getEvents().get( index ), hasEventState( node, type, key, oldValue, newValue ) );
	}

	@SuppressWarnings( "SameParameterValue" )
	private static void assertEventState(
		MockNode parent, int index, EventType<? extends NodeEvent> type, Node child, String key, Object oldValue, Object newValue
	) {
		assertThat( parent.getWatcher().getEvents().get( index ), hasEventState( parent, child, type, key, oldValue, newValue ) );
	}

	private static Matcher<NodeEvent> hasEventState( Node node, EventType<? extends NodeEvent> type ) {
		Matcher<NodeEvent> eventNode = eventNode( is( node ) );
		Matcher<NodeEvent> eventType = eventType( is( type ) );
		return allOf( eventNode, eventType );
	}

	private static Matcher<NodeEvent> hasEventState( Node node, EventType<? extends NodeEvent> type, String key, Object oldValue, Object newValue ) {
		Matcher<NodeEvent> eventNode = eventNode( is( node ) );
		Matcher<NodeEvent> eventType = eventType( is( type ) );
		Matcher<NodeEvent> eventKey = eventKey( is( key ) );
		Matcher<NodeEvent> eventOldValue = eventOldValue( is( oldValue ) );
		Matcher<NodeEvent> eventNewValue = eventNewValue( is( newValue ) );
		return allOf( eventNode, eventType, eventKey, eventOldValue, eventNewValue );
	}

	private static Matcher<NodeEvent> hasEventState( Node node, Node child, EventType<? extends NodeEvent> type, String key, Object oldValue, Object newValue ) {
		Matcher<NodeEvent> eventNode = eventNode( is( node ) );
		Matcher<NodeEvent> eventType = eventType( is( type ) );
		Matcher<NodeEvent> eventChild = eventChild( is( child ) );
		Matcher<NodeEvent> eventKey = eventKey( is( key ) );
		Matcher<NodeEvent> eventOldValue = eventOldValue( is( oldValue ) );
		Matcher<NodeEvent> eventNewValue = eventNewValue( is( newValue ) );
		return allOf( eventNode, eventChild, eventType, eventKey, eventOldValue, eventNewValue );
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

	private static Matcher<NodeEvent> eventType( Matcher<? super EventType<? extends NodeEvent>> matcher ) {
		return new FeatureMatcher<NodeEvent, EventType<? extends NodeEvent>>( matcher, "type", "type" ) {

			@Override
			protected EventType<? extends NodeEvent> featureValueOf( NodeEvent event ) {
				return event.getEventType();
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
