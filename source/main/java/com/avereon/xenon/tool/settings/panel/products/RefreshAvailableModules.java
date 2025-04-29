package com.avereon.xenon.tool.settings.panel.products;

import com.avereon.product.ProductCard;
import com.avereon.product.ProductCardComparator;
import com.avereon.xenon.product.ProgramProductCardComparator;
import com.avereon.xenon.task.Task;
import com.avereon.xenon.task.TaskManager;
import com.avereon.xenon.tool.settings.panel.ModulesAvailableSettingsPanel;
import com.avereon.zarra.javafx.Fx;

import java.util.ArrayList;
import java.util.List;

public class RefreshAvailableModules extends Task<Void> {

	private final ModulesAvailableSettingsPanel parent;

	private final boolean force;

	public RefreshAvailableModules( ModulesAvailableSettingsPanel parent, boolean force ) {
		this.parent = parent;
		this.force = force;
	}

	@Override
	public Void call() {
		TaskManager.taskThreadCheck();
		Fx.run( parent::showUpdating );
		List<ProductCard> cards = new ArrayList<>( parent.getProgram().getProductManager().getAvailableProducts( force ) );
		cards.sort( new ProgramProductCardComparator( parent.getProgram(), ProductCardComparator.Field.NAME ) );
		Fx.run( () -> parent.setProducts( cards ) );
		return null;
	}

}
