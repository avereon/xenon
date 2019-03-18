package com.xeomar.xenon.tool.product;

import com.xeomar.xenon.task.Task;
import com.xeomar.xenon.task.TaskManager;

class UpdateAvailableProducts extends Task<Void> {

	private boolean force;

	UpdateAvailableProducts() {
		this( false );
	}

	UpdateAvailableProducts( boolean force ) {
		this.force = force;
	}

	@Override
	public Void call() {
		TaskManager.taskThreadCheck();
		//			List<ProductCard> cards = new ArrayList<>( getProgram().getProductManager().getAvailableProducts( force ) );
		//			cards.sort( new ProductCardComparator( getProgram(), ProductCardComparator.Field.NAME ) );
		//			Platform.runLater( () -> availablePage.setProducts( cards ) );
		return null;
	}

}
