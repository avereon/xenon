package com.parallelsymmetry.essence.person;

import java.util.ArrayList;
import java.util.List;

// TODO Use Lombok when it is supported in Java 9
public class Contributor extends Person {

	private String organization;

	private String organizationUrl;

	private List<String> roles = new ArrayList<>();

	public String getOrganization() {
		return organization;
	}

	public void setOrganization( String organization ) {
		this.organization = organization;
	}

	public String getOrganizationUrl() {
		return organizationUrl;
	}

	public void setOrganizationUrl( String organizationUrl ) {
		this.organizationUrl = organizationUrl;
	}

	public List<String> getRoles() {
		return roles;
	}

	public void setRoles( List<String> roles ) {
		this.roles = roles;
	}

}
