package com.avereon.xenon.tool;

import com.avereon.util.Log;
import com.avereon.xenon.BundleKey;
import com.avereon.xenon.ProgramProduct;
import com.avereon.xenon.ProgramTool;
import com.avereon.xenon.PropertiesToolEvent;
import com.avereon.xenon.asset.Asset;
import com.avereon.xenon.asset.OpenAssetRequest;
import com.avereon.xenon.tool.settings.SettingsPage;
import com.avereon.xenon.tool.settings.SettingsPanel;
import com.avereon.xenon.workpane.ToolException;
import com.avereon.xenon.workpane.Workpane;
import com.avereon.zerra.javafx.Fx;

/**
 * This tool listens for "show properties" and "hide properties" events that
 * allow the user to edit the properties of an object using the settings API.
 */
public class PropertiesTool extends ProgramTool {

	private static final System.Logger log = Log.get();

	//private SettingsPage page;

	private String bundleKey;

	private SettingsPanel panel;

	public PropertiesTool( ProgramProduct product, Asset asset ) {
		super( product, asset );
	}

	@Override
	public Workpane.Placement getPlacement() {
		return Workpane.Placement.DOCK_RIGHT;
	}

	@Override
	protected void ready( OpenAssetRequest request ) throws ToolException {
		setTitle( getProduct().rb().text( BundleKey.TOOL, "properties-name" ) );
		setGraphic( getProgram().getIconLibrary().getIcon( "properties" ) );
	}

	@Override
	protected void display() throws ToolException {
		getWorkspace().getEventBus().register( PropertiesToolEvent.SHOW, e -> Fx.run( () -> showPage( e.getPage() ) ) );
		getWorkspace().getEventBus().register( PropertiesToolEvent.HIDE, e -> Fx.run( () -> hidePage( e.getPage() ) ) );
	}

	private void showPage( SettingsPage page ) {
		log.log( Log.INFO, "Show properties..." );
		if( this.panel != null ) getChildren().remove( this.panel );
		if( this.panel != null && this.panel.getPage() == page ) return;
		getChildren().addAll( this.panel = new SettingsPanel( page, "props" ) );
	}

	private void hidePage( SettingsPage page ) {
		log.log( Log.INFO, "Hide properties..." );
		if( this.panel != null && this.panel.getPage() != page ) return;
		getChildren().remove( this.panel );
		this.panel = null;
	}

}
