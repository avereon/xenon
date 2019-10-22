package com.avereon.xenon.tool.task;

import com.avereon.xenon.resource.Resource;
import com.avereon.xenon.resource.type.ProgramTaskType;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class TaskToolGetRequiredResourcesUIT extends TaskToolUIT {

	@Test
	void execute() {
		Resource resource = new Resource( ProgramTaskType.URI );
		TaskTool tool = new TaskTool( program, resource );
		assertThat( tool.getResourceDependencies().size(), is( 0 ) );
	}

}

