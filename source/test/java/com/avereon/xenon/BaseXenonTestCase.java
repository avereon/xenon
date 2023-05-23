package com.avereon.xenon;

import com.avereon.util.Parameters;
import com.avereon.xenon.test.ProgramTestConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

/**
 * This class is a duplicate of com.avereon.zenna.BaseXenonUiTestCase which is
 * intended to be visible for mod testing but is not available to Xenon to
 * avoid a circular dependency. Attempts at making this
 * class publicly available have run in to various challenges with the most
 * recent being with Surefire not putting JUnit 5 on the module path.
 */
public abstract class BaseXenonTestCase extends CommonXenonTestCase {

	@BeforeEach
	protected void setup() throws Exception {
		super.setup();

		Xenon xenon = new Xenon().setProgramParameters( Parameters.parse( ProgramTestConfig.getParameterValues() ) );
		xenon.init();

		// Create the program
		setProgram( xenon );
	}

	@AfterEach
	protected void teardown() throws Exception {
		Xenon program = getProgram();

		// Clean up the settings
		if( program != null ) program.getSettingsManager().getSettings( ProgramSettings.BASE ).delete();

		super.teardown();
	}

	public Xenon getProgram() {
		return (Xenon)super.getProgram();
	}

}
