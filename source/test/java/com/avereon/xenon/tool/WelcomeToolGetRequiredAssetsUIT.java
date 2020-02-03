package com.avereon.xenon.tool;

import com.avereon.xenon.asset.Asset;
import com.avereon.xenon.asset.type.ProgramWelcomeType;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class WelcomeToolGetRequiredAssetsUIT extends WelcomeToolUIT {

	@Test
	void execute() {
		Asset asset = new Asset( ProgramWelcomeType.URI );
		WelcomeTool tool = new WelcomeTool( program, asset );
		assertThat( tool.getAssetDependencies().size(), is( 0 ) );
	}

}
