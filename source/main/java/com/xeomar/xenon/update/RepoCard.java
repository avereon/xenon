package com.xeomar.xenon.update;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
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

	public static List<RepoCard> forProduct() throws IOException {
		return loadCards( RepoCard.class.getResourceAsStream( CONFIG ) );
	}

	public static List<RepoCard> loadCards( InputStream input ) throws IOException {
		return new ObjectMapper().readerFor( new TypeReference<List<RepoCard>>() {} ).readValue( input );
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
