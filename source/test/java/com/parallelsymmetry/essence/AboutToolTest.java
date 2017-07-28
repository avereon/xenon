package com.parallelsymmetry.essence;

import com.parallelsymmetry.essence.resource.Resource;
import com.parallelsymmetry.essence.tool.AboutTool;
import com.parallelsymmetry.essence.workarea.Workpane;
import com.parallelsymmetry.essence.workarea.WorkpaneEvent;
import org.junit.Test;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class AboutToolTest extends FxProgramTestCase {

	@Test
	public void testOpenTool() throws Exception {
		Workpane pane = program.getWorkspaceManager().getActiveWorkspace().getActiveWorkarea().getWorkpane();
		assertThat( pane.getTools().size(), is( 0 ) );

		program.getResourceManager().open( program.getResourceManager().createResource( "program:about" ) );
//		workpaneWatcher.waitForEvent( WorkpaneEvent.Type.TOOL_ACTIVATED, 1000 );
//		workpaneWatcher.waitForEvent( WorkpaneEvent.Type.TOOL_ACTIVATED, 1000 );
		Thread.sleep( 500 );
		assertThat( pane.getActiveTool(), instanceOf( AboutTool.class ) );
		assertThat( pane.getTools().size(), is( 2 ) );
	}

	@Test
	public void testOpenToolTwice() throws Exception {
		Workpane pane = program.getWorkspaceManager().getActiveWorkspace().getActiveWorkarea().getWorkpane();
		assertThat( pane.getTools().size(), is( 0 ) );

		program.getResourceManager().open( program.getResourceManager().createResource( "program:about" ) );
		workpaneWatcher.waitForEvent( WorkpaneEvent.Type.TOOL_ACTIVATED, 1000 );
		//workpaneWatcher.waitForEvent( WorkpaneEvent.Type.TOOL_ACTIVATED, 1000 );
		Thread.sleep( 500 );
		assertThat( pane.getActiveTool(), instanceOf( AboutTool.class ) );
		assertThat( pane.getTools().size(), is( 2 ) );

		program.getResourceManager().open( program.getResourceManager().createResource( "program:about" ) );
		workpaneWatcher.waitForEvent( WorkpaneEvent.Type.TOOL_ACTIVATED, 1000 );
		//workpaneWatcher.waitForEvent( WorkpaneEvent.Type.TOOL_ACTIVATED, 1000 );
		Thread.sleep( 500 );
		assertThat( pane.getTools().size(), is( 2 ) );
	}

	@Test
	public void testClosingResourceWillCloseTool() throws Exception {
		testOpenTool();
		Resource resource = program.getResourceManager().createResource( "program:about" );

		Workpane pane = program.getWorkspaceManager().getActiveWorkspace().getActiveWorkarea().getWorkpane();
		assertThat( pane.getActiveTool(), instanceOf( AboutTool.class ) );
		assertThat( pane.getTools().size(), is( 2 ) );

		program.getResourceManager().closeResources( resource );
		workpaneWatcher.waitForEvent( WorkpaneEvent.Type.TOOL_REMOVED, 1000 );

		assertThat( pane.getTools().size(), is( 1 ) );
	}

}
