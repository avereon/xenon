package com.avereon.xenon;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ProgramScreenshotsTest {

	@Test
	void formatScale() {
		assertThat( ProgramScreenshots.formatScale( 0.5 ) ).isEqualTo( "@0.5x" );
		assertThat( ProgramScreenshots.formatScale( 1.0 ) ).isEqualTo( "" );
		assertThat( ProgramScreenshots.formatScale( 1.5 ) ).isEqualTo( "@1.5x" );
		assertThat( ProgramScreenshots.formatScale( 2.0 ) ).isEqualTo( "@2x" );
	}

}
