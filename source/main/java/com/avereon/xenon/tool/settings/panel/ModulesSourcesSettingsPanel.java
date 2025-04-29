package com.avereon.xenon.tool.settings.panel;

import com.avereon.xenon.XenonProgramProduct;
import com.avereon.xenon.tool.settings.panel.products.DisplayMode;
import com.avereon.xenon.tool.settings.panel.products.ProductsSettingsPanel;
import com.avereon.xenon.tool.settings.panel.products.RefreshModuleSources;
import javafx.scene.control.Button;
import lombok.CustomLog;

@CustomLog
public class ModulesSourcesSettingsPanel extends ProductsSettingsPanel {

	public ModulesSourcesSettingsPanel( XenonProgramProduct product ) {
		super( product, DisplayMode.SOURCES );

		Button addButton = new Button( "", getProgram().getIconLibrary().getIcon( "add" ) );
		addButton.setOnMousePressed( ( e ) -> newSource() );
		addButton.setOnTouchPressed( ( e ) -> newSource() );

		getButtonBox().addAll( addButton );
	}

	@Override
	protected void updateState( boolean force ) {
		log.atFine().log( "Update product repos force=%s", force );
		getProgram().getTaskManager().submit( new RefreshModuleSources( this, force ) );
	}

}
