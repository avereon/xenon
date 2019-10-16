package com.avereon.xenon.tool.settings;

import com.avereon.xenon.resource.Resource;
import com.avereon.xenon.resource.type.ProgramGuideType;
import com.avereon.xenon.resource.type.ProgramSettingsType;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;

public class SettingsToolGetRequiredResourcesUIT extends SettingsToolUIT {

	@Test
	public void execute() {
		Resource resource = new Resource( ProgramSettingsType.URI );
		SettingsTool tool = new SettingsTool( program, resource );
		Set<URI> resources = tool.getResourceDependencies();
		assertThat( resources, containsInAnyOrder( ProgramGuideType.URI ) );
	}

}
