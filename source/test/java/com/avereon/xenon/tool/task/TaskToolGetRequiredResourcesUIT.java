package com.avereon.xenon.tool.task;

import com.avereon.xenon.resource.Resource;
import com.avereon.xenon.resource.type.ProgramTaskType;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class TaskToolGetRequiredResourcesUIT extends TaskToolUIT {

	@Test
	public void execute() {
		Resource resource = new Resource( ProgramTaskType.URI );
		TaskTool tool = new TaskTool( program, resource );
		assertThat( tool.getResourceDependencies().size(), is( 0 ) );
	}

}

