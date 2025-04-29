package com.avereon.xenon.tool.settings.panel.products;

import com.avereon.product.ProductCard;
import com.avereon.product.Rb;
import com.avereon.xenon.RbKey;
import com.avereon.xenon.product.RepoState;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Labeled;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TileList extends VBox {

	private final ProductsSettingsPanel parent;

	private final DisplayMode displayMode;

	private final Labeled message;

	private final String refreshMessage;

	private final String missingMessage;

	public TileList( ProductsSettingsPanel parent, DisplayMode displayMode ) {
		this.parent = parent;
		this.displayMode = displayMode;

		getStyleClass().addAll( "tool-product-list" );

		String mode = displayMode.name().toLowerCase();
		this.refreshMessage = Rb.text( RbKey.TOOL, "product-" + mode + "-refresh" );
		this.missingMessage = Rb.text( RbKey.TOOL, "product-" + mode + "-missing" );

		this.message = new Label();
		this.message.setPrefWidth( Double.MAX_VALUE );
		this.message.getStyleClass().addAll( "tool-product-message" );

		getChildren().add( message );

		showUpdating();
	}

	protected void showUpdating() {
		showMessage( refreshMessage );
	}

	private void showMissing() {
		showMessage( missingMessage );
	}

	private void showMessage( String messageText ) {
		getChildren().clear();
		message.setText( messageText );
		getChildren().addAll( message );
	}

	private void updateProductStates() {
		for( Node node : getChildren() ) {
			if( node instanceof ProductTile pane ) pane.updateTileState();
		}
	}

	List<BaseTile> getTiles() {
		return getChildren().filtered( ( node ) -> node instanceof BaseTile ).stream().map( node -> (BaseTile)node ).toList();
	}

	void setProducts( List<ProductCard> cards ) {
		setProducts( cards, Map.of() );
	}

	void setProducts( List<ProductCard> cards, Map<String, ProductCard> productUpdates ) {
		if( cards.isEmpty() ) {
			showMissing();
		} else {
			List<ProductTile> tiles = parent
				.createSourceList( cards )
				.stream()
				.map( ( source ) -> new ProductTile( parent.getProduct(), parent, source, productUpdates.get( source.getProductKey() ), displayMode ) )
				.toList();
			setTiles( tiles );
		}
	}

	void addRepo( RepoState card ) {
		// Add a repo tile for the card
		SourceTile tile = new SourceTile( parent.getProduct(), parent, card );
		tile.setEditUrl( true );

		List<BaseTile> tiles = new ArrayList<>( getTiles() );
		tiles.add( tile );
		setTiles( tiles );
	}

	public void setRepos( List<? extends RepoState> cards ) {
		if( cards.isEmpty() ) {
			showMissing();
		} else {
			setTiles( cards.stream().map( ( source ) -> new SourceTile( parent.getProduct(), parent, source ) ).toList() );
		}
	}

	private void setTiles( List<? extends BaseTile> tiles ) {
		getChildren().clear();
		getChildren().addAll( tiles );
		updateProductStates();
	}

}
