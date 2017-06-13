package com.parallelsymmetry.essence.node;

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
	public void testModifiedFlag() {
		MockPersonNode person = new MockPersonNode();
		assertThat( person.isModified(), is( false ) );

		person.setId( 423984 );
		assertThat( person.isModified(), is( true ));
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

}
