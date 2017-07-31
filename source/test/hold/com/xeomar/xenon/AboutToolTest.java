package com.xeomar.xenon;

import com.xeomar.xenon.resource.Resource;
import com.xeomar.xenon.tool.AboutTool;
import com.xeomar.xenon.workarea.Workpane;
import com.xeomar.xenon.workarea.WorkpaneEvent;
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
