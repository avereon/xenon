package com.parallelsymmetry.essence.node;

import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
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
		assertThat( data.isModified(), is( false ) );
		assertThat( watcher, hasEventCounts( 0, 0, 0 ) );

		data.setValue( "id", 423984 );
		assertThat( data.isModified(), is( true ) );
		//assertThat( watcher, hasEventCounts( 1, 1, 1 ) );

		data.setModified( false );
		assertThat( data.isModified(), is( false ) );
		//assertThat( watcher, hasEventCounts( 2, 2, 1 ) );
	}

	@Test
	public void testModifyByValueAndUnmodifyByValue() {
		MockNode data = new MockNode();
		NodeWatcher watcher = new NodeWatcher();
		data.addNodeListener( watcher );
		assertThat( data.isModified(), is( false ) );
		assertThat( watcher, hasEventCounts( 0, 0, 0 ) );

		data.setValue( "id", 423984 );
		assertThat( data.isModified(), is( true ) );
		//assertThat( watcher, hasEventCounts( 1, 1, 1 ) );

		data.setValue( "id", null );
		assertThat( data.isModified(), is( false ) );
		//assertThat( watcher, hasEventCounts( 2, 2, 1 ) );
	}

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

	private static Matcher<NodeWatcher> hasEventCounts( int node, int flag, int value ) {
		Matcher<NodeWatcher> nodeEventCounter = eventCount( NodeEvent.Type.NODE_CHANGED, is( node ) );
		Matcher<NodeWatcher> valueEventCounter = eventCount( NodeEvent.Type.VALUE_CHANGED, is( value ) );
		Matcher<NodeWatcher> flagEventCounter = eventCount( NodeEvent.Type.FLAG_CHANGED, is( flag ) );
		return allOf( valueEventCounter, flagEventCounter, nodeEventCounter );
	}

	private static Matcher<NodeWatcher> eventCount( NodeEvent.Type type, Matcher<? super Integer> matcher ) {
		return new FeatureMatcher<NodeWatcher, Integer>( matcher, type + " event count", "count" ) {

			@Override
			protected Integer featureValueOf( NodeWatcher actual ) {
				int count = 0;
				for( NodeEvent event : actual.getEvents() ) {
					if( event.getType() == type ) count++;
				}

				return count;
			}

		};
	}

}
