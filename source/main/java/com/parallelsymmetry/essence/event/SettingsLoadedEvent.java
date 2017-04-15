package com.parallelsymmetry.essence.event;

import com.parallelsymmetry.essence.ProgramEvent;

import java.io.File;

public class SettingsLoadedEvent extends ProgramEvent {

	private File file;

	public SettingsLoadedEvent( Object source, File file ) {
		super( source );
		this.file = file;
	}

	@Override
	public String toString() {
		return super.toString() + ":" + file.getName();
	}
}
