package com.xeomar.xenon.tool.product;

import com.xeomar.xenon.task.Task;
import com.xeomar.xenon.update.RepoCard;
import com.xeomar.xenon.update.RepoCardComparator;
import javafx.application.Platform;

import java.util.ArrayList;
import java.util.List;

class UpdateProductRepos extends Task<Void> {

	private ProductTool productTool;

	UpdateProductRepos( ProductTool productTool ) {this.productTool = productTool;}

	@Override
	public Void call() {
		List<RepoCard> cards = new ArrayList<>( productTool.getProgram().getProductManager().getRepos() );
		cards.sort( new RepoCardComparator( productTool.getProgram(), RepoCardComparator.Field.NAME ) );
		cards.sort( new RepoCardComparator( productTool.getProgram(), RepoCardComparator.Field.RANK ) );
		Platform.runLater( () -> productTool.getRepoPage().setRepos( cards ) );
		return null;
	}

}
