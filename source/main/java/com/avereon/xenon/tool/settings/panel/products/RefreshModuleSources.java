package com.avereon.xenon.tool.settings.panel.products;

import com.avereon.xenon.product.RepoState;
import com.avereon.xenon.product.RepoStateComparator;
import com.avereon.xenon.task.Task;
import com.avereon.xenon.tool.settings.panel.ModulesSourcesSettingsPanel;
import com.avereon.zerra.javafx.Fx;

import java.util.ArrayList;
import java.util.List;

public class RefreshModuleSources extends Task<Void> {

	private final ModulesSourcesSettingsPanel parent;

	private final boolean force;

	public RefreshModuleSources( ModulesSourcesSettingsPanel parent, boolean force ) {
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
