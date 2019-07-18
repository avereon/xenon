package com.avereon.xenon.tool.guide;

import com.avereon.xenon.resource.Resource;
import com.avereon.xenon.resource.type.ProgramGuideType;
import org.junit.Assert;
import org.junit.Test;

import java.net.URI;
import java.util.Set;

import static org.hamcrest.Matchers.is;

public class GuideToolGetRequiredResourcesUIT extends GuideToolUIT {

	@Test
	public void execute() {
		Resource resource = new Resource( ProgramGuideType.URI );
		GuideTool tool = new GuideTool( program, resource );

		Set<URI> resources = tool.getResourceDependencies();
		Assert.assertThat( resources.size(), is( 0 ) );
	}

}
