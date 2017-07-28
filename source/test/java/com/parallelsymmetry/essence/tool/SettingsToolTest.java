package com.parallelsymmetry.essence.tool;

import com.parallelsymmetry.essence.FxProgramTestCase;
import com.parallelsymmetry.essence.resource.Resource;
import org.junit.Assert;
import org.junit.Test;

import java.util.Set;

import static org.hamcrest.Matchers.containsInAnyOrder;

public class SettingsToolTest extends FxProgramTestCase {

	@Test
	public void testGetRequiredToolResources() {
		Resource resource = new Resource( "program:settings" );
		SettingsTool tool = new SettingsTool( program, resource );

		Set<String> resources = tool.getResourceDependencies();
		Assert.assertThat( resources, containsInAnyOrder( "program:guide" ) );
	}

}
