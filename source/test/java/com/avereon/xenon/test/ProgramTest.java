package com.avereon.xenon.test;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.hamcrest.MatcherAssert.assertThat;

public class ProgramTest extends ProgramTestCase {

	@Test
	void testGetHomeFromLauncherPathForJPackage() {
		System.setProperty( "jpackage.app-path", "/opt/xenon/bin/Xenon" );
		Path home = getProgram().getHomeFromLauncherPath();

		assertThat( home, Matchers.is( Path.of( "/opt/xenon" ) ) );

		//		if( OperatingSystem.isWindows() ) {
		//			assertThat( home, Matchers.is( Path.of( OperatingSystem.getJavaLauncherPath() ) ) );
		//		} else if( OperatingSystem.isLinux() ) {
		//			assertThat( home, Matchers.is( Path.of( OperatingSystem.getJavaLauncherPath() ).getParent() ) );
		//		} else if( OperatingSystem.isMac() ) {
		//			assertThat( home, Matchers.is( Path.of( OperatingSystem.getJavaLauncherPath() ).getParent() ) );
		//		} else {
		//			fail( "Unknown operating system: " + OperatingSystem.asString() );
		//		}
	}

}
