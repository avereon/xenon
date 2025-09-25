package com.avereon.xenon.tool.settings;

import com.avereon.xenon.ProgramTool;
import com.avereon.xenon.asset.type.ProgramSettingsType;
import com.avereon.xenon.workpane.ToolEvent;
import com.avereon.xenon.workspace.Workarea;
import com.avereon.zerra.javafx.Fx;
import org.junit.jupiter.api.Test;

import java.util.concurrent.Future;

import static com.avereon.xenon.test.ProgramTestConfig.LONG_TIMEOUT;
import static org.assertj.core.api.Assertions.assertThat;

class SettingsToolCloseAssetCloseToolUIT extends SettingsToolUIT {

	@Test
	void execute() throws Exception {
		Workarea area = getProgram().getWorkspaceManager().getActiveWorkspace().getActiveWorkarea();
		assertToolCount( area, 0 );

		Future<ProgramTool> future = getProgram().getAssetManager().openAsset( ProgramSettingsType.URI );
		getWorkpaneWatcher().waitForEvent( ToolEvent.ADDED );
		getWorkpaneWatcher().waitForEvent( ToolEvent.ADDED );
		Fx.waitForWithExceptions( LONG_TIMEOUT );
		assertThat( area.getActiveTool() ).isInstanceOf( SettingsTool.class );
		assertToolCount( area, 2 );

		getProgram().getAssetManager().closeAssets( future.get().getAsset() );
		getWorkpaneWatcher().waitForEvent( ToolEvent.REMOVED );
		Fx.waitForWithExceptions( LONG_TIMEOUT );
		assertToolCount( area, 1 );
	}

}
