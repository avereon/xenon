package com.avereon.xenon;

import com.avereon.xenon.test.annotation.PartProgramTest;
import org.junit.jupiter.api.BeforeEach;

@PartProgramTest
public class ProgramTestCase extends BaseXenonTestCase {

	@BeforeEach
	protected void setup() throws Exception {
		super.setup();
		getProgram().init();
	}

}
