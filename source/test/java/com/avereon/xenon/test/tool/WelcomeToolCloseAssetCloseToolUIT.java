package com.avereon.xenon.test.tool;

import com.avereon.xenon.test.FxProgramUIT;
import com.avereon.xenon.ProgramTool;
import com.avereon.xenon.asset.type.ProgramWelcomeType;
import com.avereon.xenon.tool.WelcomeTool;
import com.avereon.xenon.workpane.ToolEvent;
import com.avereon.xenon.workpane.Workpane;
import com.avereon.zarra.javafx.Fx;
import org.junit.jupiter.api.Test;

import java.util.concurrent.Future;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class WelcomeToolCloseAssetCloseToolUIT extends WelcomeToolUIT {

	@Test
	void execute() throws Exception {
		Workpane pane = getWorkpane();
		assertToolCount( pane, 0 );

		Future<ProgramTool> future = getProgram().getAssetManager().openAsset( ProgramWelcomeType.URI );
		getWorkpaneEventWatcher().waitForEvent( ToolEvent.ADDED );
		Fx.waitForWithExceptions( FxProgramUIT.TIMEOUT );
		assertThat( pane.getActiveTool(), instanceOf( WelcomeTool.class ) );
		assertThat( pane.getActiveView().isMaximized(), is( true ) );
		assertToolCount( pane, 1 );

		getProgram().getAssetManager().closeAssets( future.get().getAsset() );
		getWorkpaneEventWatcher().waitForEvent( ToolEvent.REMOVED );
		Fx.waitForWithExceptions( FxProgramUIT.TIMEOUT );
		assertThat( pane.getMaximizedView(), is( nullValue() ) );
		assertToolCount( pane, 0 );
	}

}
