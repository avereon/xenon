package com.avereon.xenon.event;

public class SettingsLoadedEventOld extends ProgramSettingsEventOld {

	private String root;

	private String id;

	public SettingsLoadedEventOld( Object source, String root ) {
		this( source, root, null );
	}

	public SettingsLoadedEventOld( Object source, String root, String id ) {
		super( source );
		this.root = root;
		this.id = id;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder(super.toString());
		if( id != null ) {
			builder.append( ":" );
			builder.append( id );
		}
		if( root != null ) {
			builder.append( ":" );
			builder.append( root );
		}
		return builder.toString();
	}

}
