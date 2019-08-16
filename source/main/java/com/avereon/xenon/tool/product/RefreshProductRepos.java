package com.avereon.xenon.tool.product;

import com.avereon.xenon.task.Task;
import com.avereon.xenon.update.RepoState;
import com.avereon.xenon.update.RepoStateComparator;
import javafx.application.Platform;

import java.util.ArrayList;
import java.util.List;

class RefreshProductRepos extends Task<Void> {

	private ProductTool productTool;

	RefreshProductRepos( ProductTool productTool ) {this.productTool = productTool;}

	@Override
	public Void call() {
		List<RepoState> cards = new ArrayList<>( productTool.getProgram().getProductManager().getRepos() );
		cards.sort( new RepoStateComparator( RepoStateComparator.Field.NAME ) );
		cards.sort( new RepoStateComparator( RepoStateComparator.Field.RANK ) );
		Platform.runLater( () -> productTool.getRepoPage().setRepos( cards ) );
		return null;
	}

}
