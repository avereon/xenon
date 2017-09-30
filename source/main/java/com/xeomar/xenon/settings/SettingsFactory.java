package com.xeomar.xenon.settings;

import java.io.File;

public class SettingsFactory {

	private static File root;

	private SettingsFactory() {}

	public static File getRoot() {
		return root;
	}

	public static void setRoot( File root ) {
		SettingsFactory.root = root;
	}

	public static Settings getSettings( String path ) {
		return null;
	}

}
