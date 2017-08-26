package com.xeomar.xenon.workarea;

import com.xeomar.xenon.FxPlatformTestCase;
import com.xeomar.xenon.MockSettings;
import com.xeomar.xenon.settings.Settings;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class WorkareaTest extends FxPlatformTestCase {

	private Settings settings = new MockSettings();

	@Test
	public void testConstructor() {
		Workarea area = new Workarea();
	}

	@Test
	public void testNameProperty() {
		String name = "Mock Workarea";

		// Create and setup the settings
		Workarea area = new Workarea();
		area.loadSettings( settings );

		// Set the method
		area.setName( name );

		// Assertions
		assertThat( area.getName(), is( name ) );
		assertThat( settings.getString( "name" ), is( name ) );
	}

}
