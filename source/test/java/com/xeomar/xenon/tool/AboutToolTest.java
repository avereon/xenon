package com.xeomar.xenon.tool;

import com.xeomar.xenon.FxProgramTestCase;
import com.xeomar.xenon.workarea.Workpane;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@RunWith( Parameterized.class )
public class AboutToolTest extends FxProgramTestCase {

	@Parameterized.Parameters
	public static Object[][] data() {
		return new Object[5][0];
	}

//	@Test
//	public void testGetRequiredToolResources() {
//		Resource resource = new Resource( ProgramAboutType.uri );
//		AboutTool tool = new AboutTool( program, resource );
//		Set<URI> resources = tool.getResourceDependencies();
//		assertThat( resources, containsInAnyOrder( ProgramGuideType.uri ) );
//	}

	@Test
	public void testOpenTool() throws Exception {
		Workpane pane = program.getWorkspaceManager().getActiveWorkspace().getActiveWorkarea().getWorkpane();
		assertThat( pane.getTools().size(), is( 0 ) );

		clickOn( "#menu-help" );
//		clickOn( "#menuitem-about" );
//		workpaneWatcher.waitForEvent( WorkpaneEvent.Type.TOOL_ADDED );
//		workpaneWatcher.waitForEvent( WorkpaneEvent.Type.TOOL_ADDED );
//
//		assertThat( pane.getTools().size(), is( 2 ) );
//		assertThat( pane.getActiveTool(), instanceOf( AboutTool.class ) );
	}

	//	@Test
//	public void testOpenToolTwice() throws Exception {
//		Workpane pane = program.getWorkspaceManager().getActiveWorkspace().getActiveWorkarea().getWorkpane();
//		assertThat( pane.getTools().size(), is( 0 ) );
//
//		clickOn( "#menu-help" );
//		clickOn( "#menuitem-about" );
//		workpaneWatcher.waitForEvent( WorkpaneEvent.Type.TOOL_ADDED );
//		workpaneWatcher.waitForEvent( WorkpaneEvent.Type.TOOL_ADDED );
//		assertThat( pane.getActiveTool(), instanceOf( AboutTool.class ) );
//		assertThat( pane.getTools().size(), is( 2 ) );
//
//		// Try to open the tool again and make sure there is still only one
//		clickOn( "#menu-help" );
//		clickOn( "#menuitem-about" );
//		workpaneWatcher.waitForEvent( WorkpaneEvent.Type.TOOL_ACTIVATED );
//		assertThat( pane.getTools().size(), is( 2 ) );
//	}
//
//	@Test
//	public void testClosingResourceWillCloseTool() throws Exception {
//		Workpane pane = program.getWorkspaceManager().getActiveWorkspace().getActiveWorkarea().getWorkpane();
//		assertThat( pane.getTools().size(), is( 0 ) );
//
//		Future<ProgramTool> future = program.getResourceManager().open( ProgramAboutType.uri );
//		workpaneWatcher.waitForEvent( WorkpaneEvent.Type.TOOL_ADDED );
//		workpaneWatcher.waitForEvent( WorkpaneEvent.Type.TOOL_ADDED );
//		assertThat( pane.getActiveTool(), instanceOf( AboutTool.class ) );
//		assertThat( pane.getTools().size(), is( 2 ) );
//
//		program.getResourceManager().closeResources( future.get().getResource() );
//		workpaneWatcher.waitForEvent( WorkpaneEvent.Type.TOOL_REMOVED );
//		assertThat( pane.getTools().size(), is( 1 ) );
//	}

}
