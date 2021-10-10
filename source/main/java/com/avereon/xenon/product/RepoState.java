package com.avereon.xenon.product;

import com.avereon.product.RepoCard;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@JsonIgnoreProperties( ignoreUnknown = true )
public class RepoState extends RepoCard {

	private boolean enabled = true;

	private boolean removable = true;

	private int rank;

	public RepoState() {}

	public RepoState( String url ) {
		super( url );
	}

	RepoState( RepoCard card ) {
		copyFrom( card );
	}

	@Override
	public RepoState setUrl( String url ) {
		super.setUrl( url );
		return this;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public RepoState setEnabled( boolean enabled ) {
		this.enabled = enabled;
		return this;
	}

	public boolean isRemovable() {
		return removable;
	}

	public RepoState setRemovable( boolean removable ) {
		this.removable = removable;
		return this;
	}

	public int getRank() {
		return rank;
	}

	public RepoState setRank( int rank ) {
		this.rank = rank;
		return this;
	}

	public RepoState copyFrom( RepoState state ) {
		if( state == null ) return null;
		super.copyFrom( state );
		this.enabled = state.enabled;
		this.removable = state.removable;
		this.rank = state.rank;
		return this;
	}

	public static List<RepoState> forProduct( Class<?> source ) throws IOException {
		try( InputStream input = source.getResourceAsStream( CONFIG ) ) {
			return loadCards( input );
		}
	}

	public static List<RepoState> loadCards( InputStream input ) throws IOException {
		return new ObjectMapper().readerFor( new TypeReference<List<RepoState>>() {} ).readValue( input );
	}

}
