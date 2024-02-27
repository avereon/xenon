package com.avereon.xenon.tool.settings.panel;

import com.avereon.product.Rb;
import com.avereon.xenon.RbKey;
import com.avereon.xenon.XenonProgramProduct;
import com.avereon.xenon.product.RepoState;
import com.avereon.xenon.tool.settings.panel.products.DisplayMode;
import com.avereon.xenon.tool.settings.panel.products.ProductsSettingsPanel;
import com.avereon.xenon.tool.settings.panel.products.RepoPane;
import javafx.scene.control.Button;

public class ProductsSourcesSettingsPanel extends ProductsSettingsPanel {

	public ProductsSourcesSettingsPanel( XenonProgramProduct product ) {
		super( product, DisplayMode.SOURCES );

		Button addButton = new Button( "", getProgram().getIconLibrary().getIcon( "add" ) );
		addButton.setOnMousePressed( ( e ) -> newRepo() );
		addButton.setOnTouchPressed( ( e ) -> newRepo() );

		getButtonBox().addAll( addButton );
	}

	private void newRepo() {
		String newProductMarketName = Rb.text( getProduct(), RbKey.SETTINGS, "products-source-new" );
		RepoState card = new RepoState();
		card.setName( newProductMarketName );
		card.setUrl( "" );
		card.setEnabled( true );
		card.setRemovable( true );

		RepoPane pane = new RepoPane( getProduct(), this, card );
		getChildren().add( pane );
		pane.setEditUrl( true );
	}

}
