package com.xeomar.xenon.update;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

public class CatalogCard {

	public static final String FILE = "catalog.card";

	private RepoCard repo;

	private Set<String> products = new HashSet<>();

	public CatalogCard( RepoCard repo ) {
		this.repo = repo;
	}

	public RepoCard getRepo() {
		return repo;
	}

	public void setRepo( RepoCard repo ) {
		this.repo = repo;
	}

	public Set<String> getProducts() {
		return products;
	}

	public void setProducts( Set<String> products ) {
		this.products = products == null ? Set.of() : new HashSet<>( products );
	}

	public static CatalogCard load( RepoCard repo, InputStream input ) throws IOException {
		CatalogCard catalog = new ObjectMapper().readerFor( new TypeReference<CatalogCard>() {} ).readValue( input );
		catalog.setRepo( repo );
		return catalog;
	}

}
