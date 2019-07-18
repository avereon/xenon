package com.avereon.xenon.workarea;

import com.avereon.xenon.resource.Resource;
import javafx.geometry.Side;
import org.junit.Test;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class WorkpaneDockBottomTest extends WorkpaneTestCase {

	@Test
	public void testDockBottomInLandscapeMode() throws Exception {
		Resource resource = new Resource( "mock:resource" );
		MockTool tool = new MockTool( resource );
		tool.setPlacement( Workpane.Placement.DOCK_BOTTOM );

		// Add the tool
		workpane.setDockMode( Workpane.DockMode.LANDSCAPE );
		workpane.addTool( tool );

		// Check the view placement
		WorkpaneView view = tool.getToolView();
		assertThat( view, is( not( nullValue() ) ) );
		assertThat( view.getPlacement(), is( Workpane.Placement.DOCK_BOTTOM ) );
		assertThat( view.getEdge( Side.LEFT ), is( workpane.getWallEdge( Side.LEFT ) ) );
		assertThat( view.getEdge( Side.BOTTOM ), is( workpane.getWallEdge( Side.BOTTOM ) ) );
		assertThat( view.getEdge( Side.RIGHT ), is( workpane.getWallEdge( Side.RIGHT ) ) );
	}

	@Test
	public void testDockBottomInLandscapeModeWithLeftAndRightDocks() throws Exception {
		Resource resource = new Resource( "mock:resource" );

		MockTool leftTool = new MockTool( resource );
		leftTool.setPlacement( Workpane.Placement.DOCK_LEFT );

		MockTool rightTool = new MockTool( resource );
		rightTool.setPlacement( Workpane.Placement.DOCK_RIGHT );

		MockTool tool = new MockTool( resource );
		tool.setPlacement( Workpane.Placement.DOCK_BOTTOM );

		// Add the tool
		workpane.setDockMode( Workpane.DockMode.LANDSCAPE );
		workpane.addTool( leftTool );
		workpane.addTool( rightTool );
		workpane.addTool( tool );

		// Check the view placement
		WorkpaneView view = tool.getToolView();
		assertThat( view, is( not( nullValue() ) ) );
		assertThat( view.getPlacement(), is( Workpane.Placement.DOCK_BOTTOM ) );
		assertThat( view.getEdge( Side.LEFT ), is( leftTool.getToolView().getEdge( Side.RIGHT ) ) );
		assertThat( view.getEdge( Side.BOTTOM ), is( workpane.getWallEdge( Side.BOTTOM ) ) );
		assertThat( view.getEdge( Side.RIGHT ), is( rightTool.getToolView().getEdge( Side.LEFT ) ) );

		WorkpaneEdge edge = view.getEdge( Side.TOP );
		assertThat( edge.getEdge( Side.LEFT ), is( leftTool.getToolView().getEdge( Side.RIGHT ) ) );
		assertThat( edge.getEdge( Side.RIGHT ), is( rightTool.getToolView().getEdge( Side.LEFT ) ) );
	}

	@Test
	public void testDockBottomInPortraitMode() throws Exception {
		Resource resource = new Resource( "mock:resource" );
		MockTool tool = new MockTool( resource );
		tool.setPlacement( Workpane.Placement.DOCK_BOTTOM );

		// Add the tool
		workpane.setDockMode( Workpane.DockMode.PORTRAIT );
		workpane.addTool( tool );

		// Check the view placement
		WorkpaneView view = tool.getToolView();
		assertThat( view, is( not( nullValue() ) ) );
		assertThat( view.getPlacement(), is( Workpane.Placement.DOCK_BOTTOM ) );
		assertThat( view.getEdge( Side.LEFT ), is( workpane.getWallEdge( Side.LEFT ) ) );
		assertThat( view.getEdge( Side.BOTTOM ), is( workpane.getWallEdge( Side.BOTTOM ) ) );
		assertThat( view.getEdge( Side.RIGHT ), is( workpane.getWallEdge( Side.RIGHT ) ) );
	}

	@Test
	public void testDockBottomInPortraitModeWithLeftAndRightDocks() throws Exception {
		Resource resource = new Resource( "mock:resource" );

		MockTool leftTool = new MockTool( resource );
		leftTool.setPlacement( Workpane.Placement.DOCK_LEFT );

		MockTool rightTool = new MockTool( resource );
		rightTool.setPlacement( Workpane.Placement.DOCK_RIGHT );

		MockTool tool = new MockTool( resource );
		tool.setPlacement( Workpane.Placement.DOCK_BOTTOM );

		// Add the tool
		workpane.setDockMode( Workpane.DockMode.PORTRAIT );
		workpane.addTool( leftTool );
		workpane.addTool( rightTool );
		workpane.addTool( tool );

		// Check the view placement
		WorkpaneView view = tool.getToolView();
		assertThat( view, is( not( nullValue() ) ) );
		assertThat( view.getPlacement(), is( Workpane.Placement.DOCK_BOTTOM ) );
		assertThat( view.getEdge( Side.LEFT ), is( workpane.getWallEdge( Side.LEFT ) ) );
		assertThat( view.getEdge( Side.BOTTOM ), is( workpane.getWallEdge( Side.BOTTOM ) ) );
		assertThat( view.getEdge( Side.RIGHT ), is( workpane.getWallEdge( Side.RIGHT ) ) );
	}

}
