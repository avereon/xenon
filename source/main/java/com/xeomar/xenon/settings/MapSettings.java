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
	public String getName() {
		return Paths.getName( path );
	}

	@Override
	public String getPath() {
		return path;
	}

	@Override
	public boolean exists( String path ) {
		return root.settings.get( getNodePath( this.path, path ) ) != null;
	}

	@Override
	public Settings getNode( String path ) {
		return getNode( path, (Map<String,String>)null );
	}

	@Override
	public Settings getNode( String path, Map<String, String> values ) {
		String nodePath = getNodePath( this.path, path );

		// Get or create settings node
		Settings child = root.settings.get( nodePath );

		if( child == null ) child = new MapSettings( root, nodePath, values );

		return child;
	}

	@Override
	public String[] getNodes() {
		List<String> children = new ArrayList<>();

		for( String childPath : root.settings.keySet() ) {
			if( !childPath.startsWith( path ) ) continue;
			String child = Paths.getChild( path, childPath );
			if( !TextUtil.isEmpty( child ) ) children.add( child );
		}

		return children.toArray( new String[ children.size() ] );
	}

	@Override
	public Set<String> getKeys() {
		return values.keySet();
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
		root.settings.remove( getPath() );
	}

}
