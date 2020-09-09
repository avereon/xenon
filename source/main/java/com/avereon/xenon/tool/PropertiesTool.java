package com.avereon.xenon.tool;

import com.avereon.util.Log;
import com.avereon.xenon.ProgramProduct;
import com.avereon.xenon.ProgramTool;
import com.avereon.xenon.PropertiesToolEvent;
import com.avereon.xenon.asset.Asset;
import com.avereon.xenon.asset.OpenAssetRequest;
import com.avereon.xenon.tool.settings.SettingsPage;
import com.avereon.xenon.tool.settings.SettingsPanel;
import com.avereon.xenon.workpane.ToolException;
import com.avereon.zerra.javafx.Fx;

import java.util.Map;

/**
 * This tool listens for "show properties" and "hide properties" events that
 * allow the user to edit the properties of an object using the settings API.
 */
public class PropertiesTool extends ProgramTool {

	private static final System.Logger log = Log.get();

	//private SettingsPage page;

	private SettingsPanel panel;

	public PropertiesTool( ProgramProduct product, Asset asset ) {
		super( product, asset );
	}

	@Override
	protected void ready( OpenAssetRequest request ) throws ToolException {
		// TODO Pattern after the guide tool
		getWorkspace().getEventBus().register( PropertiesToolEvent.SHOW, e -> Fx.run( () -> showPage( e.getPage() ) ) );
		getWorkspace().getEventBus().register( PropertiesToolEvent.HIDE, e -> Fx.run( () -> hidePage( e.getPage() ) ) );
	}

	private void showPage( SettingsPage page ) {
		log.log( Log.INFO, "Show properties..." );
		if( this.panel != null ) getChildren().remove( this.panel );
		if( this.panel != null && this.panel.getPage() == page ) return;
		getChildren().addAll( this.panel = new SettingsPanel( page, Map.of() ) );
	}

	private void hidePage( SettingsPage page ) {
		log.log( Log.INFO, "Hide properties..." );
		if( this.panel != null && this.panel.getPage() != page ) return;
		getChildren().remove( this.panel );
		this.panel = null;
	}

}
