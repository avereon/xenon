package com.avereon.xenon;

import com.avereon.util.OperatingSystem;
import org.junit.jupiter.api.BeforeEach;

import java.util.logging.Level;

/**
 * This class is a duplicate of com.avereon.zenna.BaseXenonUiTestCase which is
 * intended to be visible for mod testing but is not available to Xenon to
 * avoid a circular dependency. Attempts at making this
 * class publicly available have run in to various challenges with the most
 * recent being with Surefire not putting JUnit 5 on the module path.
 */
public abstract class BaseXenonTestCase {

	protected Xenon program;

	@BeforeEach
	protected void setup() throws Exception {
		// Be sure that the OperatingSystem class is properly set
		OperatingSystem.reset();
		program = new Xenon();
		java.util.logging.Logger.getLogger( "" ).setLevel( Level.OFF );
	}

	protected Xenon getProgram() {
		return program;
	}

}
