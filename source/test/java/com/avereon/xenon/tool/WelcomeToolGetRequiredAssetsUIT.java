package com.avereon.xenon.tool;

import com.avereon.xenon.resource.Resource;
import com.avereon.xenon.resource.type.ProgramWelcomeType;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WelcomeToolGetRequiredAssetsUIT extends WelcomeToolUIT {

	@Test
	void execute() {
		Resource resource = new Resource( ProgramWelcomeType.URI );
		WelcomeTool tool = new WelcomeTool( getProgram(), resource );
		assertThat( tool.getAssetDependencies() ).isEmpty();
	}

}
