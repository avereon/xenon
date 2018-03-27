package com.xeomar.xenon.tool.task;

import com.xeomar.xenon.resource.Resource;
import com.xeomar.xenon.resource.type.ProgramTaskType;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class TaskToolGetRequiredResourcesTest extends TaskToolTest {

	@Test
	public void execute() {
		Resource resource = new Resource( ProgramTaskType.uri );
		TaskTool tool = new TaskTool( program, resource );
		assertThat( tool.getResourceDependencies().size(), is( 0 ) );
	}

}

