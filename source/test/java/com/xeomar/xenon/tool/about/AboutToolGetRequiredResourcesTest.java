package com.xeomar.xenon.tool.about;

import com.xeomar.xenon.resource.Resource;
import com.xeomar.xenon.resource.type.ProgramAboutType;
import com.xeomar.xenon.resource.type.ProgramGuideType;
import com.xeomar.xenon.tool.basic.AboutTool;
import org.junit.Test;

import java.net.URI;
import java.util.Set;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertThat;

public class AboutToolGetRequiredResourcesTest extends AboutToolTest {

	@Test
	public void execute() {
		Resource resource = new Resource( ProgramAboutType.uri );
		AboutTool tool = new AboutTool( program, resource );
		Set<URI> resources = tool.getResourceDependencies();
		assertThat( resources, containsInAnyOrder( ProgramGuideType.uri ) );
	}

}
