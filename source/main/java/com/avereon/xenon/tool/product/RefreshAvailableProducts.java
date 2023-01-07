package com.avereon.xenon.tool.product;

import com.avereon.product.ProductCard;
import com.avereon.product.ProductCardComparator;
import com.avereon.xenon.task.Task;
import com.avereon.xenon.task.TaskManager;
import com.avereon.zarra.javafx.Fx;

import java.util.ArrayList;
import java.util.List;

class RefreshAvailableProducts extends Task<Void> {

	private ProductTool productTool;

	private boolean force;

	RefreshAvailableProducts( ProductTool productTool, boolean force ) {
		this.productTool = productTool;
		this.force = force;
	}

	@Override
	public Void call() {
		TaskManager.taskThreadCheck();
		Fx.run( () -> productTool.getAvailablePage().showUpdating() );
		List<ProductCard> cards = new ArrayList<>( productTool.getProgram().getProductManager().getAvailableProducts( force ) );
		cards.sort( new ProgramProductCardComparator( productTool.getProgram(), ProductCardComparator.Field.NAME ) );
		Fx.run( () -> productTool.getAvailablePage().setProducts( cards ) );
		return null;
	}

}
