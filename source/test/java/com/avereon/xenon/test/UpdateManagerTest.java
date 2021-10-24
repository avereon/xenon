package com.avereon.xenon.test;

import com.avereon.xenon.UpdateManager;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.hamcrest.MatcherAssert.assertThat;

public class UpdateManagerTest extends ProgramTest {

	@Test
	void testRelativizeUpdaterPathStrategyJava17() {
		String binPath = "bin/Xenon";
		String prefix = "mock-updater";

		Path homeFolder = Path.of( "/opt/xenon" );
		Path dataFolder = Path.of( "/home/user/.config/xenon" );
		Path updatesFolder = dataFolder.resolve( "updates" );
		Path updaterFolder = updatesFolder.resolve( "updater" );
		Path expectedUpdaterLauncher = updaterFolder.resolve( binPath );

		assertThat( UpdateManager.calcUpdaterLauncher( homeFolder, updatesFolder, prefix, "" ), Matchers.is( expectedUpdaterLauncher ) );
	}

}
