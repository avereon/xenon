package com.xeomar.xenon.tool.product;

import com.xeomar.xenon.BundleKey;
import com.xeomar.xenon.Program;
import com.xeomar.xenon.resource.type.ProgramProductType;
import com.xeomar.xenon.update.MarketCard;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;

class ProductMarketPage extends ProductToolPage {

	private ProductTool productTool;

	ProductMarketPage( Program program, ProductTool productTool ) {
		this.productTool = productTool;
		setTitle( program.getResourceBundle().getString( BundleKey.TOOL, "product-" + ProgramProductType.SOURCES ) );

		Button addButton = new Button( "", program.getIconLibrary().getIcon( "add-market" ) );

		getButtonBox().addAll( addButton );
	}

	@Override
	protected void updateState() {
		ProductTool.log.trace( "Update product markets" );
		productTool.getProgram().getExecutor().submit( new UpdateProductMarkets( productTool ) );
	}

	void setMarkets( List<MarketCard> markets ) {
		// Add a product pane for each card
		List<MarketPane> panes = new ArrayList<>( markets.size() );
		for( MarketCard market : markets ) {
			panes.add( new MarketPane( productTool, market ) );
		}

		getChildren().clear();
		getChildren().addAll( panes );

		updateMarketStates();
	}

	void updateMarketStates() {
		for( Node node : getChildren() ) {
			((MarketPane)node).updateMarketState();
		}
	}

	public void updateMarketState( MarketCard card ) {
		for( Node node : getChildren() ) {
			MarketPane panel = (MarketPane)node;
			if( panel.getSource().equals( card ) ) panel.updateMarketState();
		}
	}

}
