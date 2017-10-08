package com.xeomar.xenon.settings;

import com.xeomar.xenon.util.Paths;
import com.xeomar.xenon.util.TextUtil;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class MapSettings extends AbstractSettings {

	// The map of settings. Should only be stored in the root node
	private Map<String, Settings> settings;

	private MapSettings root;

	private String path;

	// The settings node values
	private Map<String, String> values;

	// The settings defaults.
	private Map<String, String> defaultValues;

	public MapSettings() {
		this( null, "/", new HashMap<>() );
	}

	private MapSettings( MapSettings root, String path, Map<String, String> values ) {
		if( root == null ) {
			this.settings = new ConcurrentHashMap<>();
			this.root = this;
		} else {
			this.root = root;
		}
		this.path = path;
		this.values = new ConcurrentHashMap<>();
		if( values != null ) this.values.putAll( values );
		this.root.settings.put( path, this );
	}

	@Override
	public String getPath() {
		return path;
	}

	@Override
	public Settings getChild( String path ) {
		return getChild( path, null );
	}

	@Override
	public Settings getChild( String path, Map<String, String> values ) {
		// Resolve the path
		String childPath = Paths.isAbsolute( path ) ? path : Paths.resolve( this.path, path );

		// Normalize the path
		childPath = Paths.normalize( childPath );

		// Get or create settings node
		Settings child = root.settings.get( childPath );

		if( child == null ) child = new MapSettings( root, childPath, values );

		return child;
	}

	@Override
	public String[] getChildren() {
		List<String> children = new ArrayList<>();

		for( String childPath : root.settings.keySet() ) {
			if( !childPath.startsWith( path ) ) continue;
			String child = Paths.getChild( path, childPath );
			if( !TextUtil.isEmpty( child ) ) children.add( child );
		}

		return children.toArray( new String[ children.size() ] );
	}

	@Override
	public void set( String key, Object value ) {
		String oldValue = values.get( key );
		String newValue = value == null ? null : String.valueOf( value );
		if( newValue == null ) {
			values.remove( key );
		} else {
			values.put( key, newValue );
		}
		if( !Objects.equals( oldValue, value ) ) fireEvent( new SettingsEvent( this, SettingsEvent.Type.UPDATED, path, key, oldValue, newValue ) );
	}

	@Override
	public String get( String key ) {
		return get( key, null );
	}

	@Override
	public String get( String key, Object defaultValue ) {
		Object object = values.get( key );
		String value = object == null ? null : object.toString();
		if( value == null && defaultValues != null ) value = defaultValues.get( key );
		if( value == null ) value = defaultValue == null ? null : defaultValue.toString();
		return value;
	}

	@Override
	public Map<String, String> getDefaultValues() {
		return defaultValues;
	}

	@Override
	public void setDefaultValues( Map<String, String> values ) {
		this.defaultValues = values;
	}

	@Override
	public void flush() {}

	@Override
	public void delete() {
		// NEXT Delete this settings object in the parent
		// OR Reimplement with a static map of settings and simply remove this from the map
	}

}
