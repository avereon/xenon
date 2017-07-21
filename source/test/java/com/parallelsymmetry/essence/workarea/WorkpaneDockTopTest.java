package com.parallelsymmetry.essence.workarea;

import com.parallelsymmetry.essence.resource.Resource;
import javafx.geometry.Side;
import org.junit.Test;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class WorkpaneDockTopTest extends WorkpaneTestCase {

	@Test
	public void testDockTopInLandscapeMode() throws Exception {
		Resource resource = new Resource( "mock:resource" );
		MockTool tool = new MockTool( resource );
		tool.setPlacement( Workpane.Placement.DOCK_TOP );

		// Add the tool
		workpane.setDockMode( Workpane.DockMode.LANDSCAPE );
		workpane.addTool( tool );

		// Check the view placement
		WorkpaneView view = tool.getToolView();
		assertThat( view, is( not( nullValue() ) ) );
		assertThat( view.getPlacement(), is( Workpane.Placement.DOCK_TOP ) );
		assertThat( view.getEdge( Side.LEFT ), is( workpane.getWallEdge( Side.LEFT ) ) );
		assertThat( view.getEdge( Side.TOP ), is( workpane.getWallEdge( Side.TOP ) ) );
		assertThat( view.getEdge( Side.RIGHT ), is( workpane.getWallEdge( Side.RIGHT ) ) );
	}

	@Test
	public void testDockTopInLandscapeModeWithTopAndBottomDocks() throws Exception {
		Resource resource = new Resource( "mock:resource" );

		MockTool leftTool = new MockTool( resource );
		leftTool.setPlacement( Workpane.Placement.DOCK_LEFT );

		MockTool rightTool = new MockTool( resource );
		rightTool.setPlacement( Workpane.Placement.DOCK_RIGHT );

		MockTool tool = new MockTool( resource );
		tool.setPlacement( Workpane.Placement.DOCK_TOP );

		// Add the tool
		workpane.setDockMode( Workpane.DockMode.LANDSCAPE );
		workpane.addTool( leftTool );
		workpane.addTool( rightTool );
		workpane.addTool( tool );

		// Check the view placement
		WorkpaneView view = tool.getToolView();
		assertThat( view, is( not( nullValue() ) ) );
		assertThat( view.getPlacement(), is( Workpane.Placement.DOCK_TOP ) );
		assertThat( view.getEdge( Side.LEFT ), is( leftTool.getToolView().getEdge( Side.RIGHT ) ) );
		assertThat( view.getEdge( Side.TOP ), is( workpane.getWallEdge( Side.TOP ) ) );
		assertThat( view.getEdge( Side.RIGHT ), is( rightTool.getToolView().getEdge( Side.LEFT ) ) );

		WorkpaneEdge edge = view.getEdge( Side.BOTTOM );
		assertThat( edge.getEdge( Side.LEFT ), is( leftTool.getToolView().getEdge( Side.RIGHT ) ) );
		assertThat( edge.getEdge( Side.RIGHT ), is( rightTool.getToolView().getEdge( Side.LEFT ) ) );
	}

	@Test
	public void testDockTopInPortraitMode() throws Exception {
		Resource resource = new Resource( "mock:resource" );
		MockTool tool = new MockTool( resource );
		tool.setPlacement( Workpane.Placement.DOCK_TOP );

		// Add the tool
		workpane.setDockMode( Workpane.DockMode.PORTRAIT );
		workpane.addTool( tool );

		// Check the view placement
		WorkpaneView view = tool.getToolView();
		assertThat( view, is( not( nullValue() ) ) );
		assertThat( view.getPlacement(), is( Workpane.Placement.DOCK_TOP ) );
		assertThat( view.getEdge( Side.LEFT ), is( workpane.getWallEdge( Side.LEFT ) ) );
		assertThat( view.getEdge( Side.TOP ), is( workpane.getWallEdge( Side.TOP ) ) );
		assertThat( view.getEdge( Side.RIGHT ), is( workpane.getWallEdge( Side.RIGHT ) ) );
	}

	@Test
	public void testDockTopInPortraitModeWithTopAndBottomDocks() throws Exception {
		Resource resource = new Resource( "mock:resource" );

		MockTool leftTool = new MockTool( resource );
		leftTool.setPlacement( Workpane.Placement.DOCK_LEFT );

		MockTool rightTool = new MockTool( resource );
		rightTool.setPlacement( Workpane.Placement.DOCK_RIGHT );

		MockTool tool = new MockTool( resource );
		tool.setPlacement( Workpane.Placement.DOCK_TOP );

		// Add the tool
		workpane.setDockMode( Workpane.DockMode.PORTRAIT );
		workpane.addTool( leftTool );
		workpane.addTool( rightTool );
		workpane.addTool( tool );

		// Check the view placement
		WorkpaneView view = tool.getToolView();
		assertThat( view, is( not( nullValue() ) ) );
		assertThat( view.getPlacement(), is( Workpane.Placement.DOCK_TOP ) );
		assertThat( view.getEdge( Side.LEFT ), is( workpane.getWallEdge( Side.LEFT ) ) );
		assertThat( view.getEdge( Side.TOP ), is( workpane.getWallEdge( Side.TOP ) ) );
		assertThat( view.getEdge( Side.RIGHT ), is( workpane.getWallEdge( Side.RIGHT ) ) );
	}

}
