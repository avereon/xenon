package com.avereon.xenon.tool.settings;

import com.avereon.xenon.asset.Asset;
import com.avereon.xenon.asset.type.ProgramGuideType;
import com.avereon.xenon.asset.type.ProgramSettingsType;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;

class SettingsToolGetRequiredAssetsUIT extends SettingsToolUIT {

	@Test
	void execute() {
		Asset asset = new Asset( ProgramSettingsType.URI );
		SettingsTool tool = new SettingsTool( program, asset );
		Set<URI> assets = tool.getAssetDependencies();
		assertThat( assets, containsInAnyOrder( ProgramGuideType.URI ) );
	}

}
