package com.parallelsymmetry.essence.event;

import com.parallelsymmetry.essence.ProgramEvent;

import java.io.File;

public class SettingsErrorEvent extends ProgramEvent {

	private File file;

	public SettingsErrorEvent( Object source, File file ) {
		super( source );
		this.file = file;
	}

	@Override
	public String toString() {
		return super.toString() + ":" + file.getName();
	}

}
