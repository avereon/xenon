package com.avereon.xenon.tool.welcome;

import com.avereon.xenon.resource.Resource;
import com.avereon.xenon.resource.type.ProgramWelcomeType;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class WelcomeToolGetRequiredResourcesUIT extends WelcomeToolUIT {

	@Test
	void execute() {
		Resource resource = new Resource( ProgramWelcomeType.URI );
		WelcomeTool tool = new WelcomeTool( program, resource );
		Set<URI> resources = tool.getResourceDependencies();
		assertThat( resources.size(), is( 0 ) );
	}

}
