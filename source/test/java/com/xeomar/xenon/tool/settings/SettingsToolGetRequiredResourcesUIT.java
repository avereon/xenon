package com.xeomar.xenon.tool.settings;

import com.xeomar.xenon.resource.Resource;
import com.xeomar.xenon.resource.type.ProgramGuideType;
import com.xeomar.xenon.resource.type.ProgramSettingsType;
import org.junit.Assert;
import org.junit.Test;

import java.net.URI;
import java.util.Set;

import static org.hamcrest.Matchers.containsInAnyOrder;

public class SettingsToolGetRequiredResourcesUIT extends SettingsToolUIT {

	@Test
	public void execute() {
		Resource resource = new Resource( ProgramSettingsType.URI );
		SettingsTool tool = new SettingsTool( program, resource );
		Set<URI> resources = tool.getResourceDependencies();
		Assert.assertThat( resources, containsInAnyOrder( ProgramGuideType.URI ) );
	}

}
