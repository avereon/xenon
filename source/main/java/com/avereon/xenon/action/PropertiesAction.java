package com.avereon.xenon.action;

import com.avereon.xenon.Program;
import com.avereon.xenon.ProgramAction;
import com.avereon.xenon.asset.Asset;
import com.avereon.xenon.asset.type.PropertiesType;
import com.avereon.xenon.task.Task;
import com.avereon.xenon.tool.PropertiesTool;
import com.avereon.xenon.tool.settings.SettingsPage;
import javafx.event.ActionEvent;
import lombok.CustomLog;

@CustomLog
public class PropertiesAction extends ProgramAction {

	public PropertiesAction( Program program ) {
		super( program );
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public void handle( ActionEvent event ) {
		getProgram().getTaskManager().submit( Task.of( () -> {
			try {
				log.atConfig().log( "properties action" );
				Asset asset = getProgram().getAssetManager().getCurrentAsset();
				PropertiesTool tool = (PropertiesTool)getProgram().getAssetManager().openAsset( PropertiesType.URI ).get();

				// Get the settings pages for the asset type
				SettingsPage page = asset.getType().getSettingsPages().get( "asset" );
				log.atConfig().log( "page=%s", page );

				// Set the settings for the pages
				page.setSettings( asset.getSettings() );
				//			Workspace workspace = getProgram().getWorkspaceManager().getActiveWorkspace();
				//			workspace.getEventBus().dispatch( new PropertiesToolEvent( PropertiesAction.this, PropertiesToolEvent.SHOW, page ) );

				tool.showPage( page );
			} catch( Exception exception ) {
				log.atError( exception ).log();
			}
		}) );
	}

}
