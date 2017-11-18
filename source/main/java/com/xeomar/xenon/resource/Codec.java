package com.xeomar.xenon.resource;

import com.xeomar.util.TextUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public abstract class Codec {

	private static final Logger log = LoggerFactory.getLogger( Codec.class );

	private ResourceType type;

	/**
	 * The supported <a href="https://en.wikipedia.org/wiki/Media_type">media types</a> for this codec.
	 */
	private Set<String> supportedMediaTypes;

	private Set<String> supportedFileNames;

	private Set<String> supportedFirstLines;

	public abstract String getKey();

	public abstract String getName();

	public abstract boolean canLoad();

	public abstract boolean canSave();

	public abstract void load( Resource resource, InputStream input ) throws IOException;

	public abstract void save( Resource resource, OutputStream output ) throws IOException;

	public ResourceType getResourceType() {
		return type;
	}

	public void setResourceType( ResourceType type ) {
		this.type = type;
	}

	protected void addSupportedExtension( String extension ) {
		addSupportedFileName( "^.*\\." + extension + "$" );
	}

	/**
	 * Add supported media type.
	 *
	 * @param type The media type
	 */
	public void addSupportedMediaType( String type ) {
		if( supportedMediaTypes == null ) supportedMediaTypes = new CopyOnWriteArraySet<>();
		supportedMediaTypes.add( type );
	}

	/**
	 * Add supported file name regular expression pattern.
	 *
	 * @param pattern The file name regular expression pattern
	 */
	protected void addSupportedFileName( String pattern ) {
		if( supportedFileNames == null ) supportedFileNames = new CopyOnWriteArraySet<>();
		supportedFileNames.add( pattern );
	}

	/**
	 * Add supported first line pattern.
	 *
	 * @param pattern The first line regular expression pattern
	 */
	protected void addSupportedFirstLine( String pattern ) {
		if( supportedFirstLines == null ) supportedFirstLines = new CopyOnWriteArraySet<>();
		supportedFirstLines.add( pattern );
	}

	/**
	 * A set of strings that identify the supported media types.
	 *
	 * @return The set of supported media types
	 */
	public Set<String> getSupportedMediaTypes() {
		return supportedMediaTypes == null ? new HashSet<>() : Collections.unmodifiableSet( supportedMediaTypes );
	}

	/**
	 * A set of strings that identify the supported file names in regular
	 * expression format.
	 *
	 * @return The set of supported file name patterns
	 */
	public Set<String> getSupportedFileNames() {
		return supportedFileNames == null ? new HashSet<>() : Collections.unmodifiableSet( supportedFileNames );
	}

	/**
	 * A set of strings that identify the supported first line patterns.
	 *
	 * @return The set of supported first line patterns
	 */
	public Set<String> getSupportedFirstLines() {
		return supportedFirstLines == null ? new HashSet<>() : Collections.unmodifiableSet( supportedFirstLines );
	}

	public boolean isSupportedMediaType( String type ) {
		if( TextUtil.isEmpty( type ) ) return false;
		for( String pattern : getSupportedMediaTypes() ) {
			boolean matches = pattern.equals( type );
			log.debug( "Type [" + type + "] matches [" + pattern + "]: " + matches );
			if( matches ) return true;
		}
		return false;
	}

	public boolean isSupportedFileName( String name ) {
		if( TextUtil.isEmpty( name ) ) return false;
		for( String pattern : getSupportedFileNames() ) {
			boolean matches = name.matches( pattern );
			log.debug( "Name [" + name + "] matches [" + pattern + "]: " + matches );
			if( matches ) return true;
		}
		return false;
	}

	public boolean isSupportedFirstLine( String line ) {
		if( TextUtil.isEmpty( line ) ) return false;
		for( String pattern : getSupportedFirstLines() ) {
			//boolean matches = line.matches( pattern );
			boolean matches = line.startsWith( pattern );
			log.debug( "Line [" + line + "] matches [" + pattern + "]: " + matches );
			if( matches ) return true;
		}
		return false;
	}

	public int getPriority() {
		return 0;
	}

	@Override
	public int hashCode() {
		return getKey().hashCode();
	}

	@Override
	public boolean equals( Object object ) {
		if( !(object instanceof Codec) ) return false;
		Codec that = (Codec)object;
		return this.getKey().equals( that.getKey() );
	}

	@Override
	public String toString() {
		return getName();
	}

}
