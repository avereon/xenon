package com.avereon.xenon.test;

import com.avereon.util.OperatingSystem;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.hamcrest.MatcherAssert.assertThat;

public class ProgramTest extends ProgramTestCase {

	@Test
	void testGetHomeFromLauncherPathForJPackage() {
		if( OperatingSystem.isWindows() ) {
			System.setProperty( "jpackage.app-path", "C:\\Program Files\\Xenon\\Xenon.exe" );
			assertThat( getProgram().getHomeFromLauncherPath(), Matchers.is( Path.of( "C:\\Program Files\\Xenon" ) ) );
		} else {
			System.setProperty( "jpackage.app-path", "/opt/xenon/bin/Xenon" );
			assertThat( getProgram().getHomeFromLauncherPath(), Matchers.is( Path.of( "/opt/xenon" ) ) );
		}
	}

}
