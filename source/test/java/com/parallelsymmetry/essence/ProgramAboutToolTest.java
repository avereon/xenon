package com.parallelsymmetry.essence;

import com.parallelsymmetry.essence.resource.event.ResourceLoadedEvent;
import com.parallelsymmetry.essence.tool.ProductInfoTool;
import com.parallelsymmetry.essence.workarea.Workpane;
import com.parallelsymmetry.essence.workarea.WorkpaneEvent;
import com.parallelsymmetry.essence.workarea.WorkpaneWatcher;
import javafx.stage.Stage;
import org.junit.Test;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class ProgramAboutToolTest extends FxProgramTestCase {

	@Test
	public void testOpenTool() throws Exception {
		Stage stage = program.getWorkspaceManager().getActiveWorkspace().getStage();
		assertThat( stage.isShowing(), is( true ) );

		WorkpaneWatcher workpaneWatcher = new WorkpaneWatcher();
		program.getWorkspaceManager().getActiveWorkspace().getActiveWorkarea().getWorkpane().addWorkPaneListener( workpaneWatcher );

		program.getResourceManager().open( program.getResourceManager().createResource( "program:about" ) );
		waitForEvent( ResourceLoadedEvent.class );

		workpaneWatcher.waitForEvent( WorkpaneEvent.Type.TOOL_ACTIVATED, 2000 );

		Workpane pane = program.getWorkspaceManager().getActiveWorkspace().getActiveWorkarea().getWorkpane();
		assertThat( pane.getActiveTool(), instanceOf( ProductInfoTool.class ) );
	}

}
