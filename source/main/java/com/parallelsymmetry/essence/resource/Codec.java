package com.parallelsymmetry.essence.resource;

import com.parallelsymmetry.essence.FileUtil;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public abstract class Codec {

	private ResourceType type;

	private Set<String> supportedFileNames;

	private Set<String> supportedFirstLines;

	/**
	 * The supported <a href="https://en.wikipedia.org/wiki/Media_type">media types</a> for this codec.
	 */
	private Set<String> supportedMediaTypes;

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
		addSupportedFileName( FileUtil.globToRE( "*." + extension ) );
	}

	protected void addSupportedFileName( String pattern ) {
		if( supportedFileNames == null ) supportedFileNames = new CopyOnWriteArraySet<>();
		supportedFileNames.add( pattern );
	}

	protected void addSupportedFirstLine( String line ) {
		if( supportedFirstLines == null ) supportedFirstLines = new CopyOnWriteArraySet<>();
		supportedFirstLines.add( line );
	}

	public void addSupportedMediaType( String type ) {
		if( supportedMediaTypes == null ) supportedMediaTypes = new CopyOnWriteArraySet<>();
		supportedMediaTypes.add( type );
	}

	/**
	 * A set of strings that identify the supported file names in regular
	 * expression format.
	 *
	 * @return
	 */
	public Set<String> getSupportedFileNames() {
		return supportedFileNames == null ? new HashSet<>() : Collections.unmodifiableSet( supportedFileNames );
	}

	public Set<String> getSupportedFirstLines() {
		return supportedFirstLines == null ? new HashSet<>() : Collections.unmodifiableSet( supportedFirstLines );
	}

	public Set<String> getSupportedMediaTypes() {
		return supportedMediaTypes == null ? new HashSet<>() : Collections.unmodifiableSet( supportedMediaTypes );
	}

	public boolean isSupportedFileName( String name ) {
		Set<String> supportedFileNames = getSupportedFileNames();
		if( StringUtils.isEmpty( name ) ) return false;
		for( String pattern : supportedFileNames ) {
			if( name.matches( pattern ) ) return true;
		}
		return false;
	}

	public boolean isSupportedFirstLine( String line ) {
		Set<String> supportedFirstLines = getSupportedFirstLines();
		if( StringUtils.isEmpty( line ) ) return false;
		for( String supportedFirstLine : supportedFirstLines ) {
			if( line.startsWith( supportedFirstLine ) ) return true;
		}
		return false;
	}

	public boolean isSupportedMediaType( String type ) {
		Set<String> supportedMediaTypes = getSupportedMediaTypes();
		if( StringUtils.isEmpty( type ) ) return false;
		return supportedMediaTypes.contains( type );
	}

	public int getPriority() {
		return 0;
	}

	@Override
	public String toString() {
		return getName();
	}

}
