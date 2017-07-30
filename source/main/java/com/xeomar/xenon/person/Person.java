package com.xeomar.xenon.person;

// TODO Use Lombok when it is supported in Java 9
public class Person {

	private String name;

	private String email;

	private String timezone;

	public String getName() {
		return name;
	}

	public void setName( String name ) {
		this.name = name;
	}

	@Override
	public String toString() {
		return name;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail( String email ) {
		this.email = email;
	}

	public String getTimezone() {
		return timezone;
	}

	public void setTimezone( String timezone ) {
		this.timezone = timezone;
	}
}
