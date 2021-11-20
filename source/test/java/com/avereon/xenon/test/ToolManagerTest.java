package com.avereon.xenon.test;

import com.avereon.xenon.ToolManager;
import com.avereon.xenon.task.Task;
import com.avereon.xenon.task.TaskManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.fail;

public class ToolManagerTest extends BaseTestCase {

	private TaskManager taskManager;

	private ToolManager toolManager;

	@BeforeEach
	@Override
	public void setup() throws Exception {
		super.setup();
		taskManager = new TaskManager().start();
		toolManager = new ToolManager( getProgram() );
	}

	@Test
	void testGetToolClassName() {
		toolManager.addToolAlias( "oldName", "newName" );
		assertThat( toolManager.getToolClassName( "oldName" ) ).isEqualTo( "newName" );
	}

	@Test
	void testGetToolClassNameWithNull() {
		assertThat( toolManager.getToolClassName( null ) ).isNull();
	}

	@Test
	@SuppressWarnings( { "ConstantConditions", "CatchMayIgnoreException", "ResultOfMethodCallIgnored" } )
	void testOpenToolNotOnTaskThread() {
		try {
			toolManager.openTool( null );
			fail( "Should throw a RuntimeException" );
		} catch( Exception exception ) {
			assertThat( exception ).isInstanceOf( RuntimeException.class );
		}
	}

	@Test
	@SuppressWarnings( { "ConstantConditions", "CatchMayIgnoreException", "ResultOfMethodCallIgnored" } )
	void testOpenToolWithNullAsset() {
		taskManager.submit( Task.of( "", () -> {
			try {
				toolManager.openTool( null );
				fail( "Should throw a NullPointerException" );
			} catch( Exception exception ) {
				assertThat( exception ).isInstanceOf( NullPointerException.class );
			}
		} ) );
	}

}
