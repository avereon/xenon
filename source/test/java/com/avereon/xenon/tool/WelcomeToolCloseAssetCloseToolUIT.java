package com.avereon.xenon.tool;

import com.avereon.xenon.ProgramTool;
import com.avereon.xenon.asset.type.ProgramWelcomeType;
import com.avereon.xenon.workpane.ToolEvent;
import com.avereon.xenon.workpane.Workpane;
import com.avereon.zarra.javafx.Fx;
import org.junit.jupiter.api.Test;

import java.util.concurrent.Future;

import static com.avereon.xenon.test.ProgramTestConfig.LONG_TIMEOUT;
import static org.assertj.core.api.Assertions.assertThat;

class WelcomeToolCloseAssetCloseToolUIT extends WelcomeToolUIT {

	@Test
	void execute() throws Exception {
		Workpane pane = getWorkpane();
		assertToolCount( pane, 0 );

		Future<ProgramTool> future = getProgram().getAssetManager().openAsset( ProgramWelcomeType.URI );
		getWorkpaneEventWatcher().waitForEvent( ToolEvent.ADDED );
		Fx.waitForWithExceptions( LONG_TIMEOUT );
		assertThat( pane.getActiveTool() ).isInstanceOf( WelcomeTool.class );
		assertThat( pane.getActiveView().isMaximized() ).isTrue();
		assertToolCount( pane, 1 );

		getProgram().getAssetManager().closeAssets( future.get().getAsset() );
		getWorkpaneEventWatcher().waitForEvent( ToolEvent.REMOVED );
		Fx.waitForWithExceptions( LONG_TIMEOUT );
		assertThat( pane.getMaximizedView() ).isNull();
		assertToolCount( pane, 0 );
	}

}
