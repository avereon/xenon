package com.avereon.xenon.tool.guide;

import com.avereon.xenon.BaseToolUIT;
import com.avereon.xenon.ToolInstanceMode;
import com.avereon.xenon.ToolRegistration;
import com.avereon.xenon.XenonProgramProduct;
import com.avereon.xenon.asset.Asset;
import com.avereon.xenon.asset.MockAssetType;
import com.avereon.xenon.asset.MockCodec;
import com.avereon.xenon.asset.type.ProgramGuideType;
import com.avereon.xenon.workpane.ToolEvent;
import com.avereon.zarra.javafx.Fx;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static com.avereon.xenon.test.ProgramTestConfig.TIMEOUT;
import static org.assertj.core.api.Assertions.assertThat;

public class GuidedToolUIT extends BaseToolUIT {

	private MockGuidedTool mockGuidedTool;

	@BeforeEach
	@Override
	protected void setup() throws Exception {
		super.setup();

		assertToolCount( getWorkpane(), 0 );

		MockAssetType assetType = new MockAssetType( getProgram() );
		getProgram().getAssetManager().addAssetType( assetType );
		assertThat( getProgram().getAssetManager().getAssetType( ProgramGuideType.URI.toString() ) ).isNotNull();

		ToolRegistration registration = new ToolRegistration( getProgram(), MockGuidedTool.class ).setName( "mock" ).setInstanceMode( ToolInstanceMode.SINGLETON );
		getProgram().getToolManager().registerTool( assetType, registration );
		getProgram().getAssetManager().openAsset( MockCodec.URI );

		getWorkpaneEventWatcher().waitForEvent( ToolEvent.ADDED );
		getWorkpaneEventWatcher().waitForEvent( ToolEvent.ADDED );
		Fx.waitForWithExceptions( TIMEOUT );

		assertThat( getWorkpane().getActiveTool() ).isInstanceOf( MockGuidedTool.class );
		assertToolCount( getWorkpane(), 2 );

		mockGuidedTool = (MockGuidedTool)getWorkpane().getActiveTool();
		mockGuidedTool.reset();
	}

	@Test
	void testGuidedToolReceivesGuideNodeExpandedChanges() throws Exception {
		// NOTE When testing expanded nodes the node to expand cannot be a leaf
		// Assert initial state
		assertThat( mockGuidedTool.getExpandedNodes().size() ).isEqualTo( 0 );

		Fx.run( () -> mockGuidedTool.getGuideContext().setExpandedIds( Set.of( "general" ) ) );
		Fx.waitForWithExceptions( TIMEOUT );

		assertThat( mockGuidedTool.getExpandedNodes() ).contains( mockGuidedTool.getCurrentGuide().getNode( "general" ) );
	}

	@Test
	void testGuidedToolDoesNotReceivesGuideNodeExpandedChangeWhenExpandedDoesNotChange() throws Exception {
		// NOTE When testing expanded nodes the node to expand cannot be a leaf
		// Assert initial state
		Fx.run( () -> mockGuidedTool.getGuideContext().setExpandedIds( Set.of( "general" ) ) );
		Fx.waitForWithExceptions( TIMEOUT );
		assertThat( mockGuidedTool.getGuideNodesExpandedEventCount() ).isEqualTo( 1 );

		Fx.run( () -> mockGuidedTool.getGuideContext().setExpandedIds( Set.of( "general" ) ) );
		Fx.waitForWithExceptions( TIMEOUT );
		assertThat( mockGuidedTool.getGuideNodesExpandedEventCount() ).isEqualTo( 1 );
	}

	@Test
	void testGuidedToolReceivesGuideNodeSelectedChanges() throws Exception {
		// Assert initial state
		assertThat( mockGuidedTool.getSelectedNodes().size() ).isEqualTo( 0 );

		Fx.run( () -> mockGuidedTool.getGuideContext().setSelectedIds( Set.of( "general" ) ) );
		Fx.waitForWithExceptions( TIMEOUT );
		assertThat( mockGuidedTool.getSelectedNodes() ).contains( mockGuidedTool.getCurrentGuide().getNode( "general" ) );
	}

	@Test
	void testGuidedToolDoesNotReceivesGuideNodeSelectedChangeWhenSelectionDoesNotChange() throws Exception {
		// Assert initial state
		Fx.run( () -> mockGuidedTool.getGuideContext().setSelectedIds( Set.of( "general" ) ) );
		Fx.waitForWithExceptions( TIMEOUT );
		assertThat( mockGuidedTool.getGuideNodesSelectedEventCount() ).isEqualTo( 1 );

		Fx.run( () -> mockGuidedTool.getGuideContext().setSelectedIds( Set.of( "general" ) ) );
		Fx.waitForWithExceptions( TIMEOUT );
		assertThat( mockGuidedTool.getGuideNodesSelectedEventCount() ).isEqualTo( 1 );
	}

	public static class MockGuidedTool extends GuidedTool {

		private Set<GuideNode> expandedNodes = new HashSet<>();

		private Set<GuideNode> selectedNodes = new HashSet<>();

		private int guideNodesExpandedEventCount;

		private int guideNodesSelectedEventCount;

		public MockGuidedTool( XenonProgramProduct product, Asset asset ) {
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
			assertThat( guide.getNode( "general" ) ).isEqualTo( generalNode );

			GuideNode summaryNode = new GuideNode( getProgram() );
			summaryNode.setId( "summary" );
			summaryNode.setName( "summary" );
			guide.addNode( generalNode, summaryNode );
			assertThat( guide.getNode( "summary" ) ).isEqualTo( summaryNode );

			GuideNode detailsNode = new GuideNode( getProgram() );
			detailsNode.setId( "details" );
			detailsNode.setName( "details" );
			guide.addNode( generalNode, detailsNode );
			assertThat( guide.getNode( "details" ) ).isEqualTo( detailsNode );

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
