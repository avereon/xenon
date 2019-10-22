package com.avereon.xenon.tool.about;

import com.avereon.xenon.resource.Resource;
import com.avereon.xenon.resource.type.ProgramAboutType;
import com.avereon.xenon.resource.type.ProgramGuideType;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;

class AboutToolGetRequiredResourcesUIT extends AboutToolUIT {

	@Test
	void execute() {
		Resource resource = new Resource( ProgramAboutType.URI );
		AboutTool tool = new AboutTool( program, resource );
		Set<URI> resources = tool.getResourceDependencies();
		assertThat( resources, containsInAnyOrder( ProgramGuideType.URI ) );
	}

}
