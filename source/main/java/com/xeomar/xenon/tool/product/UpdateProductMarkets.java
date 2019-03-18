package com.xeomar.xenon.tool.product;

import com.xeomar.xenon.task.Task;
import com.xeomar.xenon.update.MarketCard;
import com.xeomar.xenon.update.MarketCardComparator;
import javafx.application.Platform;

import java.util.ArrayList;
import java.util.List;

class UpdateProductMarkets extends Task<Void> {

	private ProductTool productTool;

	UpdateProductMarkets( ProductTool productTool ) {this.productTool = productTool;}

	@Override
	public Void call() {
		List<MarketCard> cards = new ArrayList<>( productTool.getProgram().getProductManager().getCatalogs() );
		cards.sort( new MarketCardComparator( productTool.getProgram(), MarketCardComparator.Field.NAME ) );
		Platform.runLater( () -> productTool.getProductMarketPage().setMarkets( cards ) );
		return null;
	}

}
