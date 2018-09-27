package com.xeomar.xenon.tool.guide;

import com.xeomar.settings.MapSettings;
import com.xeomar.xenon.FxProgramTestCase;
import com.xeomar.xenon.ProgramProduct;
import com.xeomar.xenon.resource.Resource;
import com.xeomar.xenon.resource.type.ProgramSettingsType;
import org.junit.Before;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class GuideToolSelectedNodesTest extends FxProgramTestCase {

	private MockGuidedTool mockGuidedTool;

	@Before
	public void setup() throws Exception {
		super.setup();

		Resource resource = program.getResourceManager().createResource( ProgramSettingsType.URI );
		this.mockGuidedTool = new MockGuidedTool( program, resource );
		this.mockGuidedTool.setSettings( new MapSettings() );
		// Need to call resourceReady to register the listeners
		this.mockGuidedTool.resourceReady( null );
	}

	@Test
	public void testGuidedToolReceivesGuideNodeSelectionChanges() {
		// Assert initial state
		assertThat( mockGuidedTool.getSelectedNodes().size(), is( 0 ) );

		mockGuidedTool.getGuide().setSelectedIds( "general" );
		assertThat( mockGuidedTool.getSelectedNodes(), containsInAnyOrder( mockGuidedTool.getGuide().getNode( "general" ) ) );
	}

	@Test
	public void testGuidedToolDoesNotReceivesGuideNodeSelectionChangeWhenSelectionDoesNotChange() {
		// Assert initial state
		mockGuidedTool.getGuide().setSelectedIds( "general" );
		assertThat( mockGuidedTool.getGuideNodesChangedCallCount(), is( 1 ) );

		mockGuidedTool.getGuide().setSelectedIds( "general" );
		assertThat( mockGuidedTool.getGuideNodesChangedCallCount(), is( 1 ) );
	}

	private class MockGuidedTool extends GuidedTool {

		private Set<GuideNode> selectedNodes = new HashSet<>();

		private int guideNodesChangedCallCount;

		public MockGuidedTool( ProgramProduct product, Resource resource ) {
			super( product, resource );
		}

		public Set<GuideNode> getSelectedNodes() {
			return selectedNodes;
		}

		public int getGuideNodesChangedCallCount() {
			return guideNodesChangedCallCount;
		}

		@Override
		protected void guideNodesSelected( Set<GuideNode> oldNodes, Set<GuideNode> newNodes ) {
			guideNodesChangedCallCount++;
			this.selectedNodes = newNodes;
		}

	}

}
