package com.avereon.xenon;

import com.avereon.xenon.asset.Resource;
import com.avereon.xenon.tool.guide.Guide;
import com.avereon.xenon.tool.guide.GuideContext;
import com.avereon.xenon.tool.guide.GuideNode;
import com.avereon.xenon.tool.guide.GuidedTool;
import lombok.Getter;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class MockGuidedTool extends GuidedTool {

	@Getter
	private Set<GuideNode> expandedNodes = new HashSet<>();

	@Getter
	private Set<GuideNode> selectedNodes = new HashSet<>();

	@Getter
	private int guideNodesExpandedEventCount;

	private int guideNodesSelectedEventCount;

	public MockGuidedTool( XenonProgramProduct product, Resource resource ) {
		super( product, resource );
		Guide guide = createGuide();
		getGuideContext().getGuides().add( guide );
		getGuideContext().setCurrentGuide( guide );
	}

	public GuideContext getGuideContext() {
		return super.getGuideContext();
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
