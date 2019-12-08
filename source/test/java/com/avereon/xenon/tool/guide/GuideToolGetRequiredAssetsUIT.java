package com.avereon.xenon.tool.guide;

import com.avereon.xenon.asset.Asset;
import com.avereon.xenon.asset.type.ProgramGuideType;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class GuideToolGetRequiredAssetsUIT extends GuideToolUIT {

	@Test
	void execute() {
		Asset asset = new Asset( ProgramGuideType.URI );
		GuideTool tool = new GuideTool( program, asset );

		Set<URI> assets = tool.getAssetDependencies();
		assertThat( assets.size(), is( 0 ) );
	}

}
