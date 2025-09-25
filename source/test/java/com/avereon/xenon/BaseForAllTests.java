package com.avereon.xenon;

import com.avereon.log.Log;
import com.avereon.util.OperatingSystem;
import org.junit.jupiter.api.BeforeEach;

import java.util.logging.Level;

public class BaseForAllTests {

	// NOTE Do not create an application in the base for all tests

	@BeforeEach
	protected void setup() throws Exception {
		// Turn off logging to reduce output during tests
		Log.setBaseLogLevel( Level.OFF );

		// Be sure that the OperatingSystem class is properly set
		OperatingSystem.reset();
	}

	protected void setLogLevel( Level level ) {
		Log.setBaseLogLevel( level );
	}

}
