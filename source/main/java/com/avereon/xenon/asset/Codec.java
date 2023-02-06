package com.avereon.xenon.asset;

import com.avereon.util.TextUtil;
import lombok.CustomLog;

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
public abstract class Codec implements Predicate<Asset> {

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

	private AssetType assetType;

	private final Map<Pattern, Set<String>> supportedMatches;

	private String defaultExtension;

	public Codec() {
		supportedMatches = new ConcurrentHashMap<>();
	}

	public abstract String getKey();

	public abstract String getName();

	public abstract boolean canLoad();

	public abstract boolean canSave();

	public abstract void load( Asset asset, InputStream input ) throws IOException;

	public abstract void save( Asset asset, OutputStream output ) throws IOException;

	public AssetType getAssetType() {
		return assetType;
	}

	public void setAssetType( AssetType type ) {
		this.assetType = type;
	}

	public final String getDefaultExtension() {
		return defaultExtension;
	}

	public final void setDefaultExtension( String extension ) {
		addSupported( Pattern.EXTENSION, extension );
		this.defaultExtension = extension;
	}

	public final void addSupported( Pattern type, String pattern ) {
		supportedMatches.computeIfAbsent( type, ( k ) -> new CopyOnWriteArraySet<>() ).add( pattern );
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

	public final boolean isSupported( Asset asset ) {
		return supportedMatches.keySet().stream().anyMatch( k -> isSupported( k, asset.getFileName() ) );
	}

	public int getPriority() {
		return 0;
	}

	@Override
	public final boolean test( Asset asset ) {
		return isSupported( asset );
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

	public record Association(Pattern pattern, String value) {}

}
