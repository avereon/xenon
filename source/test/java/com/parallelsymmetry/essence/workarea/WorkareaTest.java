package com.parallelsymmetry.essence.workarea;

import com.parallelsymmetry.essence.testutil.FxApplicationTestCase;
import org.apache.commons.configuration2.BaseConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class WorkareaTest extends FxApplicationTestCase {

	private Configuration configuration = new BaseConfiguration();

	@Test
	public void testConstructor() {
		Workarea area = new Workarea();
	}

	@Test
	public void testNameProperty() {
		String name = "Mock Workarea";

		// Create and setup the configuration
		Workarea area = new Workarea();
		area.setConfiguration( configuration );

		// Set the method
		area.setName( name );

		// Assertions
		assertThat( area.getName(), is( name ) );
		assertThat( configuration.getString( "name" ), is( name ) );
	}

}
