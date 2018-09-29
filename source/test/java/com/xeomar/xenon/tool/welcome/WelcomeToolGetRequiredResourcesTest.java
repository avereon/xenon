package com.xeomar.xenon.tool.welcome;

import com.xeomar.xenon.resource.Resource;
import com.xeomar.xenon.resource.type.ProgramWelcomeType;
import org.junit.Test;

import java.net.URI;
import java.util.Set;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class WelcomeToolGetRequiredResourcesTest extends WelcomeToolTest {

	@Test
	public void execute() {
		Resource resource = new Resource( ProgramWelcomeType.URI );
		WelcomeTool tool = new WelcomeTool( program, resource );
		Set<URI> resources = tool.getResourceDependencies();
		assertThat( resources.size(), is( 0 ) );
	}

}
