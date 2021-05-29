package com.avereon.xenon.tool.product;

import com.avereon.xenon.product.RepoState;
import com.avereon.xenon.product.RepoStateComparator;
import com.avereon.xenon.task.Task;
import com.avereon.zerra.javafx.Fx;

import java.util.ArrayList;
import java.util.List;

class RefreshProductRepos extends Task<Void> {

	private ProductTool productTool;

	private boolean force;

	RefreshProductRepos( ProductTool productTool, boolean force ) {
		this.productTool = productTool;
		this.force = force;
	}

	@Override
	public Void call() {
		Fx.run( () -> productTool.getRepoPage().showUpdating() );
		List<RepoState> cards = new ArrayList<>( productTool.getProgram().getProductManager().getRepos() );
		cards.sort( new RepoStateComparator( RepoStateComparator.Field.NAME ) );
		cards.sort( new RepoStateComparator( RepoStateComparator.Field.RANK ) );
		Fx.run( () -> productTool.getRepoPage().setRepos( cards ) );
		return null;
	}

}
