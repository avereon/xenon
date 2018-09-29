package com.xeomar.xenon.tool.guide;

import com.xeomar.settings.MapSettings;
import com.xeomar.xenon.FxProgramTestCase;
import com.xeomar.xenon.ProgramProduct;
import com.xeomar.xenon.resource.Resource;
import com.xeomar.xenon.resource.type.ProgramSettingsType;
import com.xeomar.xenon.util.FxUtil;
import javafx.application.Platform;
import org.junit.Before;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class GuidedToolTest extends FxProgramTestCase {

	private MockGuidedTool mockGuidedTool;

	@Before
	public void setup() throws Exception {
		super.setup();

		Resource resource = program.getResourceManager().createResource( ProgramSettingsType.URI );
		this.mockGuidedTool = new MockGuidedTool( program, resource );
		this.mockGuidedTool.setSettings( new MapSettings() );
		// Need to call resourceReady to register the listeners
		this.mockGuidedTool.resourceReady( null );
		mockGuidedTool.reset();
	}

	@Test
	public void testGuidedToolReceivesGuideNodeExpandedChanges() throws Exception {
		// Assert initial state
		assertThat( mockGuidedTool.getExpandedNodes().size(), is( 0 ) );

		Platform.runLater( () -> mockGuidedTool.getGuide().setExpandedIds( "general" ) );
		FxUtil.fxWait( 1000 );

		assertThat( mockGuidedTool.getExpandedNodes(), containsInAnyOrder( mockGuidedTool.getGuide().getNode( "general" ) ) );
	}

	@Test
	public void testGuidedToolDoesNotReceivesGuideNodeExpandedChangeWhenExpandedDoesNotChange() throws Exception {
		// Assert initial state
		Platform.runLater( () -> mockGuidedTool.getGuide().setExpandedIds( "general" ) );
		FxUtil.fxWait( 1000 );
		assertThat( mockGuidedTool.getGuideNodesExpandedEventCount(), is( 1 ) );

		Platform.runLater( () -> mockGuidedTool.getGuide().setExpandedIds( "general" ) );
		FxUtil.fxWait( 1000 );
		assertThat( mockGuidedTool.getGuideNodesExpandedEventCount(), is( 1 ) );
	}

	@Test
	public void testGuidedToolReceivesGuideNodeSelectedChanges() throws Exception {
		// Assert initial state
		assertThat( mockGuidedTool.getSelectedNodes().size(), is( 0 ) );

		Platform.runLater( () -> mockGuidedTool.getGuide().setSelectedIds( "general" ) );
		FxUtil.fxWait( 1000 );
		assertThat( mockGuidedTool.getSelectedNodes(), containsInAnyOrder( mockGuidedTool.getGuide().getNode( "general" ) ) );
	}

	@Test
	public void testGuidedToolDoesNotReceivesGuideNodeSelectedChangeWhenSelectionDoesNotChange() throws Exception {
		// Assert initial state
		Platform.runLater( () -> mockGuidedTool.getGuide().setSelectedIds( "general" ) );
		FxUtil.fxWait( 1000 );
		assertThat( mockGuidedTool.getGuideNodesSelectedEventCount(), is( 1 ) );

		Platform.runLater( () -> mockGuidedTool.getGuide().setSelectedIds( "general" ) );
		FxUtil.fxWait( 1000 );
		assertThat( mockGuidedTool.getGuideNodesSelectedEventCount(), is( 1 ) );
	}

	private class MockGuidedTool extends GuidedTool {

		private Set<GuideNode> expandedNodes = new HashSet<>();

		private Set<GuideNode> selectedNodes = new HashSet<>();

		private int guideNodesExpandedEventCount;

		private int guideNodesSelectedEventCount;

		public MockGuidedTool( ProgramProduct product, Resource resource ) {
			super( product, resource );
		}

		public Set<GuideNode> getExpandedNodes() {
			return expandedNodes;
		}

		public Set<GuideNode> getSelectedNodes() {
			return selectedNodes;
		}

		public int getGuideNodesExpandedEventCount() {
			return guideNodesExpandedEventCount;
		}

		public int getGuideNodesSelectedEventCount() {
			return guideNodesSelectedEventCount;
		}

		public void reset() {
			expandedNodes = new HashSet<>();
			selectedNodes = new HashSet<>();
			guideNodesExpandedEventCount = 0;
			guideNodesSelectedEventCount = 0;
		}

		@Override
		protected void guideNodesExpanded( Set<GuideNode> oldNodes, Set<GuideNode> newNodes ) {
			guideNodesExpandedEventCount++;
			this.expandedNodes = newNodes;
		}

		@Override
		protected void guideNodesSelected( Set<GuideNode> oldNodes, Set<GuideNode> newNodes ) {
			guideNodesSelectedEventCount++;
			this.selectedNodes = newNodes;
		}

	}

}
