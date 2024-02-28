package com.avereon.xenon.tool.settings.panel;

import com.avereon.product.Rb;
import com.avereon.xenon.RbKey;
import com.avereon.xenon.XenonProgramProduct;
import com.avereon.xenon.product.RepoState;
import com.avereon.xenon.tool.settings.panel.products.DisplayMode;
import com.avereon.xenon.tool.settings.panel.products.ProductsSettingsPanel;
import com.avereon.xenon.tool.settings.panel.products.RefreshProductRepos;
import com.avereon.xenon.tool.settings.panel.products.RepoTile;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import lombok.CustomLog;

import java.util.List;

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
	public void showUpdating() {
		getChildren().clear();
		getChildren().addAll( new Label( "Updating..." ) );
	}

	@Override
	protected void updateState( boolean force ) {
		log.atFine().log( "Update product repos" );
		getProgram().getTaskManager().submit( new RefreshProductRepos( this ) );
	}

	private void newRepo() {
		String newProductMarketName = Rb.text( getProduct(), RbKey.SETTINGS, "products-source-new" );
		RepoState card = new RepoState();
		card.setName( newProductMarketName );
		card.setUrl( "" );
		card.setEnabled( true );
		card.setRemovable( true );

		RepoTile pane = new RepoTile( getProduct(), this, card );
		getChildren().add( pane );
		pane.setEditUrl( true );
	}

	public void setRepos( List<? extends RepoState> states ) {
		// Create a repo pane for each card
		List<RepoTile> panes = states.stream().map( ( state ) -> new RepoTile( getProgram(), this, state ) ).toList();

		getChildren().clear();
		getChildren().addAll( panes );

		updateRepoStates();
	}

	private void updateRepoStates() {
		getChildren().forEach( node -> ((RepoTile)node).updateRepoState() );
	}

}
