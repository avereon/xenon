package com.avereon.xenon.test;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.hamcrest.MatcherAssert.assertThat;

public class ProgramTest extends ProgramTestCase {

	@Test
	void testGetHomeFromLauncherPathForJPackage() {
		System.setProperty( "jpackage.app-path", "/opt/xenon/bin/Xenon" );
		assertThat( getProgram().getHomeFromLauncherPath(), Matchers.is( Path.of( "/opt/xenon" ) ) );
	}

}
