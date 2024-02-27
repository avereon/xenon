package com.avereon.xenon.tool.settings.panel.products;

import com.avereon.product.ProductCard;
import com.avereon.product.ProductCardComparator;
import com.avereon.xenon.product.ProgramProductCardComparator;
import com.avereon.xenon.task.Task;
import com.avereon.xenon.task.TaskManager;
import com.avereon.xenon.tool.settings.panel.ProductsInstalledSettingsPanel;
import com.avereon.zarra.javafx.Fx;
import lombok.CustomLog;

import java.util.ArrayList;
import java.util.List;

/**
 * Should be run on the FX platform thread.
 */
@CustomLog
public class RefreshInstalledProducts extends Task<Void> {

	private final ProductsInstalledSettingsPanel parent;

	private final boolean force;

	public RefreshInstalledProducts( ProductsInstalledSettingsPanel parent, boolean force ) {
		this.parent = parent;
		this.force = force;
	}

	@Override
	public Void call() {
		TaskManager.taskThreadCheck();
		try {
			Fx.run( parent::showUpdating );
			List<ProductCard> cards = new ArrayList<>( parent.getProgram().getProductManager().getInstalledProductCards( force ) );
			cards.sort( new ProgramProductCardComparator( parent.getProgram(), ProductCardComparator.Field.NAME ) );
			Fx.run( () -> parent.setProducts( cards ) );
		} catch( Exception exception ) {
			log.atWarning().withCause( exception ).log( "Error refreshing installed products", exception );
			// TODO Notify the user there was a problem refreshing the installed products
		}
		return null;
	}

}
