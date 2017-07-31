package com.xeomar.xenon.tool;

import com.xeomar.xenon.FxProgramTestCase;
import com.xeomar.xenon.resource.Resource;
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
