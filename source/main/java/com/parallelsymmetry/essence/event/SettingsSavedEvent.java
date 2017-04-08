package com.parallelsymmetry.essence.event;

import com.parallelsymmetry.essence.ProgramEvent;

import java.io.File;

public class SettingsSavedEvent extends ProgramEvent {

	private File file;

	public SettingsSavedEvent( Object source, File file ) {
		super( source );
		this.file = file;
	}

	public String toString() {
		return super.toString() + " " + file;
	}
}
