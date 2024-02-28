package com.avereon.xenon.tool.settings.panel;

import com.avereon.xenon.XenonProgramProduct;
import com.avereon.xenon.tool.settings.panel.products.DisplayMode;
import com.avereon.xenon.tool.settings.panel.products.ProductsSettingsPanel;
import com.avereon.xenon.tool.settings.panel.products.RefreshProductSources;
import javafx.scene.control.Button;
import lombok.CustomLog;

@CustomLog
public class ProductsSourcesSettingsPanel extends ProductsSettingsPanel {

	public ProductsSourcesSettingsPanel( XenonProgramProduct product ) {
		super( product, DisplayMode.SOURCES );

		Button addButton = new Button( "", getProgram().getIconLibrary().getIcon( "add" ) );
		addButton.setOnMousePressed( ( e ) -> newRepo() );
		addButton.setOnTouchPressed( ( e ) -> newRepo() );

		getButtonBox().addAll( addButton );
	}

	@Override
	protected void updateState( boolean force ) {
		log.atFine().log( "Update product repos" );
		getProgram().getTaskManager().submit( new RefreshProductSources( this ) );
	}

}
