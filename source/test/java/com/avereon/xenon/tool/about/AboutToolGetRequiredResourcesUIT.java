package com.avereon.xenon.tool.about;

import com.avereon.xenon.resource.Resource;
import com.avereon.xenon.resource.type.ProgramAboutType;
import com.avereon.xenon.resource.type.ProgramGuideType;
import org.junit.Test;

import java.net.URI;
import java.util.Set;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertThat;

public class AboutToolGetRequiredResourcesUIT extends AboutToolUIT {

	@Test
	public void execute() {
		Resource resource = new Resource( ProgramAboutType.URI );
		AboutTool tool = new AboutTool( program, resource );
		Set<URI> resources = tool.getResourceDependencies();
		assertThat( resources, containsInAnyOrder( ProgramGuideType.URI ) );
	}

}
