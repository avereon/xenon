package com.avereon.xenon.tool.guide;

import com.avereon.xenon.BaseToolUIT;
import com.avereon.xenon.ProgramProduct;
import com.avereon.xenon.resource.Resource;
import com.avereon.xenon.resource.type.ProgramSettingsType;
import com.avereon.xenon.util.FxUtil;
import javafx.application.Platform;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;

public class GuidedToolUIT extends BaseToolUIT {

	private MockGuidedTool mockGuidedTool;

	@BeforeEach
	@Override
	public void setup() throws Exception {
		super.setup();

		Resource resource = program.getResourceManager().createResource( ProgramSettingsType.URI );
		this.mockGuidedTool = new MockGuidedTool( program, resource );
		mockGuidedTool.reset();
	}

	@Test
	void testGuidedToolReceivesGuideNodeExpandedChanges() throws Exception {
		// Assert initial state
		assertThat( mockGuidedTool.getExpandedNodes().size(), is( 0 ) );

		Platform.runLater( () -> mockGuidedTool.getGuide().setExpandedIds( "general" ) );
		FxUtil.fxWait( 1000 );

		assertThat( mockGuidedTool.getExpandedNodes(), containsInAnyOrder( mockGuidedTool.getGuide().getNode( "general" ) ) );
	}

	@Test
	void testGuidedToolDoesNotReceivesGuideNodeExpandedChangeWhenExpandedDoesNotChange() throws Exception {
		// Assert initial state
		Platform.runLater( () -> mockGuidedTool.getGuide().setExpandedIds( "general" ) );
		FxUtil.fxWait( 1000 );
		assertThat( mockGuidedTool.getGuideNodesExpandedEventCount(), is( 1 ) );

		Platform.runLater( () -> mockGuidedTool.getGuide().setExpandedIds( "general" ) );
		FxUtil.fxWait( 1000 );
		assertThat( mockGuidedTool.getGuideNodesExpandedEventCount(), is( 1 ) );
	}

	@Test
	void testGuidedToolReceivesGuideNodeSelectedChanges() throws Exception {
		// Assert initial state
		assertThat( mockGuidedTool.getSelectedNodes().size(), is( 0 ) );

		Platform.runLater( () -> mockGuidedTool.getGuide().setSelectedIds( "general" ) );
		FxUtil.fxWait( 1000 );
		assertThat( mockGuidedTool.getSelectedNodes(), containsInAnyOrder( mockGuidedTool.getGuide().getNode( "general" ) ) );
	}

	@Test
	void testGuidedToolDoesNotReceivesGuideNodeSelectedChangeWhenSelectionDoesNotChange() throws Exception {
		// Assert initial state
		Platform.runLater( () -> mockGuidedTool.getGuide().setSelectedIds( "general" ) );
		FxUtil.fxWait( 1000 );
		assertThat( mockGuidedTool.getGuideNodesSelectedEventCount(), is( 1 ) );

		Platform.runLater( () -> mockGuidedTool.getGuide().setSelectedIds( "general" ) );
		FxUtil.fxWait( 1000 );
		assertThat( mockGuidedTool.getGuideNodesSelectedEventCount(), is( 1 ) );
	}

	private static class MockGuidedTool extends GuidedTool {

		private Set<GuideNode> expandedNodes = new HashSet<>();

		private Set<GuideNode> selectedNodes = new HashSet<>();

		private int guideNodesExpandedEventCount;

		private int guideNodesSelectedEventCount;

		MockGuidedTool( ProgramProduct product, Resource resource ) {
			super( product, resource );
		}

		Set<GuideNode> getExpandedNodes() {
			return expandedNodes;
		}

		Set<GuideNode> getSelectedNodes() {
			return selectedNodes;
		}

		int getGuideNodesExpandedEventCount() {
			return guideNodesExpandedEventCount;
		}

		int getGuideNodesSelectedEventCount() {
			return guideNodesSelectedEventCount;
		}

		void reset() {
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
