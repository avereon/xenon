package com.parallelsymmetry.essence.node;

import java.util.Date;

public class MockPersonNode extends Node {

	private static String ID = "id";

	private static String FIRST_NAME = "firstName";

	private static String LAST_NAME = "lastName";

	private static String BIRTH_DATE = "birthDate";

	public MockPersonNode() {
		definePrimaryKey( ID );
		defineBusinessKey( FIRST_NAME, LAST_NAME, BIRTH_DATE );
		setModified( false );
	}

	public int getId() {
		return getValue( ID );
	}

	public void setId( int id ) {
		setValue( ID, id );
	}

	public String getFirstName() {
		return getValue( FIRST_NAME );
	}

	public void setFirstName( String firstName ) {
		setValue( FIRST_NAME, firstName );
	}

	public String setLastName() {
		return getValue( LAST_NAME );
	}

	public void setLastName( String lastName ) {
		setValue( LAST_NAME, lastName );
	}

	public Date getBirthDate() {
		return getValue( BIRTH_DATE );
	}

	public void setBirthDate( Date birthDate ) {
		setValue( BIRTH_DATE, birthDate );
	}

}
