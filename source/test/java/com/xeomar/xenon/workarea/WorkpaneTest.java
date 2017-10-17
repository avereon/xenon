package com.xeomar.xenon.workarea;

import com.xeomar.xenon.worktool.Tool;
import javafx.geometry.Side;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import org.hamcrest.CustomTypeSafeMatcher;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import org.junit.Test;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class WorkpaneTest extends WorkpaneTestCase {

	@Test
	public void testAddRemoveToolEvents() {
		int index = 0;
		MockTool tool = new MockTool( resource );

		// Add the tool but do not set it active.
		workpane.addTool( tool, false );
		assertThat( tool, nextEvent( isMethod( MockTool.ALLOCATE ) ) );
		assertThat( tool, nextEvent( isMethod( MockTool.DISPLAY ) ) );
		// Didn't choose to select the tool, so no activate event
		assertThat( tool.getEvents().size(), is( 2 ) );

		// Even though the tool was not activated, it is still the active tool in the toolview
		assertThat( tool.getToolView().getActiveTool(), is( tool ) );

		// Remove the tool.
		WorkpaneView view = tool.getToolView();
		workpane.removeTool( tool );
		// Didn't choose to select the tool, so no deactivate event
		assertThat( tool, nextEvent( isMethod( MockTool.CONCEAL ) ) );
		assertThat( tool, nextEvent( isMethod( MockTool.DEALLOCATE ) ) );
		assertThat( tool.getEvents().size(), is( 4 ) );

		// The active tool in the toolview should now be null
		assertThat( tool.getToolView(), is( nullValue() ) );
		assertThat( view.getActiveTool(), is( nullValue() ) );
	}

	@Test
	public void testAddSelectRemoveToolEvents() {
		int index = 0;
		MockTool tool = new MockTool( resource );
		assertThat( workpane.getActiveTool(), is( nullValue() ) );

		// Add the tool and set it active.
		workpane.addTool( tool, true );
		assertThat( tool, nextEvent( isMethod( MockTool.ALLOCATE ) ) );
		assertThat( tool, nextEvent( isMethod( MockTool.DISPLAY ) ) );
		assertThat( tool, nextEvent( isMethod( MockTool.ACTIVATE ) ) );
		assertThat( tool.getEvents().size(), is( 3 ) );

		// Even though the tool was not activated, it is still the active tool in the toolview
		assertThat( tool.getToolView().getActiveTool(), is( tool ) );

		// Remove the tool.
		WorkpaneView view = tool.getToolView();
		workpane.removeTool( tool );
		assertThat( tool, nextEvent( isMethod( MockTool.DEACTIVATE ) ) );
		assertThat( tool, nextEvent( isMethod( MockTool.CONCEAL ) ) );
		assertThat( tool, nextEvent( isMethod( MockTool.DEALLOCATE ) ) );
		assertThat( tool.getEvents().size(), is( 6 ) );

		// The active tool in the toolview should now be null
		assertThat( tool.getToolView(), is( nullValue() ) );
		assertThat( view.getActiveTool(), is( nullValue() ) );
	}

	@Test
	public void testSelectToolEvents() {
		int index = 0;
		MockTool tool1 = new MockTool( resource );
		MockTool tool2 = new MockTool( resource );
		MockTool tool3 = new MockTool( resource );
		assertThat( tool1.getEvents().size(), is( 0 ) );
		assertThat( tool2.getEvents().size(), is( 0 ) );
		assertThat( tool3.getEvents().size(), is( 0 ) );
		int index1 = 0;
		int index2 = 0;
		int index3 = 0;

		// Add tool one but do not activate it.
		workpane.addTool( tool1, false );
		assertThat( tool1, nextEvent( isMethod( MockTool.ALLOCATE ) ) );
		assertThat( tool1, nextEvent( isMethod( MockTool.DISPLAY ) ) );
		assertThat( tool1.getEvents().size(), is( 2 ) );

		// Add tool two but do not activate it.
		workpane.addTool( tool2, false );
		assertThat( tool2, nextEvent( isMethod( MockTool.ALLOCATE ) ) );
		assertThat( tool2.getEvents().size(), is( 1 ) );

		// Add tool three and activate it.
		workpane.addTool( tool3, true );
		assertThat( tool1, nextEvent( isMethod( MockTool.CONCEAL ) ) );
		assertThat( tool1.getEvents().size(), is( 3 ) );
		assertThat( tool3, nextEvent( isMethod( MockTool.ALLOCATE ) ) );
		assertThat( tool3, nextEvent( isMethod( MockTool.DISPLAY ) ) );
		assertThat( tool3, nextEvent( isMethod( MockTool.ACTIVATE ) ) );
		assertThat( tool3.getEvents().size(), is( 3 ) );

		// Try to set tool three active again.
		workpane.setActiveTool( tool3 );
		assertThat( tool3, nextEvent( isMethod( MockTool.DEACTIVATE ) ) );
		assertThat( tool3, nextEvent( isMethod( MockTool.ACTIVATE ) ) );
		assertThat( tool3.getEvents().size(), is( 5 ) );

		// Set tool two active.
		workpane.setActiveTool( tool2 );
		assertThat( tool3, nextEvent( isMethod( MockTool.DEACTIVATE ) ) );
		assertThat( tool3, nextEvent( isMethod( MockTool.CONCEAL ) ) );
		assertThat( tool3.getEvents().size(), is( 7 ) );
		assertThat( tool2, nextEvent( isMethod( MockTool.DISPLAY ) ) );
		assertThat( tool2, nextEvent( isMethod( MockTool.ACTIVATE ) ) );
		assertThat( tool2.getEvents().size(), is( 3 ) );

		// Remove tool one.
		// This tests the removal of an inactive tool.
		workpane.removeTool( tool1 );
		assertThat( tool1, nextEvent( isMethod( MockTool.DEALLOCATE ) ) );
		assertThat( tool1.getEvents().size(), is( 4 ) );

		// Remove tool two.
		// This tests the removal of an active tool when there is more than one tool.
		workpane.removeTool( tool2 );
		assertThat( tool2, nextEvent( isMethod( MockTool.DEACTIVATE ) ) );
		assertThat( tool2, nextEvent( isMethod( MockTool.CONCEAL ) ) );
		assertThat( tool2, nextEvent( isMethod( MockTool.DEALLOCATE ) ) );
		assertThat( tool2.getEvents().size(), is( 6 ) );
		assertThat( tool3, nextEvent( isMethod( MockTool.DISPLAY ) ) );
		assertThat( tool3, nextEvent( isMethod( MockTool.ACTIVATE ) ) );
		assertThat( tool3.getEvents().size(), is( 9 ) );
	}

	@Test
	public void testSetActiveTool() throws Exception {
		Tool tool1 = new MockTool( resource );
		Tool tool2 = new MockTool( resource );
		Tool tool3 = new MockTool( resource );

		workpane.addTool( tool1, false );
		workpane.addTool( tool2, false );
		workpane.addTool( tool3, false );

		assertThat( getActiveTool( toolview ), is( tool1 ) );
		workpane.setActiveTool( tool2 );
		assertThat( getActiveTool( toolview ), is( tool2 ) );
		workpane.setActiveTool( tool3 );
		assertThat( getActiveTool( toolview ), is( tool3 ) );
		workpane.setActiveTool( tool1 );
		assertThat( getActiveTool( toolview ), is( tool1 ) );
	}

	@Test
	public void testDockMode() throws Exception {
		assertThat( workpane.getDockMode(), is( Workpane.DEFAULT_DOCK_MODE ) );

		workpane.setDockMode( Workpane.DockMode.PORTRAIT );
		assertThat( workpane.getDockMode(), is( Workpane.DockMode.PORTRAIT ) );

		workpane.setDockMode( Workpane.DockMode.LANDSCAPE );
		assertThat( workpane.getDockMode(), is( Workpane.DockMode.LANDSCAPE ) );

		workpane.setDockMode( Workpane.DEFAULT_DOCK_MODE );
		assertThat( workpane.getDockMode(), is( Workpane.DEFAULT_DOCK_MODE ) );
	}

	@Test
	public void testSplitNorth() throws Exception {
		WorkpaneView view1 = workpane.split( Side.TOP );
		assertThat( toolview.topEdge.getPosition(), is( Workpane.DEFAULT_WALL_SPLIT_RATIO ) );
		assertThat( toolview.bottomEdge.getPosition(), is( 1d ) );
		assertThat( toolview.leftEdge.getPosition(), is( 0d ) );
		assertThat( toolview.rightEdge.getPosition(), is( 1d ) );

		assertThat( view1.topEdge.getPosition(), is( 0d ) );
		assertThat( view1.bottomEdge.getPosition(), is( Workpane.DEFAULT_WALL_SPLIT_RATIO ) );
		assertThat( view1.leftEdge.getPosition(), is( 0d ) );
		assertThat( view1.rightEdge.getPosition(), is( 1d ) );
	}

	// TODO Continue implementing test methods

	@Test
	public void testSplitSouth() throws Exception {
		WorkpaneView view1 = workpane.split( Side.BOTTOM );
		assertThat( toolview.topEdge.getPosition(), is( 0d ) );
		assertThat( toolview.bottomEdge.getPosition(), is( 1 - Workpane.DEFAULT_WALL_SPLIT_RATIO ) );
		assertThat( toolview.leftEdge.getPosition(), is( 0d ) );
		assertThat( toolview.rightEdge.getPosition(), is( 1d ) );

		assertThat( view1.topEdge.getPosition(), is( 1 - Workpane.DEFAULT_WALL_SPLIT_RATIO ) );
		assertThat( view1.bottomEdge.getPosition(), is( 1d ) );
		assertThat( view1.leftEdge.getPosition(), is( 0d ) );
		assertThat( view1.rightEdge.getPosition(), is( 1d ) );
	}

	@Test
	public void testSplitWest() throws Exception {
		WorkpaneView view1 = workpane.split( Side.LEFT );
		assertThat( toolview.topEdge.getPosition(), is( 0d ) );
		assertThat( toolview.bottomEdge.getPosition(), is( 1d ) );
		assertThat( toolview.leftEdge.getPosition(), is( Workpane.DEFAULT_WALL_SPLIT_RATIO ) );
		assertThat( toolview.rightEdge.getPosition(), is( 1d ) );

		assertThat( view1.topEdge.getPosition(), is( 0d ) );
		assertThat( view1.bottomEdge.getPosition(), is( 1d ) );
		assertThat( view1.leftEdge.getPosition(), is( 0d ) );
		assertThat( view1.rightEdge.getPosition(), is( Workpane.DEFAULT_WALL_SPLIT_RATIO ) );
	}

	@Test
	public void testSplitEast() throws Exception {
		WorkpaneView view1 = workpane.split( Side.RIGHT );
		assertThat( toolview.topEdge.getPosition(), is( 0d ) );
		assertThat( toolview.bottomEdge.getPosition(), is( 1d ) );
		assertThat( toolview.leftEdge.getPosition(), is( 0d ) );
		assertThat( toolview.rightEdge.getPosition(), is( 1 - Workpane.DEFAULT_WALL_SPLIT_RATIO ) );

		assertThat( view1.topEdge.getPosition(), is( 0d ) );
		assertThat( view1.bottomEdge.getPosition(), is( 1d ) );
		assertThat( view1.leftEdge.getPosition(), is( 1 - Workpane.DEFAULT_WALL_SPLIT_RATIO ) );
		assertThat( view1.rightEdge.getPosition(), is( 1d ) );
	}

	@Test
	public void testSplitNorthCompound() throws Exception {
		WorkpaneView view1 = workpane.split( toolview, Side.LEFT );
		assertThat( toolview.topEdge.getPosition(), is( 0d ) );
		assertThat( toolview.bottomEdge.getPosition(), is( 1d ) );
		assertThat( toolview.leftEdge.getPosition(), is( Workpane.DEFAULT_VIEW_SPLIT_RATIO ) );
		assertThat( toolview.rightEdge.getPosition(), is( 1d ) );

		assertThat( view1.topEdge.getPosition(), is( 0d ) );
		assertThat( view1.bottomEdge.getPosition(), is( 1d ) );
		assertThat( view1.leftEdge.getPosition(), is( 0d ) );
		assertThat( view1.rightEdge.getPosition(), is( Workpane.DEFAULT_VIEW_SPLIT_RATIO ) );

		WorkpaneView view2 = workpane.split( Side.TOP );
		assertThat( toolview.topEdge.getPosition(), is( Workpane.DEFAULT_WALL_SPLIT_RATIO ) );
		assertThat( toolview.bottomEdge.getPosition(), is( 1d ) );
		assertThat( toolview.leftEdge.getPosition(), is( Workpane.DEFAULT_VIEW_SPLIT_RATIO ) );
		assertThat( toolview.rightEdge.getPosition(), is( 1d ) );

		assertThat( view1.topEdge.getPosition(), is( Workpane.DEFAULT_WALL_SPLIT_RATIO ) );
		assertThat( view1.bottomEdge.getPosition(), is( 1d ) );
		assertThat( view1.leftEdge.getPosition(), is( 0d ) );
		assertThat( view1.rightEdge.getPosition(), is( Workpane.DEFAULT_VIEW_SPLIT_RATIO ) );

		assertThat( view2.topEdge.getPosition(), is( 0d ) );
		assertThat( view2.bottomEdge.getPosition(), is( Workpane.DEFAULT_WALL_SPLIT_RATIO ) );
		assertThat( view2.leftEdge.getPosition(), is( 0d ) );
		assertThat( view2.rightEdge.getPosition(), is( 1d ) );
	}

	@Test
	public void testSplitSouthCompound() throws Exception {
		WorkpaneView view1 = workpane.split( toolview, Side.LEFT );
		assertThat( toolview.topEdge.getPosition(), is( 0d ) );
		assertThat( toolview.bottomEdge.getPosition(), is( 1d ) );
		assertThat( toolview.leftEdge.getPosition(), is( Workpane.DEFAULT_VIEW_SPLIT_RATIO ) );
		assertThat( toolview.rightEdge.getPosition(), is( 1d ) );

		assertThat( view1.topEdge.getPosition(), is( 0d ) );
		assertThat( view1.bottomEdge.getPosition(), is( 1d ) );
		assertThat( view1.leftEdge.getPosition(), is( 0d ) );
		assertThat( view1.rightEdge.getPosition(), is( Workpane.DEFAULT_VIEW_SPLIT_RATIO ) );

		WorkpaneView view2 = workpane.split( Side.BOTTOM );
		assertThat( toolview.topEdge.getPosition(), is( 0d ) );
		assertThat( toolview.bottomEdge.getPosition(), is( 1 - Workpane.DEFAULT_WALL_SPLIT_RATIO ) );
		assertThat( toolview.leftEdge.getPosition(), is( Workpane.DEFAULT_VIEW_SPLIT_RATIO ) );
		assertThat( toolview.rightEdge.getPosition(), is( 1d ) );

		assertThat( view1.topEdge.getPosition(), is( 0d ) );
		assertThat( view1.bottomEdge.getPosition(), is( 1 - Workpane.DEFAULT_WALL_SPLIT_RATIO ) );
		assertThat( view1.leftEdge.getPosition(), is( 0d ) );
		assertThat( view1.rightEdge.getPosition(), is( Workpane.DEFAULT_VIEW_SPLIT_RATIO ) );

		assertThat( view2.topEdge.getPosition(), is( 1 - Workpane.DEFAULT_WALL_SPLIT_RATIO ) );
		assertThat( view2.bottomEdge.getPosition(), is( 1d ) );
		assertThat( view2.leftEdge.getPosition(), is( 0d ) );
		assertThat( view2.rightEdge.getPosition(), is( 1d ) );
	}

	@Test
	public void testSplitEastCompound() throws Exception {
		WorkpaneView view1 = workpane.split( toolview, Side.TOP );
		assertThat( toolview.topEdge.getPosition(), is( Workpane.DEFAULT_VIEW_SPLIT_RATIO ) );
		assertThat( toolview.bottomEdge.getPosition(), is( 1d ) );
		assertThat( toolview.leftEdge.getPosition(), is( 0d ) );
		assertThat( toolview.rightEdge.getPosition(), is( 1d ) );

		assertThat( view1.topEdge.getPosition(), is( 0d ) );
		assertThat( view1.bottomEdge.getPosition(), is( Workpane.DEFAULT_VIEW_SPLIT_RATIO ) );
		assertThat( view1.leftEdge.getPosition(), is( 0d ) );
		assertThat( view1.rightEdge.getPosition(), is( 1d ) );

		WorkpaneView view2 = workpane.split( Side.RIGHT );
		assertThat( toolview.topEdge.getPosition(), is( Workpane.DEFAULT_VIEW_SPLIT_RATIO ) );
		assertThat( toolview.bottomEdge.getPosition(), is( 1d ) );
		assertThat( toolview.leftEdge.getPosition(), is( 0d ) );
		assertThat( toolview.rightEdge.getPosition(), is( 1 - Workpane.DEFAULT_WALL_SPLIT_RATIO ) );

		assertThat( view1.topEdge.getPosition(), is( 0d ) );
		assertThat( view1.bottomEdge.getPosition(), is( Workpane.DEFAULT_VIEW_SPLIT_RATIO ) );
		assertThat( view1.leftEdge.getPosition(), is( 0d ) );
		assertThat( view1.rightEdge.getPosition(), is( 1.0 - Workpane.DEFAULT_WALL_SPLIT_RATIO ) );

		assertThat( view2.topEdge.getPosition(), is( 0d ) );
		assertThat( view2.bottomEdge.getPosition(), is( 1d ) );
		assertThat( view2.leftEdge.getPosition(), is( 1 - Workpane.DEFAULT_WALL_SPLIT_RATIO ) );
		assertThat( view2.rightEdge.getPosition(), is( 1d ) );
	}

	@Test
	public void testSplitWestCompound() throws Exception {
		WorkpaneView view1 = workpane.split( toolview, Side.TOP );
		assertThat( toolview.topEdge.getPosition(), is( Workpane.DEFAULT_VIEW_SPLIT_RATIO ) );
		assertThat( toolview.bottomEdge.getPosition(), is( 1d ) );
		assertThat( toolview.leftEdge.getPosition(), is( 0d ) );
		assertThat( toolview.rightEdge.getPosition(), is( 1d ) );

		assertThat( view1.topEdge.getPosition(), is( 0d ) );
		assertThat( view1.bottomEdge.getPosition(), is( Workpane.DEFAULT_VIEW_SPLIT_RATIO ) );
		assertThat( view1.leftEdge.getPosition(), is( 0d ) );
		assertThat( view1.rightEdge.getPosition(), is( 1d ) );

		WorkpaneView view2 = workpane.split( Side.LEFT );
		assertThat( toolview.topEdge.getPosition(), is( Workpane.DEFAULT_VIEW_SPLIT_RATIO ) );
		assertThat( toolview.bottomEdge.getPosition(), is( 1d ) );
		assertThat( toolview.leftEdge.getPosition(), is( Workpane.DEFAULT_WALL_SPLIT_RATIO ) );
		assertThat( toolview.rightEdge.getPosition(), is( 1d ) );

		assertThat( view1.topEdge.getPosition(), is( 0d ) );
		assertThat( view1.bottomEdge.getPosition(), is( Workpane.DEFAULT_VIEW_SPLIT_RATIO ) );
		assertThat( view1.leftEdge.getPosition(), is( Workpane.DEFAULT_WALL_SPLIT_RATIO ) );
		assertThat( view1.rightEdge.getPosition(), is( 1d ) );

		assertThat( view2.topEdge.getPosition(), is( 0d ) );
		assertThat( view2.bottomEdge.getPosition(), is( 1d ) );
		assertThat( view2.leftEdge.getPosition(), is( 0d ) );
		assertThat( view2.rightEdge.getPosition(), is( Workpane.DEFAULT_WALL_SPLIT_RATIO ) );
	}

	@Test
	public void testSplitViewNorth() throws Exception {
		WorkpaneView view1 = workpane.split( toolview, Side.TOP );
		assertThat( toolview.topEdge.getPosition(), is( Workpane.DEFAULT_VIEW_SPLIT_RATIO ) );
		assertThat( toolview.bottomEdge.getPosition(), is( 1d ) );
		assertThat( toolview.leftEdge.getPosition(), is( 0d ) );
		assertThat( toolview.rightEdge.getPosition(), is( 1d ) );

		assertThat( view1.topEdge.getPosition(), is( 0d ) );
		assertThat( view1.bottomEdge.getPosition(), is( Workpane.DEFAULT_VIEW_SPLIT_RATIO ) );
		assertThat( view1.leftEdge.getPosition(), is( 0d ) );
		assertThat( view1.rightEdge.getPosition(), is( 1d ) );
	}

	@Test
	public void testSplitViewNorthWithSize() throws Exception {
		double size = 0.31;
		WorkpaneView view1 = workpane.split( toolview, Side.TOP, size );
		assertThat( toolview.topEdge.getPosition(), is( size ) );
		assertThat( toolview.bottomEdge.getPosition(), is( 1d ) );
		assertThat( toolview.leftEdge.getPosition(), is( 0d ) );
		assertThat( toolview.rightEdge.getPosition(), is( 1d ) );

		assertThat( view1.topEdge.getPosition(), is( 0d ) );
		assertThat( view1.bottomEdge.getPosition(), is( size ) );
		assertThat( view1.leftEdge.getPosition(), is( 0d ) );
		assertThat( view1.rightEdge.getPosition(), is( 1d ) );
	}

	@Test
	public void testSplitViewSouth() throws Exception {
		WorkpaneView view1 = workpane.split( toolview, Side.BOTTOM );
		assertThat( toolview.topEdge.getPosition(), is( 0d ) );
		assertThat( toolview.bottomEdge.getPosition(), is( 1 - Workpane.DEFAULT_VIEW_SPLIT_RATIO ) );
		assertThat( toolview.leftEdge.getPosition(), is( 0d ) );
		assertThat( toolview.rightEdge.getPosition(), is( 1d ) );

		assertThat( view1.topEdge.getPosition(), is( 1 - Workpane.DEFAULT_VIEW_SPLIT_RATIO ) );
		assertThat( view1.bottomEdge.getPosition(), is( 1d ) );
		assertThat( view1.leftEdge.getPosition(), is( 0d ) );
		assertThat( view1.rightEdge.getPosition(), is( 1d ) );
	}

	@Test
	public void testSplitViewSouthWithSize() throws Exception {
		double size = 0.33;
		WorkpaneView view1 = workpane.split( toolview, Side.BOTTOM, size );
		assertThat( toolview.topEdge.getPosition(), is( 0d ) );
		assertThat( toolview.bottomEdge.getPosition(), is( 1 - size ) );
		assertThat( toolview.leftEdge.getPosition(), is( 0d ) );
		assertThat( toolview.rightEdge.getPosition(), is( 1d ) );

		assertThat( view1.topEdge.getPosition(), is( 1 - size ) );
		assertThat( view1.bottomEdge.getPosition(), is( 1d ) );
		assertThat( view1.leftEdge.getPosition(), is( 0d ) );
		assertThat( view1.rightEdge.getPosition(), is( 1d ) );
	}

	@Test
	public void testSplitViewWest() throws Exception {
		WorkpaneView view1 = workpane.split( toolview, Side.LEFT );
		assertThat( toolview.topEdge.getPosition(), is( 0d ) );
		assertThat( toolview.bottomEdge.getPosition(), is( 1d ) );
		assertThat( toolview.leftEdge.getPosition(), is( Workpane.DEFAULT_VIEW_SPLIT_RATIO ) );
		assertThat( toolview.rightEdge.getPosition(), is( 1d ) );

		assertThat( view1.topEdge.getPosition(), is( 0d ) );
		assertThat( view1.bottomEdge.getPosition(), is( 1d ) );
		assertThat( view1.leftEdge.getPosition(), is( 0d ) );
		assertThat( view1.rightEdge.getPosition(), is( Workpane.DEFAULT_VIEW_SPLIT_RATIO ) );
	}

	@Test
	public void testSplitViewWestWithSize() throws Exception {
		double size = 0.35;
		WorkpaneView view1 = workpane.split( toolview, Side.LEFT, size );
		assertThat( toolview.topEdge.getPosition(), is( 0d ) );
		assertThat( toolview.bottomEdge.getPosition(), is( 1d ) );
		assertThat( toolview.leftEdge.getPosition(), is( size ) );
		assertThat( toolview.rightEdge.getPosition(), is( 1d ) );

		assertThat( view1.topEdge.getPosition(), is( 0d ) );
		assertThat( view1.bottomEdge.getPosition(), is( 1d ) );
		assertThat( view1.leftEdge.getPosition(), is( 0d ) );
		assertThat( view1.rightEdge.getPosition(), is( size ) );
	}

	@Test
	public void testSplitViewEast() throws Exception {
		WorkpaneView view1 = workpane.split( toolview, Side.RIGHT );
		assertThat( toolview.topEdge.getPosition(), is( 0d ) );
		assertThat( toolview.bottomEdge.getPosition(), is( 1d ) );
		assertThat( toolview.leftEdge.getPosition(), is( 0d ) );
		assertThat( toolview.rightEdge.getPosition(), is( 1 - Workpane.DEFAULT_VIEW_SPLIT_RATIO ) );

		assertThat( view1.topEdge.getPosition(), is( 0d ) );
		assertThat( view1.bottomEdge.getPosition(), is( 1d ) );
		assertThat( view1.leftEdge.getPosition(), is( 1 - Workpane.DEFAULT_VIEW_SPLIT_RATIO ) );
		assertThat( view1.rightEdge.getPosition(), is( 1d ) );
	}

	@Test
	public void testSplitViewEastWithSize() throws Exception {
		double size = 0.37;
		WorkpaneView view1 = workpane.split( toolview, Side.RIGHT, size );
		assertThat( toolview.topEdge.getPosition(), is( 0d ) );
		assertThat( toolview.bottomEdge.getPosition(), is( 1d ) );
		assertThat( toolview.leftEdge.getPosition(), is( 0d ) );
		assertThat( toolview.rightEdge.getPosition(), is( 1 - size ) );

		assertThat( view1.topEdge.getPosition(), is( 0d ) );
		assertThat( view1.bottomEdge.getPosition(), is( 1d ) );
		assertThat( view1.leftEdge.getPosition(), is( 1 - size ) );
		assertThat( view1.rightEdge.getPosition(), is( 1d ) );
	}

	private Tool getActiveTool( WorkpaneView view ) throws Exception {
		TabPane pane = (TabPane)view.getChildren().get( 0 );

		int selectedIndex = pane.getSelectionModel().getSelectedIndex();
		Tab tab = pane.getTabs().get( selectedIndex );

		return (Tool)tab.getContent();
	}

	private static Matcher<MockTool> hasEvent( int index ) {
		return hasEvent( index, not( nullValue() ) );
	}

	private static <T> Matcher<MockTool> nextEvent( Matcher<T> valueMatcher ) {
		return new FeatureMatcher<MockTool,T>( valueMatcher, "Tool method call", "method call" ) {

			@Override
			@SuppressWarnings( "unchecked" )
			protected T featureValueOf( MockTool tool ) {
				try {
					return (T)tool.getNextEvent();
				} catch( ArrayIndexOutOfBoundsException exception ) {
					return null;
				}
			}

		};
	}

	private static <T> Matcher<MockTool> hasEvent( int index, Matcher<T> valueMatcher ) {
		return new FeatureMatcher<MockTool,T>( valueMatcher, "Tool method call", "method call" ) {

			@Override
			@SuppressWarnings( "unchecked" )
			protected T featureValueOf( MockTool tool ) {
				try {
					return (T)tool.getEvents().get( index );
				} catch( ArrayIndexOutOfBoundsException exception ) {
					return null;
				}
			}

		};
	}

	private static Matcher<MockTool.MethodCall> isMethod( String name ) {
		return new CustomTypeSafeMatcher<MockTool.MethodCall>( "matching " + name ) {

			@Override
			protected boolean matchesSafely( MockTool.MethodCall event ) {
				return event != null && event.method.equals( name );
			}

		};
	}

}
