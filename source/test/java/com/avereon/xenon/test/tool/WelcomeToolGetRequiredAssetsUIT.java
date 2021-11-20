package com.avereon.xenon.test.tool;

import com.avereon.xenon.asset.Asset;
import com.avereon.xenon.asset.type.ProgramWelcomeType;
import com.avereon.xenon.tool.WelcomeTool;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WelcomeToolGetRequiredAssetsUIT extends WelcomeToolUIT {

	@Test
	void execute() {
		Asset asset = new Asset( ProgramWelcomeType.URI );
		WelcomeTool tool = new WelcomeTool( getProgram(), asset );
		assertThat( tool.getAssetDependencies().size() ).isEqualTo( 0 );
	}

}
