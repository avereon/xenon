package com.xeomar.xenon.tool.guide;

import com.xeomar.xenon.resource.Resource;
import com.xeomar.xenon.resource.type.ProgramGuideType;
import org.junit.Assert;
import org.junit.Test;

import java.net.URI;
import java.util.Set;

import static org.hamcrest.Matchers.is;

public class GuideToolGetRequiredResourcesTest extends GuideToolTest {

	@Test
	public void execute() {
		Resource resource = new Resource( ProgramGuideType.uri );
		GuideTool tool = new GuideTool( program, resource );

		Set<URI> resources = tool.getResourceDependencies();
		Assert.assertThat( resources.size(), is( 0 ) );
	}

}
