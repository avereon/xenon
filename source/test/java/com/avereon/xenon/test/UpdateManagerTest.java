package com.avereon.xenon.test;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;

public class UpdateManagerTest {

	@Test
	void testRelativizeUpdaterPath() {
		Path homeFolder = Path.of( "/opt/xenon/lib/runtime" );
		Path dataFolder = Path.of( "/home/ecco/.config/xenon" );
		Path javaLauncher = Path.of( "/opt/xenon/bin/Xenon" );
		Path updaterFolder = dataFolder.resolve( "updates/updater" );

		Path relative = homeFolder.relativize( javaLauncher );
		Path updaterLauncher = updaterFolder.resolve( relative ).normalize();

		System.out.println( "relative=" + relative );
		System.out.println( "updaterLauncher=" + updaterLauncher );
	}

}
