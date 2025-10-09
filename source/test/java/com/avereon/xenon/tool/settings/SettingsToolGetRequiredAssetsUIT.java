package com.avereon.xenon.tool.settings;

import com.avereon.xenon.resource.Resource;
import com.avereon.xenon.resource.type.ProgramGuideType;
import com.avereon.xenon.resource.type.ProgramSettingsType;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class SettingsToolGetRequiredAssetsUIT extends SettingsToolUIT {

	@Test
	void execute() {
		Resource resource = new Resource( ProgramSettingsType.URI );
		SettingsTool tool = new SettingsTool( getProgram(), resource );
		Set<URI> assets = tool.getAssetDependencies();
		assertThat( assets ).contains( ProgramGuideType.URI );
	}

}
