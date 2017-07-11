package com.parallelsymmetry.essence.tool;

import com.parallelsymmetry.essence.FxPlatformTestCase;
import org.junit.Test;

import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertThat;

public class SettingsToolTest extends FxPlatformTestCase {

	@Test
	@SuppressWarnings( "unchecked" )
	public void testToolInfo() {
		assertThat( SettingsTool.getToolInfo().getRequiredToolClasses(), contains( GuideTool.class ) );
	}

}
