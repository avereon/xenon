package com.avereon.xenon;

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

	private ToolManager toolManager;

	@BeforeEach
	@Override
	public void setup() throws Exception {
		super.setup();
		taskManager = new TaskManager().start();
		toolManager = new ToolManager( program );
	}

	@Test
	void testGetToolClassName() {
		toolManager.addToolAlias( "oldName", "newName" );
		assertThat( toolManager.getToolClassName( "oldName" ), is( "newName" ) );
	}

	@Test
	void testGetToolClassNameWithNull() {
		assertThat( toolManager.getToolClassName( null ), is( nullValue() ) );
	}

	@Test
	@SuppressWarnings( "ConstantConditions" )
	void testOpenToolNotOnTaskThread() {
		try {
			toolManager.openTool( null );
			fail( "Should throw a RuntimeException" );
		} catch( Exception exception ) {
			assertThat( exception, is( instanceOf( RuntimeException.class ) ) );
		}
	}

	@Test
	@SuppressWarnings( "ConstantConditions" )
	void testOpenToolWithNullAsset() {
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
