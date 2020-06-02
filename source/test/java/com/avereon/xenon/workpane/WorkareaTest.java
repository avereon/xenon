package com.avereon.xenon.workpane;

import com.avereon.xenon.FxPlatformTestCase;
import com.avereon.xenon.workspace.Workarea;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;

class WorkareaTest extends FxPlatformTestCase {

	@Test
	void testConstructor() {
		Workarea area = new Workarea();
		assertNotNull( area );
	}

	@Test
	void testNameProperty() {
		String name = "Mock Workarea";

		// Create and setup the settings
		Workarea area = new Workarea();

		// Set the method
		area.setName( name );

		// Assertions
		assertThat( area.getName(), is( name ) );
	}

	@Test
	void testActiveProperty() {
		// Create and setup the settings
		Workarea area = new Workarea();
		assertFalse( area.isActive() );

		// Active
		area.setActive( true );
		assertTrue( area.isActive() );

		// Inactive
		area.setActive( false );
		assertFalse( area.isActive() );
	}

}
