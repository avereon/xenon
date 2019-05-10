package com.xeomar.xenon.tool.product;

import com.xeomar.product.ProductCard;
import com.xeomar.product.ProductCardComparator;
import com.xeomar.xenon.task.Task;
import com.xeomar.xenon.task.TaskManager;
import javafx.application.Platform;

import java.util.ArrayList;
import java.util.List;

class RefreshAvailableProducts extends Task<Void> {

	private ProductTool productTool;

	private boolean force;

	RefreshAvailableProducts( ProductTool productTool ) {
		this( productTool, false );
	}

	RefreshAvailableProducts( ProductTool productTool, boolean force ) {
		this.productTool = productTool;
		this.force = force;
	}

	@Override
	public Void call() {
		TaskManager.taskThreadCheck();
		List<ProductCard> cards = new ArrayList<>( productTool.getProgram().getProductManager().getAvailableProducts( force ) );
		cards.sort( new ProductCardComparator( productTool.getProgram(), ProductCardComparator.Field.NAME ) );
		Platform.runLater( () -> productTool.getAvailablePage().setProducts( cards ) );
		return null;
	}

}
