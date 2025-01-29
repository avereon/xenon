package com.avereon.xenon;

import com.avereon.xenon.task.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.fail;

@ExtendWith( MockitoExtension.class )
public class ToolManagerTest extends BasePartXenonTestCase {

	private ToolManager toolManager;

	@BeforeEach
	@Override
	public void setup() throws Exception {
		super.setup();
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
	@SuppressWarnings( { "ConstantConditions", "CatchMayIgnoreException" } )
	void testOpenToolNotOnTaskThread() {
		try {
			toolManager.openTool( null );
			fail( "Should throw a RuntimeException" );
		} catch( Exception exception ) {
			assertThat( exception ).isInstanceOf( RuntimeException.class );
		}
	}

	@Test
	@SuppressWarnings( { "ConstantConditions", "CatchMayIgnoreException" } )
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
