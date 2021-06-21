package com.avereon.xenon.tool.settings;

import com.avereon.xenon.ProgramTool;
import com.avereon.xenon.asset.type.ProgramSettingsType;
import com.avereon.xenon.workpane.ToolEvent;
import com.avereon.xenon.workpane.Workpane;
import com.avereon.zerra.javafx.Fx;
import org.junit.jupiter.api.Test;

import java.util.concurrent.Future;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;

class SettingsToolCloseAssetCloseToolUIT extends SettingsToolUIT {

	@Test
	void execute() throws Exception {
		Workpane pane = program.getWorkspaceManager().getActiveWorkspace().getActiveWorkarea().getWorkpane();
		assertToolCount( pane, 0 );

		Future<ProgramTool> future = program.getAssetManager().openAsset( ProgramSettingsType.URI );
		workpaneWatcher.waitForEvent( ToolEvent.ADDED );
		workpaneWatcher.waitForEvent( ToolEvent.ADDED );
		Fx.waitForWithExceptions( TIMEOUT );
		assertThat( pane.getActiveTool(), instanceOf( SettingsTool.class ) );
		assertToolCount( pane, 2 );

		program.getAssetManager().closeAssets( future.get().getAsset() );
		workpaneWatcher.waitForEvent( ToolEvent.REMOVED );
		Fx.waitForWithExceptions( TIMEOUT );
		assertToolCount( pane, 1 );
	}

}
