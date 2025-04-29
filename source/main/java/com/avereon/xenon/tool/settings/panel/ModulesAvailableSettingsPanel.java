package com.avereon.xenon.tool.settings.panel;

import com.avereon.xenon.XenonProgramProduct;
import com.avereon.xenon.tool.settings.panel.products.DisplayMode;
import com.avereon.xenon.tool.settings.panel.products.ProductsSettingsPanel;
import com.avereon.xenon.tool.settings.panel.products.RefreshAvailableModules;
import javafx.scene.control.Button;
import lombok.CustomLog;

/**
 * This settings panel is used to display the available products and allow users
 * to install, uninstall, and update products.
 */
@CustomLog
public class ModulesAvailableSettingsPanel extends ProductsSettingsPanel {

	public ModulesAvailableSettingsPanel( XenonProgramProduct product ) {
		super( product, DisplayMode.AVAILABLE );

		Button refreshButton = new Button( "", getProgram().getIconLibrary().getIcon( "refresh" ) );
		refreshButton.setOnAction( event -> updateState( true ) );
		Button downloadAllButton = new Button( "", getProgram().getIconLibrary().getIcon( "download" ) );
		downloadAllButton.setOnAction( event -> installProducts( getSourcePanels() ) );

		getButtonBox().addAll( refreshButton, downloadAllButton );
	}

	@Override
	protected void updateState( boolean force ) {
		log.atFiner().log(  "Update available products force=%s", force );
		getProgram().getTaskManager().submit( new RefreshAvailableModules( this, force ) );
	}

}
