package com.avereon.xenon;

import com.avereon.util.OperatingSystem;
import org.junit.jupiter.api.BeforeEach;

import java.util.logging.Level;

public class BaseForAllTests {

	// NOTE Do not create an application in the base for all tests

	@BeforeEach
	protected void setup() throws Exception {
		// Turn off logging reduce output during tests
		java.util.logging.Logger.getLogger( "" ).setLevel( Level.OFF );

		// Be sure that the OperatingSystem class is properly set
		OperatingSystem.reset();
	}

}
