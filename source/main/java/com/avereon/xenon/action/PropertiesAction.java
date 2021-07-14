package com.avereon.xenon.action;

import com.avereon.xenon.Program;
import com.avereon.xenon.ProgramAction;
import com.avereon.xenon.asset.Asset;
import com.avereon.xenon.asset.type.PropertiesType;
import com.avereon.xenon.tool.settings.SettingsPage;
import javafx.event.ActionEvent;

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
		Asset asset = getProgram().getAssetManager().getCurrentAsset();
		getProgram().getAssetManager().openAsset( PropertiesType.URI );

		SettingsPage page = asset.getSettingsPage();
		page.setSettings( asset.getSettings() );

//		SettingsPage page = designPropertiesMap.getSettingsPage( type );
//			page.setSettings( settings );
//			getWorkspace().getEventBus().dispatch( new PropertiesToolEvent( DesignTool.this, PropertiesToolEvent.SHOW, page ) );

		}

}
