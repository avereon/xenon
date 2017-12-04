package com.xeomar.xenon.tool;

import com.xeomar.xenon.FxProgramTestCase;
import com.xeomar.xenon.resource.Resource;
import com.xeomar.xenon.resource.type.ProgramAboutType;
import com.xeomar.xenon.resource.type.ProgramGuideType;
import com.xeomar.xenon.workarea.Workpane;
import com.xeomar.xenon.workarea.WorkpaneEvent;
import org.junit.Test;

import java.net.URI;
import java.util.Set;
import java.util.concurrent.Future;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class AboutToolTest extends FxProgramTestCase {

	@Test
	public void testGetRequiredToolResources() {
		Resource resource = new Resource( ProgramAboutType.uri );
		AboutTool tool = new AboutTool( program, resource );
		Set<URI> resources = tool.getResourceDependencies();
		assertThat( resources, containsInAnyOrder( ProgramGuideType.uri ) );
	}

	@Test
	public void testOpenTool() throws Exception {
		Workpane pane = program.getWorkspaceManager().getActiveWorkspace().getActiveWorkarea().getWorkpane();
		assertThat( pane.getTools().size(), is( 0 ) );

		clickOn( "#menu-help" );
		clickOn( "#menuitem-about" );
		workpaneWatcher.waitForEvent( WorkpaneEvent.Type.TOOL_ADDED );
		workpaneWatcher.waitForEvent( WorkpaneEvent.Type.TOOL_ADDED );
		assertThat( pane.getActiveTool(), instanceOf( AboutTool.class ) );
		assertThat( pane.getTools().size(), is( 2 ) );
	}

	@Test
	public void testOpenToolTwice() throws Exception {
		Workpane pane = program.getWorkspaceManager().getActiveWorkspace().getActiveWorkarea().getWorkpane();
		assertThat( pane.getTools().size(), is( 0 ) );

		clickOn( "#menu-help" );
		clickOn( "#menuitem-about" );
		workpaneWatcher.waitForEvent( WorkpaneEvent.Type.TOOL_ADDED );
		workpaneWatcher.waitForEvent( WorkpaneEvent.Type.TOOL_ADDED );
		assertThat( pane.getActiveTool(), instanceOf( AboutTool.class ) );
		assertThat( pane.getTools().size(), is( 2 ) );

		clickOn( "#menu-help" );
		clickOn( "#menuitem-about" );
		workpaneWatcher.waitForEvent( WorkpaneEvent.Type.TOOL_ACTIVATED );
		assertThat( pane.getTools().size(), is( 2 ) );
	}

	@Test
	public void testClosingResourceWillCloseTool() throws Exception {
		Workpane pane = program.getWorkspaceManager().getActiveWorkspace().getActiveWorkarea().getWorkpane();
		assertThat( pane.getTools().size(), is( 0 ) );

		Future<AbstractTool> future = program.getResourceManager().open( ProgramAboutType.uri );
		workpaneWatcher.waitForEvent( WorkpaneEvent.Type.TOOL_ADDED );
		workpaneWatcher.waitForEvent( WorkpaneEvent.Type.TOOL_ADDED );
		assertThat( pane.getActiveTool(), instanceOf( AboutTool.class ) );
		assertThat( pane.getTools().size(), is( 2 ) );

		program.getResourceManager().closeResources( future.get().getResource() );
		workpaneWatcher.waitForEvent( WorkpaneEvent.Type.TOOL_REMOVED );
		assertThat( pane.getTools().size(), is( 1 ) );
	}

}
