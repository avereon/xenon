package com.avereon.xenon;

import com.avereon.util.OperatingSystem;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

public class XenonTest extends ProgramTestCase {

	@Test
	void testGetHomeFromLauncherPathForJPackage() {
		if( OperatingSystem.isWindows() ) {
			assertThat( getProgram().getHomeFromLauncherPath() ).isEqualTo( Path.of( "C:\\Program Files\\Xenon" ) );
		} else {
			assertThat( getProgram().getHomeFromLauncherPath() ).isEqualTo( Path.of( "/opt/xenon" ) );
		}
	}

	@Test
	void combineProfileMode() {
		assertThat( getProgram().combineProfileMode( "profile", "mode" ) ).isEqualTo( "profile-mode" );
		assertThat( getProgram().combineProfileMode( "profile", null ) ).isEqualTo( "profile" );
		assertThat( getProgram().combineProfileMode( null, "mode" ) ).isEqualTo( "mode" );
		assertThat( getProgram().combineProfileMode( null, null ) ).isEqualTo( "" );
	}

	@Test
	void getHomeFromLauncherPath() {
		assertThat( getProgram().getHomeFromLauncherPath("C:/Program Files/Xenon/Xenon.exe", true) ).isEqualTo( Path.of( "C:/Program Files/Xenon" ) );
		assertThat( getProgram().getHomeFromLauncherPath("/opt/xenon/bin/xenon", false) ).isEqualTo( Path.of( "/opt/xenon" ) );
	}

}
