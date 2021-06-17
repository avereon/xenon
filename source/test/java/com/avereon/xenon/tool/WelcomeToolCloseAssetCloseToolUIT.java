package com.avereon.xenon.tool;

import com.avereon.xenon.ProgramTool;
import com.avereon.xenon.asset.type.ProgramWelcomeType;
import com.avereon.xenon.workpane.ToolEvent;
import com.avereon.xenon.workpane.Workpane;
import com.avereon.zerra.javafx.Fx;
import org.junit.jupiter.api.Test;

import java.util.concurrent.Future;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class WelcomeToolCloseAssetCloseToolUIT extends WelcomeToolUIT {

	@Test
	void execute() throws Exception {
		Workpane pane = program.getWorkspaceManager().getActiveWorkspace().getActiveWorkarea().getWorkpane();
		assertToolCount( pane, 0 );

		Future<ProgramTool> future = program.getAssetManager().openAsset( ProgramWelcomeType.URI );
		workpaneWatcher.waitForEvent( ToolEvent.ADDED );
		Fx.waitForWithInterrupt( TIMEOUT );
		assertThat( pane.getActiveTool(), instanceOf( WelcomeTool.class ) );
		assertThat( pane.getActiveView().isMaximized(), is( true ) );
		assertToolCount( pane, 1 );

		program.getAssetManager().closeAssets( future.get().getAsset() );
		workpaneWatcher.waitForEvent( ToolEvent.REMOVED );
		Fx.waitForWithInterrupt( TIMEOUT );
		assertThat( pane.getMaximizedView(), is( nullValue() ) );
		assertToolCount( pane, 0 );
	}

}
