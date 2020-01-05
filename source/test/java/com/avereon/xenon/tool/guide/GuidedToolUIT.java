package com.avereon.xenon.tool.guide;

import com.avereon.venza.javafx.FxUtil;
import com.avereon.xenon.BaseToolUIT;
import com.avereon.xenon.Program;
import com.avereon.xenon.ProgramProduct;
import com.avereon.xenon.asset.Asset;
import com.avereon.xenon.asset.MockAssetType;
import com.avereon.xenon.tool.ToolInstanceMode;
import com.avereon.xenon.tool.ToolMetadata;
import com.avereon.xenon.workpane.ToolEvent;
import javafx.application.Platform;
import javafx.scene.control.TreeItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class GuidedToolUIT extends BaseToolUIT {

	private MockGuidedTool mockGuidedTool;

	@BeforeEach
	@Override
	public void setup() throws Exception {
		super.setup();

		assertThat( workpane.getTools().size(), is( 0 ) );

		MockAssetType assetType = new MockAssetType( program );
		program.getAssetManager().addAssetType( assetType );
		program.getAssetManager().registerUriAssetType( MockAssetType.URI, assetType );
		program.getToolManager().registerTool( assetType, new ToolMetadata( program, MockGuidedTool.class ).setName( "mock" ).setInstanceMode( ToolInstanceMode.SINGLETON ) );

		program.getAssetManager().openAsset( MockAssetType.URI );

		workpaneWatcher.waitForEvent( ToolEvent.ADDED );
		workpaneWatcher.waitForEvent( ToolEvent.ADDED );

		assertThat( workpane.getTools().size(), is( 2 ) );
		assertThat( workpane.getActiveTool(), instanceOf( MockGuidedTool.class ) );

		mockGuidedTool = (MockGuidedTool)workpane.getActiveTool();
		mockGuidedTool.reset();
	}

	@Test
	void testGuidedToolReceivesGuideNodeExpandedChanges() throws Exception {
		// NOTE When testing expanded nodes the node to expand cannot be a leaf
		// Assert initial state
		assertThat( mockGuidedTool.getExpandedNodes().size(), is( 0 ) );

		Platform.runLater( () -> mockGuidedTool.getGuide().setExpandedIds( Set.of( "general" ) ) );
		FxUtil.fxWait( 1000 );

		assertThat( mockGuidedTool.getExpandedNodes(), containsInAnyOrder( mockGuidedTool.getGuide().getNode( "general" ) ) );
	}

	@Test
	void testGuidedToolDoesNotReceivesGuideNodeExpandedChangeWhenExpandedDoesNotChange() throws Exception {
		// NOTE When testing expanded nodes the node to expand cannot be a leaf
		// Assert initial state
		Platform.runLater( () -> mockGuidedTool.getGuide().setExpandedIds( Set.of( "general" ) ) );
		FxUtil.fxWait( 1000 );
		assertThat( mockGuidedTool.getGuideNodesExpandedEventCount(), is( 1 ) );

		Platform.runLater( () -> mockGuidedTool.getGuide().setExpandedIds( Set.of( "general" ) ) );
		FxUtil.fxWait( 1000 );
		assertThat( mockGuidedTool.getGuideNodesExpandedEventCount(), is( 1 ) );
	}

	@Test
	void testGuidedToolReceivesGuideNodeSelectedChanges() throws Exception {
		// Assert initial state
		assertThat( mockGuidedTool.getSelectedNodes().size(), is( 0 ) );

		Platform.runLater( () -> mockGuidedTool.getGuide().setSelectedIds( Set.of( "general" ) ) );
		FxUtil.fxWait( 1000 );
		assertThat( mockGuidedTool.getSelectedNodes(), containsInAnyOrder( mockGuidedTool.getGuide().getNode( "general" ) ) );
	}

	@Test
	void testGuidedToolDoesNotReceivesGuideNodeSelectedChangeWhenSelectionDoesNotChange() throws Exception {
		// Assert initial state
		Platform.runLater( () -> mockGuidedTool.getGuide().setSelectedIds( Set.of( "general" ) ) );
		FxUtil.fxWait( 1000 );
		assertThat( mockGuidedTool.getGuideNodesSelectedEventCount(), is( 1 ) );

		Platform.runLater( () -> mockGuidedTool.getGuide().setSelectedIds( Set.of( "general" ) ) );
		FxUtil.fxWait( 1000 );
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
			guide.getRoot().getChildren().clear();

			GuideNode generalNode = new GuideNode();
			generalNode.setId( "general" );
			generalNode.setName( "general" );
			TreeItem<GuideNode> generalTreeNode  = createGuideNode( getProgram(), generalNode );
			guide.getRoot().getChildren().add( generalTreeNode );

			GuideNode summaryNode = new GuideNode();
			summaryNode.setId( "summary" );
			summaryNode.setName( "summary" );
			generalTreeNode.getChildren().add( createGuideNode( getProgram(), summaryNode ) );

			GuideNode detailsNode = new GuideNode();
			detailsNode.setId( "details" );
			detailsNode.setName( "details" );
			generalTreeNode.getChildren().add( createGuideNode( getProgram(), detailsNode ) );

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

		private TreeItem<GuideNode> createGuideNode( Program program, GuideNode node ) {
			return new TreeItem<>( node, program.getIconLibrary().getIcon( node.getIcon() ) );
		}

	}

}
