package com.avereon.xenon.tool;

import com.avereon.xenon.ProgramTool;
import com.avereon.xenon.asset.type.ProgramAboutType;
import com.avereon.xenon.workpane.ToolEvent;
import com.avereon.xenon.workpane.Workpane;
import com.avereon.zerra.javafx.Fx;
import org.junit.jupiter.api.Test;

import java.util.concurrent.Future;
import java.util.logging.Level;

import static com.avereon.xenon.test.ProgramTestConfig.LONG_TIMEOUT;
import static org.assertj.core.api.Assertions.assertThat;

class AboutToolCloseAssetCloseToolUIT extends AboutToolUIT {

	@Test
	void execute() throws Exception {
		setLogLevel( Level.FINE );

		// given
		Workpane pane = getWorkarea();
		assertToolCount( pane, 0 );

		Future<ProgramTool> future = getProgram().getAssetManager().openAsset( ProgramAboutType.URI );
		getWorkpaneEventWatcher().waitForEvent( ToolEvent.ADDED );
		getWorkpaneEventWatcher().waitForEvent( ToolEvent.ADDED );
		Fx.waitForWithExceptions( LONG_TIMEOUT );
		assertThat( pane.getActiveTool() ).isInstanceOf( AboutTool.class );
		assertToolCount( pane, 2 );

		getProgram().getToolManager().printAssetTypeToolMap( getClass().getSimpleName() );

		// FIXME The tool came back null
		assertThat( future.get() ).withFailMessage( "Tool should not be null" ).isNotNull();
		assertThat( future.get().getAsset() ).withFailMessage( "Asset should not be null" ).isNotNull();

		// when
		getProgram().getAssetManager().closeAssets( future.get().getAsset() );
		getWorkpaneEventWatcher().waitForEvent( ToolEvent.REMOVED );
		Fx.waitForWithExceptions( LONG_TIMEOUT );

		// then
		assertToolCount( pane, 1 );
	}

}
