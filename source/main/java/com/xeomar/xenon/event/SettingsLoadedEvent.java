package com.xeomar.xenon.event;

public class SettingsLoadedEvent extends ProgramSettingsEvent {

	private String root;

	private String id;

	public SettingsLoadedEvent( Object source, String root ) {
		this( source, root, null );
	}

	public SettingsLoadedEvent( Object source, String root, String id ) {
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
