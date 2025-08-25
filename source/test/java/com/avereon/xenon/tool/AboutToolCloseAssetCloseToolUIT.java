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

class AboutToolCloseAssetCloseToolUIT extends AboutToolUIT {

	@Test
	void execute() throws Exception {
		// given
		Workpane pane = getWorkarea();
		assertToolCount( pane, 0 );

		System.out.println( "AboutToolCloseAssetCloseToolUIT openAsset" );
		Future<ProgramTool> future = getProgram().getAssetManager().openAsset( ProgramAboutType.URI );
		System.out.println( "AboutToolCloseAssetCloseToolUIT waiting for tool added one" );
		getWorkpaneEventWatcher().waitForEvent( ToolEvent.ADDED );
		System.out.println( "AboutToolCloseAssetCloseToolUIT waiting for tool added two" );
		getWorkpaneEventWatcher().waitForEvent( ToolEvent.ADDED );
		Fx.waitForWithExceptions( LONG_TIMEOUT );
		System.out.println( "AboutToolCloseAssetCloseToolUIT FX is settled" );
		assertThat( pane.getActiveTool() ).isInstanceOf( AboutTool.class );
		System.out.println( "AboutToolCloseAssetCloseToolUIT FX tool count asserting..." );
		assertToolCount( pane, 2 );
		System.out.println( "AboutToolCloseAssetCloseToolUIT FX tool count asserted" );

		// when
		getProgram().getAssetManager().closeAssets( future.get().getAsset() );
		getWorkpaneEventWatcher().waitForEvent( ToolEvent.REMOVED );
		Fx.waitForWithExceptions( LONG_TIMEOUT );

		// then
		assertToolCount( pane, 1 );
	}

}
