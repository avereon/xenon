package com.avereon.xenon;

import com.avereon.util.OperatingSystem;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

public class UpdateManagerTest extends ProgramTest {

	@Test
	void testRelativizeUpdaterPathStrategyJava17() {
		String binPath = "bin/Xenon";
		String prefix = "mock-updater";

		Path homeFolder = Path.of( "/opt/xenon" );
		Path dataFolder = Path.of( "/home/user/.config/xenon" );

		if( OperatingSystem.isWindows() ) {
			binPath = "Xenon.exe";
			homeFolder = Path.of( "C:\\Program Files\\Xenon" );
			dataFolder = Path.of( "C:\\Users\\user\\.config\\xenon" );
		}

		Path updatesFolder = dataFolder.resolve( "updates" );
		Path updaterFolder = updatesFolder.resolve( "updater" );
		Path expectedUpdaterLauncher = updaterFolder.resolve( binPath );

		assertThat( UpdateManager.calcUpdaterLauncher( homeFolder, updatesFolder, prefix, "" ) ).isEqualTo( expectedUpdaterLauncher );
	}

}
