package com.avereon.xenon.test;

import com.avereon.util.OperatingSystem;
import com.avereon.xenon.Program;
import org.junit.jupiter.api.BeforeEach;

import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class BaseTestCase {

	protected Program program;

	@BeforeEach
	protected void setup() throws Exception {
		// Be sure that the OperatingSystem class is properly set
		OperatingSystem.reset();
		program = new Program();
		Logger.getLogger( "" ).setLevel( Level.OFF );
	}

	protected Program getProgram() {
		return program;
	}

}
