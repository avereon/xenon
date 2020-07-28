package com.avereon.xenon.tool.guide;

import com.avereon.zerra.javafx.FxUtil;
import com.avereon.xenon.BaseToolUIT;
import com.avereon.xenon.ProgramProduct;
import com.avereon.xenon.ToolInstanceMode;
import com.avereon.xenon.ToolRegistration;
import com.avereon.xenon.asset.Asset;
import com.avereon.xenon.asset.MockAssetType;
import com.avereon.xenon.asset.MockCodec;
import com.avereon.xenon.workpane.ToolEvent;
import javafx.application.Platform;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class GuidedToolUIT extends BaseToolUIT {

	private MockGuidedTool mockGuidedTool;

	@BeforeEach
	@Override
	public void setup() throws Exception {
		super.setup();

		assertToolCount( workpane, 0 );
		assertThat( program.getAssetManager().getAssetTypes().size(), is( 11 ) );

		assertNotNull( program.getAssetManager().getAssetType( "program:guide" ) );
		MockAssetType assetType = new MockAssetType( program );
		program.getAssetManager().addAssetType( assetType );

		ToolRegistration registration = new ToolRegistration( program, MockGuidedTool.class ).setName( "mock" ).setInstanceMode( ToolInstanceMode.SINGLETON );
		program.getToolManager().registerTool( assetType, registration );
		program.getAssetManager().openAsset( MockCodec.URI );

		workpaneWatcher.waitForEvent( ToolEvent.ADDED );
		workpaneWatcher.waitForEvent( ToolEvent.ADDED );

		assertThat( workpane.getActiveTool(), instanceOf( MockGuidedTool.class ) );
		assertToolCount( workpane, 2 );

		mockGuidedTool = (MockGuidedTool)workpane.getActiveTool();
		mockGuidedTool.reset();
	}

	@Test
	void testGuidedToolReceivesGuideNodeExpandedChanges() throws Exception {
		// NOTE When testing expanded nodes the node to expand cannot be a leaf
		// Assert initial state
		assertThat( mockGuidedTool.getExpandedNodes().size(), is( 0 ) );

		Platform.runLater( () -> mockGuidedTool.getGuide().setExpandedIds( Set.of( "general" ) ) );
		FxUtil.fxWait( TIMEOUT );

		assertThat( mockGuidedTool.getExpandedNodes(), containsInAnyOrder( mockGuidedTool.getGuide().getNode( "general" ) ) );
	}

	@Test
	void testGuidedToolDoesNotReceivesGuideNodeExpandedChangeWhenExpandedDoesNotChange() throws Exception {
		// NOTE When testing expanded nodes the node to expand cannot be a leaf
		// Assert initial state
		Platform.runLater( () -> mockGuidedTool.getGuide().setExpandedIds( Set.of( "general" ) ) );
		FxUtil.fxWait( TIMEOUT );
		assertThat( mockGuidedTool.getGuideNodesExpandedEventCount(), is( 1 ) );

		Platform.runLater( () -> mockGuidedTool.getGuide().setExpandedIds( Set.of( "general" ) ) );
		FxUtil.fxWait( TIMEOUT );
		assertThat( mockGuidedTool.getGuideNodesExpandedEventCount(), is( 1 ) );
	}

	@Test
	void testGuidedToolReceivesGuideNodeSelectedChanges() throws Exception {
		// Assert initial state
		assertThat( mockGuidedTool.getSelectedNodes().size(), is( 0 ) );

		Platform.runLater( () -> mockGuidedTool.getGuide().setSelectedIds( Set.of( "general" ) ) );
		FxUtil.fxWait( TIMEOUT );
		assertThat( mockGuidedTool.getSelectedNodes(), containsInAnyOrder( mockGuidedTool.getGuide().getNode( "general" ) ) );
	}

	@Test
	void testGuidedToolDoesNotReceivesGuideNodeSelectedChangeWhenSelectionDoesNotChange() throws Exception {
		// Assert initial state
		Platform.runLater( () -> mockGuidedTool.getGuide().setSelectedIds( Set.of( "general" ) ) );
		FxUtil.fxWait( TIMEOUT );
		assertThat( mockGuidedTool.getGuideNodesSelectedEventCount(), is( 1 ) );

		Platform.runLater( () -> mockGuidedTool.getGuide().setSelectedIds( Set.of( "general" ) ) );
		FxUtil.fxWait( TIMEOUT );
		assertThat( mockGuidedTool.getGuideNodesSelectedEventCount(), is( 1 ) );
	}

	public static class MockGuidedTool extends GuidedTool {

		private Guide guide;

		private Set<GuideNode> expandedNodes = new HashSet<>();

		private Set<GuideNode> selectedNodes = new HashSet<>();

		private int guideNodesExpandedEventCount;

		private int guideNodesSelectedEventCount;

		public MockGuidedTool( ProgramProduct product, Asset asset ) {
			super( product, asset );
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
		protected Guide getGuide() {
			if( guide != null ) return guide;

			Guide guide = new Guide();
			guide.clear();

			GuideNode generalNode = new GuideNode( getProgram() );
			generalNode.setId( "general" );
			generalNode.setName( "general" );
			guide.addNode( generalNode );

			GuideNode summaryNode = new GuideNode( getProgram() );
			summaryNode.setId( "summary" );
			summaryNode.setName( "summary" );
			guide.addNode( generalNode, summaryNode );

			GuideNode detailsNode = new GuideNode( getProgram() );
			detailsNode.setId( "details" );
			detailsNode.setName( "details" );
			guide.addNode( generalNode, detailsNode );

			return this.guide = guide;
		}

		@Override
		protected void guideNodesExpanded( Set<GuideNode> oldNodes, Set<GuideNode> newNodes ) {
			System.out.println( "Expand: " + newNodes );
			guideNodesExpandedEventCount++;
			this.expandedNodes = newNodes;
		}

		@Override
		protected void guideNodesSelected( Set<GuideNode> oldNodes, Set<GuideNode> newNodes ) {
			System.out.println( "Select: " + newNodes );
			guideNodesSelectedEventCount++;
			this.selectedNodes = newNodes;
		}

	}

}
