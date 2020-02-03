package com.avereon.xenon.tool;

import com.avereon.xenon.asset.Asset;
import com.avereon.xenon.asset.type.ProgramAboutType;
import com.avereon.xenon.asset.type.ProgramGuideType;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;

class AboutToolGetRequiredAssetsUIT extends AboutToolUIT {

	@Test
	void execute() {
		Asset asset = new Asset( ProgramAboutType.URI );
		AboutTool tool = new AboutTool( program, asset );
		Set<URI> assets = tool.getAssetDependencies();
		assertThat( assets, containsInAnyOrder( ProgramGuideType.URI ) );
	}

}
