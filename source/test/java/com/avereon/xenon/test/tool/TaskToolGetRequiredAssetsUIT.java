package com.avereon.xenon.test.tool;

import com.avereon.xenon.asset.Asset;
import com.avereon.xenon.asset.type.ProgramTaskType;
import com.avereon.xenon.tool.TaskTool;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TaskToolGetRequiredAssetsUIT extends TaskToolUIT {

	@Test
	void execute() {
		Asset asset = new Asset( ProgramTaskType.URI );
		TaskTool tool = new TaskTool( getProgram(), asset );
		assertThat( tool.getAssetDependencies().size() ).isEqualTo( 0 );
	}

}
