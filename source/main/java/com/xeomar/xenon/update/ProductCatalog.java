package com.xeomar.xenon.update;

import com.xeomar.settings.Settings;
import com.xeomar.util.Configurable;
import com.xeomar.util.XmlDescriptor;
import com.xeomar.xenon.util.UriUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ProductCatalog implements Configurable {

	private static final Logger log = LoggerFactory.getLogger( ProductCatalog.class );

	public static final String CATALOG_PATH = "/catalog";

	public static final String ICON_PATH = CATALOG_PATH + "/icon/@uri";

	public static final String NAME_PATH = CATALOG_PATH + "/name";

	public static final String SOURCES_PATH = CATALOG_PATH + "/sources/source";

	// When the attribute is in the root node there is no path info.
	private static final String SOURCE_PATH = "@uri";

	private String name;

	private URI iconUri;

	private URI sourceUri;

	private boolean enabled;

	private boolean removable;

	private Settings settings;

	private List<URI> sources = new CopyOnWriteArrayList<>();

	/*
	 * This constructor is required for the Settings API.
	 */
	public ProductCatalog() {}

	public ProductCatalog( XmlDescriptor descriptor, URI base ) {
		update( descriptor, base );
	}

	public ProductCatalog update( XmlDescriptor descriptor, URI base ) {
		String iconUri = descriptor.getValue( ICON_PATH );
		String name = descriptor.getValue( NAME_PATH );

		try {
			if( iconUri != null ) this.iconUri = UriUtil.resolve( base, new URI( iconUri ) );
		} catch( URISyntaxException exception ) {
			log.error( "Invalid icon uri: " + iconUri, exception );
		}
		this.name = name;

		this.sources.clear();
		for( Node node : descriptor.getNodes( SOURCES_PATH ) ) {
			String sourcePath = new XmlDescriptor( node ).getValue( SOURCE_PATH );
			try {
				URI uri = UriUtil.resolve( base, new URI( sourcePath ) );
				log.debug( "Adding catalog source: " + uri );
				this.sources.add( uri );
			} catch( URISyntaxException exception ) {
				log.error( "Invalid source path: " + sourcePath, exception );
			}
		}

		return this;
	}

	public URI getIconUri() {
		return iconUri;
	}

	public void setIconUri( URI uri ) {
		this.iconUri = uri;
	}

	public String getName() {
		return name;
	}

	public void setName( String name ) {
		this.name = name;
		saveSettings();
	}

	public URI getSourceUri() {
		return sourceUri;
	}

	public void setSourceUri( URI uri ) {
		this.sourceUri = uri;
		saveSettings();
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled( boolean enabled ) {
		this.enabled = enabled;
		saveSettings();
	}

	public boolean isRemovable() {
		return removable;
	}

	public void setRemovable( boolean removable ) {
		this.removable = removable;
		saveSettings();
	}

	public List<URI> getSources() {
		return new ArrayList<>( sources );
	}

	public void setSources( List<URI> sources ) {
		this.sources.clear();
		this.sources.addAll( sources );
		saveSettings();
	}

	@Override
	public void setSettings( Settings settings ) {
		if( settings == null || this.settings != null ) return;

		this.settings = settings;

		name = settings.get( "name", null );
		enabled = settings.getBoolean( "enabled", true );
		removable = settings.getBoolean( "removable", true );
		String iconUriString = settings.get( "iconUri", null );
		String sourceUriString = settings.get( "sourceUri", null );

		if( iconUriString != null ) {
			try {
				iconUri = URI.create( iconUriString );
			} catch( Throwable throwable ) {
				log.error( "Invalid icon uri: " + iconUriString, throwable );
			}
		}

		if( sourceUriString != null ) {
			try {
				sourceUri = URI.create( sourceUriString );
			} catch( Throwable throwable ) {
				log.error( "Invalid source uri: " + sourceUriString, throwable );
			}
		}
	}

	@Override
	public Settings getSettings() {
		return settings;
	}

	private void saveSettings() {
		settings.set( "name", name );
		settings.set( "enabled", enabled );
		settings.set( "removable", removable );
		settings.set( "iconUri", iconUri == null ? null : iconUri.toString() );
		settings.set( "sourceUri", sourceUri == null ? null : sourceUri.toString() );
	}

}
