package com.avereon.xenon;

import com.avereon.xenon.resource.ResourceManager;
import com.avereon.xenon.task.Task;
import com.avereon.xenon.task.TaskManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.fail;

public class ToolManagerTest extends BaseTestCase {

	private TaskManager taskManager;

	private ResourceManager resourceManager;

	private ToolManager toolManager;

	@BeforeEach
	@Override
	public void setup() throws Exception {
		super.setup();
		taskManager = new TaskManager().start();
		resourceManager = new ResourceManager( program );
		toolManager = new ToolManager( program );
	}

	@Test
	public void testGetToolClassName() {
		toolManager.addToolAlias( "oldName", "newName" );
		assertThat( toolManager.getToolClassName( "oldName" ), is( "newName" ) );
	}

	@Test
	public void testGetToolClassNameWithNull() {
		assertThat( toolManager.getToolClassName( null ), is( nullValue() ) );
	}

	@Test
	public void testOpenToolNotOnTaskThread() {
		try {
			toolManager.openTool( null );
			fail( "Should throw a RuntimeException" );
		} catch( Exception exception ) {
			assertThat( exception, is( instanceOf( RuntimeException.class ) ) );
		}
	}

	@Test
	public void testOpenToolWithNullResource() {
		taskManager.submit( Task.of( "", () -> {
			try {
				toolManager.openTool( null );
				fail( "Should throw a NullPointerException" );
			} catch( Exception exception ) {
				assertThat( exception, is( instanceOf( NullPointerException.class ) ) );
			}
		} ) );
	}

}
