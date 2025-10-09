package com.avereon.xenon.resource;

import com.avereon.util.TextUtil;
import lombok.CustomLog;
import lombok.Getter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Predicate;

@CustomLog
public abstract class Codec implements Predicate<Resource> {

	public enum Pattern {
		MEDIATYPE {
			boolean accept( String pattern, String value ) {
				return value.equals( pattern );
			}
		},
		EXTENSION {
			boolean accept( String pattern, String value ) {
				return value.endsWith( "." + pattern );
			}
		},
		FILENAME {
			boolean accept( String pattern, String value ) {
				return value.matches( pattern );
			}
		},
		FIRSTLINE {
			boolean accept( String pattern, String value ) {
				return value.startsWith( pattern );
			}
		},
		SCHEME {
			boolean accept( String pattern, String value ) {
				return value.startsWith( pattern + ":" );
			}
		},
		URI {
			boolean accept( String pattern, String value ) {
				return value.startsWith( pattern );
			}
		};

		abstract boolean accept( String pattern, String value );
	}

	@Getter
	private ResourceType resourceType;

	private final Map<Pattern, Set<String>> supportedMatches;

	private String defaultExtension;

	protected Codec() {
		supportedMatches = new ConcurrentHashMap<>();
	}

	public abstract String getKey();

	public abstract String getName();

	public abstract boolean canLoad();

	public abstract boolean canSave();

	public abstract void load( Resource resource, InputStream input ) throws IOException;

	public abstract void save( Resource resource, OutputStream output ) throws IOException;

	public void setResourceType( ResourceType type ) {
		this.resourceType = type;
	}

	public final String getDefaultExtension() {
		return defaultExtension;
	}

	public final void setDefaultExtension( String extension ) {
		addSupported( Pattern.EXTENSION, extension );
		this.defaultExtension = extension;
	}

	public final void addSupported( Pattern type, String pattern ) {
		supportedMatches.computeIfAbsent( type, k -> new CopyOnWriteArraySet<>() ).add( pattern );
	}

	public final Set<String> getSupported( Pattern type ) {
		return Collections.unmodifiableSet( supportedMatches.getOrDefault( type, Set.of() ) );
	}

	public final Set<Association> getAssociations() {
		Set<Association> associations = new HashSet<>();

		for( Pattern pattern : Pattern.values() ) {
			for( String value : getSupported( pattern ) ) {
				associations.add( new Association( pattern, value ) );
			}
		}

		return associations;
	}

	public final boolean isSupported( Pattern type, String value ) {
		if( TextUtil.isEmpty( value ) ) return false;
		return supportedMatches.getOrDefault( type, Set.of() ).stream().anyMatch( p -> type.accept( p, value ) );
	}

	public final boolean isSupported( Resource resource ) {
		return supportedMatches.keySet().stream().anyMatch( k -> isSupported( k, resource.getFileName() ) );
	}

	public int getPriority() {
		return 0;
	}

	@Override
	public final boolean test( Resource resource ) {
		return isSupported( resource );
	}

	@Override
	public int hashCode() {
		return getKey().hashCode();
	}

	@Override
	public boolean equals( Object object ) {
		if( !(object instanceof Codec that) ) return false;
		return this.getKey().equals( that.getKey() );
	}

	@Override
	public String toString() {
		return getName();
	}

	public record Association(Pattern pattern, String value) {}

}
