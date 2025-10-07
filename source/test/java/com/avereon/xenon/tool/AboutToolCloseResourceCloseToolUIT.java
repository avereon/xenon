package com.avereon.xenon.tool;

import com.avereon.xenon.ProgramTool;
import com.avereon.xenon.asset.type.ProgramAboutType;
import com.avereon.xenon.workpane.ToolEvent;
import com.avereon.xenon.workpane.Workpane;
import com.avereon.zerra.javafx.Fx;
import org.junit.jupiter.api.Test;

import java.util.concurrent.Future;

import static com.avereon.xenon.test.ProgramTestConfig.LONG_TIMEOUT;
import static org.assertj.core.api.Assertions.assertThat;

class AboutToolCloseResourceCloseToolUIT extends AboutToolUIT {

	@Test
	void execute() throws Exception {
		// given
		Workpane pane = getWorkarea();
		assertToolCount( pane, 0 );

		// NOTE Returns immediately
		Future<ProgramTool> future = getProgram().getResourceManager().openAsset( ProgramAboutType.URI );
		getWorkpaneWatcher().waitForEvent( ToolEvent.ADDED );
		getWorkpaneWatcher().waitForEvent( ToolEvent.ADDED );

		assertThat( pane.getActiveTool() ).isInstanceOf( AboutTool.class );
		assertToolCount( pane, 2 );

		// when
		getProgram().getResourceManager().closeAssets( future.get().getResource() );
		getWorkpaneWatcher().waitForEvent( ToolEvent.REMOVED );
		Fx.waitForWithExceptions( LONG_TIMEOUT );

		// then
		assertToolCount( pane, 1 );
	}

}
