package com.xeomar.xenon.event;

import java.io.File;

public class SettingsLoadedEvent extends SettingsEvent {

	private File file;

	private String id;

	public SettingsLoadedEvent( Object source, File file ) {
		this( source, file, null );
	}

	public SettingsLoadedEvent( Object source, File file, String id ) {
		super( source );
		this.file = file;
		this.id = id;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder(super.toString());
		if( id != null ) {
			builder.append( ":" );
			builder.append( id );
		}
		if( file != null ) {
			builder.append( ":" );
			builder.append( file.getName() );
		}
		return builder.toString();
	}
}
