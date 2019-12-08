package com.avereon.xenon.workpane;

import com.avereon.settings.MapSettings;
import com.avereon.settings.Settings;
import com.avereon.xenon.FxPlatformTestCase;
import com.avereon.xenon.workspace.Workarea;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class WorkareaTest extends FxPlatformTestCase {

	private Settings settings = new MapSettings();

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
		area.setSettings( settings );

		// Set the method
		area.setName( name );

		// Assertions
		assertThat( area.getName(), is( name ) );
	}

}
