package com.avereon.xenon.tool;

import com.avereon.xenon.ProgramTool;
import com.avereon.xenon.asset.type.ProgramAboutType;
import com.avereon.xenon.workpane.ToolEvent;
import com.avereon.xenon.workpane.Workpane;
import com.avereon.zarra.javafx.Fx;
import org.junit.jupiter.api.Test;

import java.util.concurrent.Future;

import static com.avereon.xenon.test.ProgramTestConfig.LONG_TIMEOUT;
import static org.assertj.core.api.Assertions.assertThat;

class AboutToolCloseAssetCloseToolUIT extends AboutToolUIT {

	@Test
	void execute() throws Exception {
		Workpane pane = getWorkarea();
		assertToolCount( pane, 0 );

		Future<ProgramTool> future = getProgram().getAssetManager().openAsset( ProgramAboutType.URI );
		getWorkpaneEventWatcher().waitForEvent( ToolEvent.ADDED );
		getWorkpaneEventWatcher().waitForEvent( ToolEvent.ADDED );
		Fx.waitForWithExceptions( LONG_TIMEOUT );
		assertThat( pane.getActiveTool() ).isInstanceOf( AboutTool.class );
		assertToolCount( pane, 2 );

		getProgram().getAssetManager().closeAssets( future.get().getAsset() );
		getWorkpaneEventWatcher().waitForEvent( ToolEvent.REMOVED );
		Fx.waitForWithExceptions( LONG_TIMEOUT );
		assertToolCount( pane, 1 );
	}

}
