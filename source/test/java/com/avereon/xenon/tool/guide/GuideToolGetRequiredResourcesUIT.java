package com.avereon.xenon.tool.guide;

import com.avereon.xenon.resource.Resource;
import com.avereon.xenon.resource.type.ProgramGuideType;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class GuideToolGetRequiredResourcesUIT extends GuideToolUIT {

	@Test
	void execute() {
		Resource resource = new Resource( ProgramGuideType.URI );
		GuideTool tool = new GuideTool( program, resource );

		Set<URI> resources = tool.getResourceDependencies();
		assertThat( resources.size(), is( 0 ) );
	}

}
