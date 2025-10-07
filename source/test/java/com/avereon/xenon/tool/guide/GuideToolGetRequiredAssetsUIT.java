package com.avereon.xenon.tool.guide;

import com.avereon.xenon.asset.Resource;
import com.avereon.xenon.asset.type.ProgramGuideType;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class GuideToolGetRequiredAssetsUIT extends GuideToolUIT {

	@Test
	void execute() {
		Resource resource = new Resource( ProgramGuideType.URI );
		GuideTool tool = new GuideTool( getProgram(), resource );

		Set<URI> assets = tool.getAssetDependencies();
		assertThat( assets.size() ).isEqualTo( 0 );
	}

}
