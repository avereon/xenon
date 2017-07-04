package com.parallelsymmetry.essence.workarea;

import com.parallelsymmetry.essence.MockSettings;
import com.parallelsymmetry.essence.settings.Settings;
import com.parallelsymmetry.essence.testutil.FxPlatformTestCase;
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
