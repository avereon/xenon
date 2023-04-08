package com.avereon.xenon;

import com.avereon.xenon.test.NewBaseProgramTestCase;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class NewPatternUnitTest extends NewBaseProgramTestCase {

	@Test
	void testSomething() {
		assertThat( "2" ).isEqualTo( String.valueOf( 2 ) );
	}

}
