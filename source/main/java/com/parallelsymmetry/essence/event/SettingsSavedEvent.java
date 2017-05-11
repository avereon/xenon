package com.parallelsymmetry.essence.event;

import com.parallelsymmetry.essence.ProgramEvent;

import java.io.File;

public class SettingsSavedEvent extends SettingsEvent {

	private File file;

	private String id;

	public SettingsSavedEvent( Object source, File file ) {
		this( source, file, null );
	}

	public SettingsSavedEvent( Object source, File file, String id ) {
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
