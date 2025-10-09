package com.avereon.xenon.tool;

import com.avereon.xenon.resource.Resource;
import com.avereon.xenon.resource.type.ProgramTaskType;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TaskToolGetRequiredAssetsUIT extends TaskToolUIT {

	@Test
	void execute() {
		Resource resource = new Resource( ProgramTaskType.URI );
		TaskTool tool = new TaskTool( getProgram(), resource );
		assertThat( tool.getAssetDependencies() ).isEmpty();
	}

}
