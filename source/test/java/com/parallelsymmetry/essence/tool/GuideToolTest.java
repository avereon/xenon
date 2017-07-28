package com.parallelsymmetry.essence.tool;

import com.parallelsymmetry.essence.FxProgramTestCase;
import com.parallelsymmetry.essence.resource.Resource;
import org.junit.Assert;
import org.junit.Test;

import java.util.Set;

import static org.hamcrest.Matchers.is;

public class GuideToolTest extends FxProgramTestCase {

	@Test
	public void testGetRequiredToolResources() {
		Resource resource = new Resource( "program:guide" );
		GuideTool tool = new GuideTool( program, resource );

		Set<String> resources = tool.getResourceDependencies();
		Assert.assertThat(resources.size(), is( 0 ));
	}

}
