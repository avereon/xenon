package com.xeomar.xenon.workarea;

import com.xeomar.xenon.resource.Resource;
import javafx.geometry.Side;
import org.junit.Test;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class WorkpaneDockLeftTest extends WorkpaneTestCase {

	@Test
	public void testDockLeftInLandscapeMode() throws Exception {
		Resource resource = new Resource( "mock:resource" );
		MockTool tool = new MockTool( resource );
		tool.setPlacement( Workpane.Placement.DOCK_LEFT );

		// Add the tool
		workpane.setDockMode( Workpane.DockMode.LANDSCAPE );
		workpane.addTool( tool );

		// Check the view placement
		WorkpaneView view = tool.getToolView();
		assertThat( view, is( not( nullValue() ) ) );
		assertThat( view.getPlacement(), is( Workpane.Placement.DOCK_LEFT ) );
		assertThat( view.getEdge( Side.TOP ), is( workpane.getWallEdge( Side.TOP ) ) );
		assertThat( view.getEdge( Side.LEFT ), is( workpane.getWallEdge( Side.LEFT ) ) );
		assertThat( view.getEdge( Side.BOTTOM ), is( workpane.getWallEdge( Side.BOTTOM ) ) );
	}

	@Test
	public void testDockLeftInLandscapeModeWithTopAndBottomDocks() throws Exception {
		Resource resource = new Resource( "mock:resource" );

		MockTool topTool = new MockTool( resource );
		topTool.setPlacement( Workpane.Placement.DOCK_TOP );

		MockTool bottomTool = new MockTool( resource );
		bottomTool.setPlacement( Workpane.Placement.DOCK_BOTTOM );

		MockTool tool = new MockTool( resource );
		tool.setPlacement( Workpane.Placement.DOCK_LEFT );

		// Add the tool
		workpane.setDockMode( Workpane.DockMode.LANDSCAPE );
		workpane.addTool( topTool );
		workpane.addTool( bottomTool );
		workpane.addTool( tool );

		// Check the view placement
		WorkpaneView view = tool.getToolView();
		assertThat( view, is( not( nullValue() ) ) );
		assertThat( view.getPlacement(), is( Workpane.Placement.DOCK_LEFT ) );
		assertThat( view.getEdge( Side.TOP ), is( workpane.getWallEdge( Side.TOP ) ) );
		assertThat( view.getEdge( Side.LEFT ), is( workpane.getWallEdge( Side.LEFT ) ) );
		assertThat( view.getEdge( Side.BOTTOM ), is( workpane.getWallEdge( Side.BOTTOM ) ) );
	}

	@Test
	public void testDockLeftInPortraitMode() throws Exception {
		Resource resource = new Resource( "mock:resource" );
		MockTool tool = new MockTool( resource );
		tool.setPlacement( Workpane.Placement.DOCK_LEFT );

		// Add the tool
		workpane.setDockMode( Workpane.DockMode.PORTRAIT );
		workpane.addTool( tool );

		// Check the view placement
		WorkpaneView view = tool.getToolView();
		assertThat( view, is( not( nullValue() ) ) );
		assertThat( view.getPlacement(), is( Workpane.Placement.DOCK_LEFT ) );
		assertThat( view.getEdge( Side.TOP ), is( workpane.getWallEdge( Side.TOP ) ) );
		assertThat( view.getEdge( Side.LEFT ), is( workpane.getWallEdge( Side.LEFT ) ) );
		assertThat( view.getEdge( Side.BOTTOM ), is( workpane.getWallEdge( Side.BOTTOM ) ) );
	}

	@Test
	public void testDockLeftInPortraitModeWithTopAndBottomDocks() throws Exception {
		Resource resource = new Resource( "mock:resource" );

		MockTool topTool = new MockTool( resource );
		topTool.setPlacement( Workpane.Placement.DOCK_TOP );

		MockTool bottomTool = new MockTool( resource );
		bottomTool.setPlacement( Workpane.Placement.DOCK_BOTTOM );

		MockTool tool = new MockTool( resource );
		tool.setPlacement( Workpane.Placement.DOCK_LEFT );

		// Add the tool
		workpane.setDockMode( Workpane.DockMode.PORTRAIT );
		workpane.addTool( topTool );
		workpane.addTool( bottomTool );
		workpane.addTool( tool );

		// Check the view placement
		WorkpaneView view = tool.getToolView();
		assertThat( view, is( not( nullValue() ) ) );
		assertThat( view.getPlacement(), is( Workpane.Placement.DOCK_LEFT ) );
		assertThat( view.getEdge( Side.TOP ), is( topTool.getToolView().getEdge( Side.BOTTOM ) ) );
		assertThat( view.getEdge( Side.LEFT ), is( workpane.getWallEdge( Side.LEFT ) ) );
		assertThat( view.getEdge( Side.BOTTOM ), is( bottomTool.getToolView().getEdge( Side.TOP ) ) );

		WorkpaneEdge edge = view.getEdge( Side.RIGHT );
		assertThat( edge.getEdge( Side.TOP ), is( topTool.getToolView().getEdge( Side.BOTTOM ) ) );
		assertThat( edge.getEdge( Side.BOTTOM ), is( bottomTool.getToolView().getEdge( Side.TOP ) ) );
	}

}
