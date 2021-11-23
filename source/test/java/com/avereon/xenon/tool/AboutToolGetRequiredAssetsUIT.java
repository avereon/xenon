package com.avereon.xenon.tool;

import com.avereon.xenon.asset.Asset;
import com.avereon.xenon.asset.type.ProgramAboutType;
import com.avereon.xenon.asset.type.ProgramGuideType;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class AboutToolGetRequiredAssetsUIT extends AboutToolUIT {

	@Test
	void execute() {
		Asset asset = new Asset( ProgramAboutType.URI );
		AboutTool tool = new AboutTool( getProgram(), asset );
		Set<URI> assets = tool.getAssetDependencies();
		assertThat( assets ).contains( ProgramGuideType.URI );
	}

}
