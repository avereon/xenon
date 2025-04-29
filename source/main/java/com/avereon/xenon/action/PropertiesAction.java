package com.avereon.xenon.action;

import com.avereon.data.NodeSettings;
import com.avereon.xenon.Xenon;
import com.avereon.xenon.ProgramAction;
import com.avereon.xenon.PropertiesToolEvent;
import com.avereon.xenon.asset.Asset;
import com.avereon.xenon.asset.type.ProgramPropertiesType;
import com.avereon.xenon.task.Task;
import com.avereon.xenon.tool.settings.SettingsPage;
import com.avereon.xenon.workspace.Workspace;
import com.avereon.zarra.javafx.Fx;
import javafx.event.ActionEvent;
import lombok.CustomLog;

@CustomLog
public class PropertiesAction extends ProgramAction {

	public PropertiesAction( Xenon program ) {
		super( program );
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public void handle( ActionEvent event ) {
		// Get the settings pages for the asset type
		Asset asset = getProgram().getAssetManager().getCurrentAsset();
		SettingsPage page = asset.getType().getSettingsPages().get( "asset" );

		// Set the settings for the pages
		page.setSettings( new NodeSettings( asset.getModel() ) );

		// Switch to a task thread to get the tool
		getProgram().getTaskManager().submit( Task.of( () -> {
			try {
				// Show the properties tool
				getProgram().getAssetManager().openAsset( ProgramPropertiesType.URI ).get();

				// Fire the event on the FX thread
				Workspace workspace = getProgram().getWorkspaceManager().getActiveWorkspace();
				Fx.run( () -> workspace.getEventBus().dispatch( new PropertiesToolEvent( PropertiesAction.this, PropertiesToolEvent.SHOW, page ) ) );
			} catch( Exception exception ) {
				log.atError( exception ).log();
			}
		} ) );
	}

}
