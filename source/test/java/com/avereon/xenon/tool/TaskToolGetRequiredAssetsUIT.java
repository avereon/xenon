package com.avereon.xenon.tool;

import com.avereon.xenon.asset.Asset;
import com.avereon.xenon.asset.type.ProgramTaskType;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class TaskToolGetRequiredAssetsUIT extends TaskToolUIT {

	@Test
	void execute() {
		Asset asset = new Asset( ProgramTaskType.URI );
		TaskTool tool = new TaskTool( program, asset );
		assertThat( tool.getAssetDependencies().size(), is( 0 ) );
	}

}

