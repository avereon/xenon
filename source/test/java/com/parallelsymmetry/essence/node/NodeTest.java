package com.parallelsymmetry.essence.node;

import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class NodeTest {

	private Node node;

	@Before
	public void setup() throws Exception {
		node = new Node();
	}

	@Test
	public void testNewNodeModifiedState() {
		MockNode node = new MockNode();
		assertThat( node.isModified(), is( false ) );
	}

	@Test
	public void testModifyByFlagAndUnmodifyByFlag() {
		MockNode data = new MockNode();
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
		MockNode data = new MockNode();
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
		MockNode data = new MockNode();
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

		MockNode node = new MockNode();
		assertThat( node.getValue( key ), is( nullValue() ) );

		node.setValue( key, value );
		assertThat( node.getValue( key ), is( value ) );

		node.setValue( key, null );
		assertThat( node.getValue( key ), is( nullValue() ) );
	}

	@Test
	public void testObjectAttribute() {
		String key = "key";
		Object value = new Object();

		MockNode node = new MockNode();
		node.setValue( key, value );

		assertThat( node.getValue( key ), is( value ) );
	}

	@Test
	public void testStringAttribute() {
		String key = "key";
		String value = "value";

		MockNode node = new MockNode();
		node.setValue( key, value );

		assertThat( node.getValue( key ), is( value ) );
	}

	@Test
	public void testBooleanAttribute() {
		String key = "key";
		boolean value = true;

		MockNode node = new MockNode();
		node.setValue( key, value );

		assertThat( node.getValue( key ), is( value ) );
	}

	@Test
	public void testIntegerAttribute() {
		String key = "key";
		int value = 0;

		MockNode node = new MockNode();
		node.setValue( key, value );

		assertThat( node.getValue( key ), is( value ) );
	}

	@Test
	public void testSetNullAttributeToNull() {
		MockNode data = new MockNode();
		NodeWatcher watcher = new NodeWatcher();
		data.addNodeListener( watcher );

		data.setValue( "attribute", null );
		assertThat( watcher, hasEventCounts( 0, 0, 0 ) );
	}

	@Test
	public void testSetAttributeWithNullName() {
		MockNode data = new MockNode();
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
		MockNode data = new MockNode();

		try {
			data.getValue( null );
			Assert.fail( "Null attribute names are not allowed." );
		} catch( NullPointerException exception ) {
			assertThat( exception.getMessage(), is( "Value key cannot be null" ) );
		}
	}

	@Test
	public void testNullAttributeValues() {
		MockNode data = new MockNode();
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
		MockNode data = new MockNode();
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
		MockNode data = new MockNode();
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
	public void testUnmodifiedByUnsetAttribute() {
		MockNode data = new MockNode();
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
		MockNode data = new MockNode();
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
		MockNode data = new MockNode();

		assertThat( data.getFlag( key ), is( false ) );
		data.setFlag( key, true );
		assertThat( data.getFlag( key ), is( true ) );
		data.setFlag( key, false );
		assertThat( data.getFlag( key ), is( false ) );
	}

	@Test
	public void testSetNullMetaValueToFalse() {
		MockNode data = new MockNode();
		NodeWatcher watcher = new NodeWatcher();
		data.addNodeListener( watcher );

		data.setFlag( "attribute", false );
		assertThat( watcher, hasEventCounts( 0, 0, 0 ) );
	}

	@Test
	public void testSetMetaValueWithNullName() {
		MockNode data = new MockNode();
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
		MockNode data = new MockNode();
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
		MockNode data = new MockNode();
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
		MockNode data = new MockNode();
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
		assertEventState( watcher, index++, data, NodeEvent.Type.VALUE_CHANGED, "attribute", null, "value0" );
		assertEventState( watcher, index++, data, NodeEvent.Type.FLAG_CHANGED, Node.MODIFIED, false, true );
		assertEventState( watcher, index++, data, NodeEvent.Type.NODE_CHANGED );
		assertEventState( watcher, index++, data, NodeEvent.Type.VALUE_CHANGED, "attribute", "value0", "value1" );
		assertEventState( watcher, index++, data, NodeEvent.Type.NODE_CHANGED );
		assertEventState( watcher, index++, data, NodeEvent.Type.VALUE_CHANGED, "attribute", "value1", null );
		assertEventState( watcher, index++, data, NodeEvent.Type.FLAG_CHANGED, Node.MODIFIED, true, false );
		assertEventState( watcher, index++, data, NodeEvent.Type.NODE_CHANGED );
		assertThat( watcher.getEvents().size(), is( index ));
	}

	// NEXT Continue implementing tests

	@Test
	public void testToString() {
		MockNode data = new MockNode();
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
		MockNode data = new MockNode();
		assertThat( data.toString( true ), is( "MockNode[]" ) );

		data.setValue( "firstName", "Jane" );
		data.setValue( "lastName", "Doe" );
		assertThat( data.toString( true ), is( "MockNode[firstName=Jane,lastName=Doe]" ) );
	}

	@Test
	public void testHashCode() {
		MockNode data = new MockNode();
		data.definePrimaryKey( "id" );
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
		data1.definePrimaryKey( "id" );
		data1.defineBusinessKey( "firstName", "lastName", "birthDate" );
		MockNode data2 = new MockNode();
		data2.definePrimaryKey( "id" );
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

	@Test
	public void testLink() {
		Node target = new Node();
		Edge edge = node.add( target );

		assertThat( edge.isDirected(), is( false ) );
		assertThat( node.getLinks(), contains( edge ) );
	}

	@Test
	public void testUnlink() {
		Node target = new Node();
		Edge edge = node.add( target );

		assertThat( edge.isDirected(), is( false ) );
		assertThat( node.getLinks(), contains( edge ) );

		node.remove( edge.getTarget() );

		assertThat( node.getLinks(), not( contains( edge ) ) );
	}

	private void listEvents( NodeWatcher watcher ) {
		for( NodeEvent event : watcher.getEvents() ) {
			System.out.println( "Event: " + event );
		}
	}

	private static Matcher<Node> hasStates( boolean modified, int modifiedValueCount ) {
		Matcher<Node> modifiedMatcher = flag( Node.MODIFIED, is( modified ) );
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

	private static void assertEventState( NodeWatcher watcher, int index, Node node, NodeEvent.Type type ) {
		assertThat( watcher.getEvents().get( index ), hasEventState( node, type ) );
	}

	private static void assertEventState( NodeWatcher watcher, int index, Node node, NodeEvent.Type type, String key, Object oldValue, Object newValue ) {
		assertThat( watcher.getEvents().get( index ), hasEventState( node, type, key, oldValue, newValue ) );
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

	private static Matcher<NodeEvent> eventNode( Matcher<? super Node> matcher ) {
		return new FeatureMatcher<NodeEvent, Node>( matcher, "node", "node" ) {

			@Override
			protected Node featureValueOf( NodeEvent event ) {
				return event.getNode();
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
