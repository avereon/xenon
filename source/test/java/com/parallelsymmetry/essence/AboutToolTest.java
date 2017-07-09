package com.parallelsymmetry.essence;

import com.parallelsymmetry.essence.tool.ProductInfoTool;
import com.parallelsymmetry.essence.workarea.Workpane;
import com.parallelsymmetry.essence.workarea.WorkpaneEvent;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
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
		workpaneWatcher.waitForEvent( WorkpaneEvent.Type.TOOL_ACTIVATED, 2000 );
		assertThat( pane.getActiveTool(), instanceOf( ProductInfoTool.class ) );
		assertThat( pane.getTools().size(), is( 1 ) );
	}

	@Test
	public void testOpenToolTwice() throws Exception {
		Workpane pane = program.getWorkspaceManager().getActiveWorkspace().getActiveWorkarea().getWorkpane();
		assertThat( pane.getTools().size(), is( 0 ) );

		program.getResourceManager().open( program.getResourceManager().createResource( "program:about" ) );
		workpaneWatcher.waitForEvent( WorkpaneEvent.Type.TOOL_ACTIVATED, 2000 );
		assertThat( pane.getActiveTool(), instanceOf( ProductInfoTool.class ) );
		assertThat( pane.getTools().size(), is( 1 ) );

		workpaneWatcher.clearEvent( WorkpaneEvent.Type.TOOL_ACTIVATED );
		program.getResourceManager().open( program.getResourceManager().createResource( "program:about" ) );
		workpaneWatcher.waitForEvent( WorkpaneEvent.Type.TOOL_ACTIVATED, 2000 );
		assertThat( pane.getTools().size(), is( 1 ) );
	}


	@Test
	public void testCloseTool() throws Exception {
		testOpenTool();
		Workpane pane = program.getWorkspaceManager().getActiveWorkspace().getActiveWorkarea().getWorkpane();
		assertThat( pane.getActiveTool(), instanceOf( ProductInfoTool.class ) );
		assertThat( pane.getTools().size(), is( 1 ) );

		// TODO Close the tool by clicking on the close button
		TabPane tabPane = pane.getActiveView().getToolTabPane();
		Tab tab = pane.getActiveView().getToolTabPane().getTabs().get( 0 );
		//TabPaneSkin skin = (TabPaneSkin) pane.getActiveView().getToolTabPane().getSkin();
		//TabPaneBehavior tabPaneBehavior = (TabPaneBehavior) skin.getBehavior();

//		System.out.println( tab.getTypeSelector() );
//		clickOn( tabPane.lookup( "Tab" ) );
//
//		Thread.sleep( 5000 );
		// Broken in Java 9
		Button button = lookup( ".tab-close-button" ).from( tabPane ).query();

		assertThat( pane.getTools().size(), is( 0 ) );
	}
}
