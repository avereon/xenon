package com.avereon.xenon.tool.product;

import com.avereon.product.ProductCard;
import com.avereon.product.ProductCardComparator;
import com.avereon.xenon.product.ProgramProductCardComparator;
import com.avereon.xenon.task.Task;
import com.avereon.xenon.task.TaskManager;
import com.avereon.zarra.javafx.Fx;
import lombok.CustomLog;

import java.util.ArrayList;
import java.util.List;

/**
 * Should be run on the FX platform thread.
 */
@CustomLog
@Deprecated
class RefreshInstalledProducts extends Task<Void> {

	private final ProductTool productTool;

	private final boolean force;

	public RefreshInstalledProducts( ProductTool productTool, boolean force ) {
		this.productTool = productTool;
		this.force = force;
	}

	@Override
	public Void call() {
		TaskManager.taskThreadCheck();
		try {
			Fx.run( () -> productTool.getInstalledPage().showUpdating());
			List<ProductCard> cards = new ArrayList<>( productTool.getProgram().getProductManager().getInstalledProductCards( force ) );
			cards.sort( new ProgramProductCardComparator( productTool.getProgram(), ProductCardComparator.Field.NAME ) );
			Fx.run( () -> productTool.getInstalledPage().setProducts( cards ) );
		} catch( Exception exception ) {
			log.atWarning().withCause( exception ).log( "Error refreshing installed products", exception );
			// TODO Notify the user there was a problem refreshing the installed products
		}
		return null;
	}

}
