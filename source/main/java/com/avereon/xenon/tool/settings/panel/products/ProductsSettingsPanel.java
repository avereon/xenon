package com.avereon.xenon.tool.settings.panel.products;

import com.avereon.product.ProductCard;
import com.avereon.product.ProductCardComparator;
import com.avereon.product.Rb;
import com.avereon.xenon.RbKey;
import com.avereon.xenon.XenonProgramProduct;
import com.avereon.xenon.product.ProgramProductCardComparator;
import com.avereon.xenon.tool.settings.SettingsPanel;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;

import java.util.*;

public abstract class ProductsSettingsPanel extends SettingsPanel {

	protected static final int ICON_SIZE = 48;

	private final HBox buttons;

	private final ProductList productList;

	protected ProductsSettingsPanel( XenonProgramProduct product, DisplayMode displayMode ) {
		super( product );
		this.buttons = new HBox();
		this.buttons.setId( "tool-product-page-header-buttons" );
		this.productList = new ProductList( this, displayMode );

		String mode = displayMode.name().toLowerCase();

		// Add the title to the panel based on the display mode
		addTitle( Rb.text( product, RbKey.SETTINGS, "products-" + mode ) );
		getChildren().add( new BorderPane( null, null, buttons, null, null ) );

		// Add the product list to the panel wrapped in a scroll pane
		ScrollPane productListScroller = new ScrollPane( productList );
		productListScroller.setFitToWidth( true );
		productListScroller.setFitToHeight( true );
		getChildren().add( productListScroller );
	}

	protected ObservableList<Node> getButtonBox() {
		return buttons.getChildren();
	}

	protected List<ProductCard> createSourceList( List<ProductCard> cards ) {
		// Clean out duplicate releases and create unique product list.
		List<ProductCard> uniqueList = new ArrayList<>();
		Map<String, List<ProductCard>> cardMap = new HashMap<>();
		for( ProductCard card : cards ) {
			List<ProductCard> productReleaseCards = cardMap.get( card.getProductKey() );
			if( productReleaseCards == null ) {
				productReleaseCards = new ArrayList<>();
				productReleaseCards.add( card );
				cardMap.put( card.getProductKey(), productReleaseCards );
				uniqueList.add( card );
			} else {
				boolean found = false;
				for( ProductCard releaseCard : productReleaseCards ) {
					found = found | card.getRelease().equals( releaseCard.getRelease() );
				}
				if( !found ) productReleaseCards.add( card );
			}
		}

		// Create the sources.
		List<ProductCard> sources = new ArrayList<>();
		for( ProductCard card : uniqueList ) {
			List<ProductCard> releases = cardMap.get( card.getProductKey() );
			if( releases != null ) {
				releases.sort( Collections.reverseOrder( new ProgramProductCardComparator( getProgram(), ProductCardComparator.Field.RELEASE ) ) );
				sources.add( releases.getFirst() );
			}
		}

		return sources;
	}

}
