package com.xeomar.xenon.update;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.List;

public class CatalogCard {

	public static final String CARD = "/META-INF/catalog.card";

	private static final Logger log = LoggerFactory.getLogger( CatalogCard.class );

	private String name;

	private URI icon;

	private URI source;

	private boolean enabled;

	private boolean removable;

	private List<URI> products;

	public String getName() {
		return name;
	}

	public void setName( String name ) {
		this.name = name;
	}

	public URI getIcon() {
		return icon;
	}

	public void setIcon( URI icon ) {
		this.icon = icon;
	}

	public URI getSource() {
		return source;
	}

	public void setSource( URI source ) {
		this.source = source;
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

	public List<URI> getProducts() {
		return products;
	}

	public void setProducts( List<URI> products ) {
		this.products = products;
	}

}
