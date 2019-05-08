package com.xeomar.xenon.update;

import java.util.Objects;

public class RepoCard {

	public static final String CONFIG = "/META-INF/repositories.json";

	private String name;

	private String repo;

	private String icon;

	private boolean enabled;

	private boolean removable;

	public String getName() {
		return name;
	}

	public void setName( String name ) {
		this.name = name;
	}

	public String getRepo() {
		return repo;
	}

	public void setRepo( String repo ) {
		this.repo = repo;
	}

	public String getIcon() {
		return icon;
	}

	public void setIcon( String icon ) {
		this.icon = icon;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled( boolean enabled ) {
		this.enabled = enabled;
	}

	public boolean isRemovable() {
		return removable;
	}

	public void setRemovable( boolean removable ) {
		this.removable = removable;
	}

	@Override
	public boolean equals( Object object ) {
		if( this == object ) return true;
		if( object == null || getClass() != object.getClass() ) return false;
		RepoCard repoCard = (RepoCard)object;
		return repo.equals( repoCard.repo );
	}

	@Override
	public int hashCode() {
		return Objects.hash( repo );
	}
}
