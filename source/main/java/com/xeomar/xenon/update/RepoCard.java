package com.xeomar.xenon.update;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/*
The catalog.card file, and therefore this class, server two purposes but
probably should not. One purpose is to specify the repository location of the
market/store/repo. The other purpose is to specify the products available at
the market/store/repo.

This dual purpose may simply be a historical artifact of how the original repo
was set up and operated. It provided the initial repo configuration for the
program and was a reasonable repo index at the same time.
 */
@JsonIgnoreProperties( ignoreUnknown = true )
public class RepoCard {

	public static final String CONFIG = "/META-INF/repositories.json";

	private String name;

	private String icon;

	private String repo;

	private boolean enabled;

	private boolean removable;

	private int rank;

	private Set<String> products = new HashSet<>();

	public RepoCard() {}

	public RepoCard( String repo ) {
		this.repo = repo;
	}

	public static List<RepoCard> forProduct() throws IOException {
		return loadCards( RepoCard.class.getResourceAsStream( CONFIG ) );
	}

	public static List<RepoCard> loadCards( InputStream input ) throws IOException {
		return new ObjectMapper().readerFor( new TypeReference<List<RepoCard>>() {} ).readValue( input );
	}

	//	public RepoCard copyFrom( RepoCard card ) {
	//		return updateWith( card, null );
	//	}
	//
	//	public RepoCard updateWith( RepoCard card, URI source ) {
	//		this.name = card.name;
	//		this.iconUri = card.iconUri;
	//		this.cardUri = card.cardUri;
	//		this.enabled = card.enabled;
	//		this.removable = card.removable;
	//		this.products = card.products;
	//
	//		if( source != null ) this.cardUri = source.toString();
	//
	//		return this;
	//	}

	public String getName() {
		return name;
	}

	public void setName( String name ) {
		this.name = name;
	}

	public String getIcon() {
		return icon;
	}

	public void setIcon( String icon ) {
		this.icon = icon;
	}

	public String getRepo() {
		return repo;
	}

	public void setRepo( String repo ) {
		this.repo = repo;
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

	public int getRank() {
		return rank;
	}

	public void setRank( int rank ) {
		this.rank = rank;
	}

	//	public Set<String> getProducts() {
//		return products;
//	}
//
//	public void setProducts( Set<String> products ) {
//		this.products = products == null ? Set.of() : new HashSet<>( products );
//	}

	@Override
	public String toString() {
		return repo;
	}

	@Override
	public boolean equals( Object object ) {
		if( this == object ) return true;
		if( !(object instanceof RepoCard) ) return false;
		RepoCard that = (RepoCard)object;
		return Objects.equals( this.repo, that.repo );
	}

	@Override
	public int hashCode() {
		return Objects.hash( repo );
	}

}
