package com.avereon.xenon.test.workpane;

import com.avereon.xenon.test.FxPlatformTestCase;
import com.avereon.xenon.workspace.Workarea;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WorkareaTest extends FxPlatformTestCase {

	@Test
	void testConstructor() {
		Workarea area = new Workarea();
		assertThat( area ).isNotNull();
	}

	@Test
	void testNameProperty() {
		String name = "Mock Workarea";

		// Create and setup the settings
		Workarea area = new Workarea();

		// Set the method
		area.setName( name );

		// Assertions
		assertThat( area.getName() ).isEqualTo( name );
	}

	@Test
	void testActiveProperty() {
		// Create and setup the settings
		Workarea area = new Workarea();
		assertThat( area.isActive() ).isFalse();

		// Active
		area.setActive( true );
		assertThat( area.isActive() ).isTrue();

		// Inactive
		area.setActive( false );
		assertThat( area.isActive() ).isFalse();
	}

}
