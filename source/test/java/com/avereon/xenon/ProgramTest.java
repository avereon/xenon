package com.avereon.xenon;

import com.avereon.util.OperatingSystem;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

public class ProgramTest extends ProgramTestCase {

	@Test
	void testGetHomeFromLauncherPathForJPackage() {
		if( OperatingSystem.isWindows() ) {
			System.setProperty( "jpackage.app-path", "C:\\Program Files\\Xenon\\Xenon.exe" );
			assertThat( getProgram().getHomeFromLauncherPath() ).isEqualTo( Path.of( "C:\\Program Files\\Xenon" ) );
		} else {
			System.setProperty( "jpackage.app-path", "/opt/xenon/bin/Xenon" );
			assertThat( getProgram().getHomeFromLauncherPath() ).isEqualTo( Path.of( "/opt/xenon" ) );
		}
	}

}
