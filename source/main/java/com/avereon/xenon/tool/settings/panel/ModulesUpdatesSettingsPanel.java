package com.avereon.xenon.tool.settings.panel;

import com.avereon.xenon.XenonProgramProduct;
import com.avereon.xenon.tool.settings.panel.products.RefreshModuleUpdates;
import com.avereon.xenon.tool.settings.panel.products.DisplayMode;
import com.avereon.xenon.tool.settings.panel.products.ProductsSettingsPanel;
import javafx.scene.control.Button;
import lombok.CustomLog;

@CustomLog
public class ModulesUpdatesSettingsPanel extends ProductsSettingsPanel {

	public ModulesUpdatesSettingsPanel( XenonProgramProduct product ) {
		super( product, DisplayMode.UPDATES );

		Button refreshButton = new Button( "", getProgram().getIconLibrary().getIcon( "refresh" ) );
		refreshButton.setOnAction( event -> updateState( true ) );
		Button downloadAllButton = new Button( "", getProgram().getIconLibrary().getIcon( "download" ) );
		downloadAllButton.setOnAction( event -> updateProducts( getSourcePanels() ) );

		getButtonBox().addAll( refreshButton, downloadAllButton );
	}

	@Override
	protected void updateState( boolean force ) {
		log.atFiner().log( "Update available updates force=%s", force );
		getProgram().getTaskManager().submit( new RefreshModuleUpdates( this, force ) );
	}

}
