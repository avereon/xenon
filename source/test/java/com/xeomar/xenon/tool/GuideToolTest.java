package com.xeomar.xenon.tool;

import com.xeomar.xenon.FxProgramTestCase;
import com.xeomar.xenon.resource.Resource;
import com.xeomar.xenon.resource.type.ProgramGuideType;
import com.xeomar.xenon.workarea.Workpane;
import com.xeomar.xenon.workarea.WorkpaneEvent;
import org.junit.Assert;
import org.junit.Test;

import java.util.Set;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class GuideToolTest extends FxProgramTestCase {

	@Test
	public void testGetRequiredToolResources() {
		Resource resource = new Resource( ProgramGuideType.URI );
		GuideTool tool = new GuideTool( program, resource );

		Set<String> resources = tool.getResourceDependencies();
		Assert.assertThat(resources.size(), is( 0 ));
	}

	@Test
	public void testOpenGuideTool() throws Exception {
		Workpane pane = program.getWorkspaceManager().getActiveWorkspace().getActiveWorkarea().getWorkpane();
		assertThat( pane.getTools().size(), is( 0 ) );

		program.getResourceManager().open( program.getResourceManager().createResource( ProgramGuideType.URI ) );
		workpaneWatcher.waitForEvent( WorkpaneEvent.Type.TOOL_ADDED );
		assertThat( pane.getActiveTool(), instanceOf( GuideTool.class ) );
		assertThat( pane.getTools().size(), is( 1 ) );
	}

	@Test
	public void testClosingResourceWillCloseTool() throws Exception {
		Workpane pane = program.getWorkspaceManager().getActiveWorkspace().getActiveWorkarea().getWorkpane();
		assertThat( pane.getTools().size(), is( 0 ) );

		program.getResourceManager().open( program.getResourceManager().createResource( ProgramGuideType.URI ) );
		workpaneWatcher.waitForEvent( WorkpaneEvent.Type.TOOL_ADDED );
		assertThat( pane.getActiveTool(), instanceOf( GuideTool.class ) );
		assertThat( pane.getTools().size(), is( 1 ) );

		Resource resource = program.getResourceManager().createResource( ProgramGuideType.URI );
		program.getResourceManager().closeResources( resource );
		workpaneWatcher.waitForEvent( WorkpaneEvent.Type.TOOL_REMOVED );
		assertThat( pane.getTools().size(), is( 0 ) );
	}

}
