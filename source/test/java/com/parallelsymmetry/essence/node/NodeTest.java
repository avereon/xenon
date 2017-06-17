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
	public void testModifiedFlag() {
		MockNode data = new MockNode();
		NodeWatcher watcher = new NodeWatcher();
		data.addNodeListener( watcher );

		assertThat( data.getModifiedValueCount(), is( 0 ) );
		assertThat( data.isModified(), is( false ) );
		assertThat( watcher, hasEventCounts( 0, 0, 0 ) );

		data.setModified( true );
		assertThat( data.getModifiedValueCount(), is( 0 ) );
		assertThat( data.isModified(), is( true ) );
		assertThat( watcher, hasEventCounts( 0, 1, 1 ) );

		//		data.setModified( false );
		//		assertEquals( 0, data.getModifiedAttributeCount() );
		//		assertFalse( data.isModified() );
		//		assertEventCounts( handler, 2, 2, 0 );
	}

	@Test
	public void testModified() {
		MockPersonNode person = new MockPersonNode();
		assertThat( person.isModified(), is( false ) );

		person.setId( 423984 );
		assertThat( person.isModified(), is( true ) );

		person.setModified( false );
		assertThat( person.isModified(), is( false ) );
	}

	@Test
	public void testToString() {
		MockPersonNode person = new MockPersonNode();
		assertThat( person.toString(), is( "MockPersonNode[]" ) );

		person.setFirstName( "Jane" );
		person.setBirthDate( new Date( 0 ) );
		assertThat( person.toString(), is( "MockPersonNode[firstName=Jane,birthDate=Wed Dec 31 17:00:00 MST 1969]" ) );

		person.setLastName( "Doe" );
		assertThat( person.toString(), is( "MockPersonNode[firstName=Jane,lastName=Doe,birthDate=Wed Dec 31 17:00:00 MST 1969]" ) );
	}

	@Test
	public void testToStringWithAllValues() {
		MockPersonNode person = new MockPersonNode();
		assertThat( person.toString( true ), is( "MockPersonNode[]" ) );
	}

	@Test
	public void testHashCode() {
		MockPersonNode person = new MockPersonNode();
		assertThat( person.hashCode(), is( 0 ) );

		// Test the primary key
		person.setId( 2849234 );
		assertThat( person.hashCode(), is( 2849234 ) );

		// Test the business key
		person.setLastName( "Doe" );
		assertThat( person.hashCode(), is( 2782408 ) );
	}

	@Test
	public void testEquals() {
		MockPersonNode person1 = new MockPersonNode();
		MockPersonNode person2 = new MockPersonNode();
		assertThat( person1.equals( person2 ), is( true ) );

		// Test the primary key
		person1.setId( 2849234 );
		assertThat( person1.equals( person2 ), is( false ) );

		person2.setId( 2849234 );
		assertThat( person1.equals( person2 ), is( true ) );

		// Test the business key
		person1.setLastName( "Doe" );
		assertThat( person1.equals( person2 ), is( false ) );

		person2.setLastName( "Doe" );
		assertThat( person1.equals( person2 ), is( true ) );
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

	private static Matcher<NodeWatcher> hasEventCounts( int value, int flag, int node ) {
		Matcher<NodeWatcher> valueEventCounter = eventCount( NodeEvent.Type.VALUE_CHANGED, is( value ) );
		Matcher<NodeWatcher> flagEventCounter = eventCount( NodeEvent.Type.FLAG_CHANGED, is( flag ) );
		Matcher<NodeWatcher> nodeEventCounter = eventCount( NodeEvent.Type.NODE_CHANGED, is( node ) );
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
