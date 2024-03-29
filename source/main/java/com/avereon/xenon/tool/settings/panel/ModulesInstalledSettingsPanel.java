package com.avereon.xenon.tool.settings.panel;

import com.avereon.xenon.XenonProgramProduct;
import com.avereon.xenon.tool.settings.panel.products.DisplayMode;
import com.avereon.xenon.tool.settings.panel.products.ProductsSettingsPanel;
import com.avereon.xenon.tool.settings.panel.products.RefreshInstalledModules;
import javafx.scene.control.Button;
import lombok.CustomLog;

@CustomLog
public class ModulesInstalledSettingsPanel extends ProductsSettingsPanel {

	public ModulesInstalledSettingsPanel( XenonProgramProduct product ) {
		super( product, DisplayMode.INSTALLED );

		Button refreshButton = new Button( "", getProgram().getIconLibrary().getIcon( "refresh" ) );
		refreshButton.setOnAction( event -> updateState( true ) );

		getButtonBox().addAll( refreshButton );
	}

	@Override
	protected void updateState( boolean force ) {
		log.atFiner().log( "Update installed products force=%s", force );
		getProgram().getTaskManager().submit( new RefreshInstalledModules( this, force ) );
	}

}
