package com.avereon.xenon.tool.product;

import com.avereon.xenon.ProgramTool;
import com.avereon.xenon.asset.type.ProgramProductType;
import com.avereon.xenon.workpane.ToolEvent;
import com.avereon.xenon.workpane.Workpane;
import org.junit.jupiter.api.Test;

import java.util.concurrent.Future;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;

class ProductToolCloseAssetCloseToolUIT extends ProductToolUIT {

	@Test
	void execute() throws Exception {
		Workpane pane = program.getWorkspaceManager().getActiveWorkspace().getActiveWorkarea().getWorkpane();
		assertToolCount( pane, 0 );

		Future<ProgramTool> future = program.getAssetManager().openAsset( ProgramProductType.URI );
		workpaneWatcher.waitForEvent( ToolEvent.ADDED );
		workpaneWatcher.waitForEvent( ToolEvent.ADDED );
		assertThat( pane.getActiveTool(), instanceOf( ProductTool.class ) );
		assertToolCount( pane, 2 );

		program.getAssetManager().closeAssets( future.get().getAsset() );
		workpaneWatcher.waitForEvent( ToolEvent.REMOVED );
		assertToolCount( pane, 1 );
	}

}
