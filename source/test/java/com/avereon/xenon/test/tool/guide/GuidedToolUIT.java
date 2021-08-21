package com.avereon.xenon.test.tool.guide;

import com.avereon.xenon.ProgramProduct;
import com.avereon.xenon.ToolInstanceMode;
import com.avereon.xenon.ToolRegistration;
import com.avereon.xenon.asset.Asset;
import com.avereon.xenon.asset.type.ProgramGuideType;
import com.avereon.xenon.test.BaseToolUIT;
import com.avereon.xenon.test.asset.MockAssetType;
import com.avereon.xenon.test.asset.MockCodec;
import com.avereon.xenon.tool.guide.Guide;
import com.avereon.xenon.tool.guide.GuideContext;
import com.avereon.xenon.tool.guide.GuideNode;
import com.avereon.xenon.tool.guide.GuidedTool;
import com.avereon.xenon.workpane.ToolEvent;
import com.avereon.zerra.javafx.Fx;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class GuidedToolUIT extends BaseToolUIT {

	private MockGuidedTool mockGuidedTool;

	@BeforeEach
	@Override
	protected void setup() throws Exception {
		super.setup();

		assertToolCount( getWorkpane(), 0 );
		MatcherAssert.assertThat( getProgram().getAssetManager().getAssetTypes().size(), is( 12 ) );

		MockAssetType assetType = new MockAssetType( getProgram() );
		getProgram().getAssetManager().addAssetType( assetType );
		assertNotNull( getProgram().getAssetManager().getAssetType( ProgramGuideType.URI.toString() ) );

		ToolRegistration registration = new ToolRegistration( getProgram(), MockGuidedTool.class ).setName( "mock" ).setInstanceMode( ToolInstanceMode.SINGLETON );
		getProgram().getToolManager().registerTool( assetType, registration );
		getProgram().getAssetManager().openAsset( MockCodec.URI );

		getWorkpaneEventWatcher().waitForEvent( ToolEvent.ADDED );
		getWorkpaneEventWatcher().waitForEvent( ToolEvent.ADDED );
		Fx.waitForWithExceptions( TIMEOUT );

		assertThat( getWorkpane().getActiveTool(), instanceOf( MockGuidedTool.class ) );
		assertToolCount( getWorkpane(), 2 );

		mockGuidedTool = (MockGuidedTool)getWorkpane().getActiveTool();
		mockGuidedTool.reset();
	}

	@Test
	void testGuidedToolReceivesGuideNodeExpandedChanges() throws Exception {
		// NOTE When testing expanded nodes the node to expand cannot be a leaf
		// Assert initial state
		assertThat( mockGuidedTool.getExpandedNodes().size(), is( 0 ) );

		Fx.run( () -> mockGuidedTool.getGuideContext().setExpandedIds( Set.of( "general" ) ) );
		Fx.waitForWithExceptions( TIMEOUT );

		MatcherAssert.assertThat( mockGuidedTool.getExpandedNodes(), Matchers.containsInAnyOrder( mockGuidedTool.getCurrentGuide().getNode( "general" ) ) );
	}

	@Test
	void testGuidedToolDoesNotReceivesGuideNodeExpandedChangeWhenExpandedDoesNotChange() throws Exception {
		// NOTE When testing expanded nodes the node to expand cannot be a leaf
		// Assert initial state
		Fx.run( () -> mockGuidedTool.getGuideContext().setExpandedIds( Set.of( "general" ) ) );
		Fx.waitForWithExceptions( TIMEOUT );
		assertThat( mockGuidedTool.getGuideNodesExpandedEventCount(), is( 1 ) );

		Fx.run( () -> mockGuidedTool.getGuideContext().setExpandedIds( Set.of( "general" ) ) );
		Fx.waitForWithExceptions( TIMEOUT );
		assertThat( mockGuidedTool.getGuideNodesExpandedEventCount(), is( 1 ) );
	}

	@Test
	void testGuidedToolReceivesGuideNodeSelectedChanges() throws Exception {
		// Assert initial state
		assertThat( mockGuidedTool.getSelectedNodes().size(), is( 0 ) );

		Fx.run( () -> mockGuidedTool.getGuideContext().setSelectedIds( Set.of( "general" ) ) );
		Fx.waitForWithExceptions( TIMEOUT );
		MatcherAssert.assertThat( mockGuidedTool.getSelectedNodes(), Matchers.containsInAnyOrder( mockGuidedTool.getCurrentGuide().getNode( "general" ) ) );
	}

	@Test
	void testGuidedToolDoesNotReceivesGuideNodeSelectedChangeWhenSelectionDoesNotChange() throws Exception {
		// Assert initial state
		Fx.run( () -> mockGuidedTool.getGuideContext().setSelectedIds( Set.of( "general" ) ) );
		Fx.waitForWithExceptions( TIMEOUT );
		assertThat( mockGuidedTool.getGuideNodesSelectedEventCount(), is( 1 ) );

		Fx.run( () -> mockGuidedTool.getGuideContext().setSelectedIds( Set.of( "general" ) ) );
		Fx.waitForWithExceptions( TIMEOUT );
		assertThat( mockGuidedTool.getGuideNodesSelectedEventCount(), is( 1 ) );
	}

	public static class MockGuidedTool extends GuidedTool {

		private Set<GuideNode> expandedNodes = new HashSet<>();

		private Set<GuideNode> selectedNodes = new HashSet<>();

		private int guideNodesExpandedEventCount;

		private int guideNodesSelectedEventCount;

		public MockGuidedTool( ProgramProduct product, Asset asset ) {
			super( product, asset );
			Guide guide = createGuide();
			getGuideContext().getGuides().add( guide );
			getGuideContext().setCurrentGuide( guide );
		}

		public GuideContext getGuideContext() {
			return super.getGuideContext();
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

		private Guide createGuide() {
			Guide guide = new Guide();
			guide.clear();

			GuideNode generalNode = new GuideNode( getProgram() );
			generalNode.setId( "general" );
			generalNode.setName( "general" );
			guide.addNode( generalNode );
			assertThat( guide.getNode( "general" ), is( generalNode ) );

			GuideNode summaryNode = new GuideNode( getProgram() );
			summaryNode.setId( "summary" );
			summaryNode.setName( "summary" );
			guide.addNode( generalNode, summaryNode );
			assertThat( guide.getNode( "summary" ), is( summaryNode ) );

			GuideNode detailsNode = new GuideNode( getProgram() );
			detailsNode.setId( "details" );
			detailsNode.setName( "details" );
			guide.addNode( generalNode, detailsNode );
			assertThat( guide.getNode( "details" ), is( detailsNode ) );

			return guide;
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
