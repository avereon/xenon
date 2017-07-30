package com.xeomar.xenon.event;

import java.io.File;

public class SettingsSavedEvent extends SettingsEvent {

	private File file;

	private String scope;

	public SettingsSavedEvent( Object source, File file ) {
		this( source, file, null );
	}

	public SettingsSavedEvent( Object source, File file, String scope ) {
		super( source );
		this.file = file;
		this.scope = scope;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder(super.toString());
		if( scope != null ) {
			builder.append( ":" );
			builder.append( scope );
		}
		if( file != null ) {
			builder.append( ":" );
			builder.append( file.getName() );
		}
		return builder.toString();
	}
}
