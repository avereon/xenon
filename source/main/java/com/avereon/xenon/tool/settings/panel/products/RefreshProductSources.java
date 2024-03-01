package com.avereon.xenon.tool.settings.panel.products;

import com.avereon.xenon.product.RepoState;
import com.avereon.xenon.product.RepoStateComparator;
import com.avereon.xenon.task.Task;
import com.avereon.xenon.tool.settings.panel.ProductsSourcesSettingsPanel;
import com.avereon.zarra.javafx.Fx;

import java.util.ArrayList;
import java.util.List;

public class RefreshProductSources extends Task<Void> {

	private final ProductsSourcesSettingsPanel parent;

	private final boolean force;

	public RefreshProductSources( ProductsSourcesSettingsPanel parent, boolean force ) {
		this.parent = parent;
		this.force = force;
	}

	@Override
	public Void call() {
		Fx.run( parent::showUpdating );

		// Load the product repos

		List<RepoState> cards = new ArrayList<>( parent.getProgram().getProductManager().getRepos( force ) );
		cards.sort( new RepoStateComparator( RepoStateComparator.Field.NAME ) );
		cards.sort( new RepoStateComparator( RepoStateComparator.Field.RANK ) );
		Fx.run( () -> parent.setSources( cards ) );
		return null;
	}

}
