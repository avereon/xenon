package com.avereon.xenon.tool.product;

import com.avereon.util.LogUtil;
import com.avereon.xenon.BundleKey;
import com.avereon.xenon.Program;
import com.avereon.xenon.resource.type.ProgramProductType;
import com.avereon.product.RepoCard;
import javafx.scene.Node;
import javafx.scene.control.Button;
import org.slf4j.Logger;

import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.stream.Collectors;

class RepoPage extends ProductToolPage {

	private static final Logger log = LogUtil.get( MethodHandles.lookup().lookupClass() );

	private Program program;

	private ProductTool productTool;

	RepoPage( Program program, ProductTool productTool ) {
		this.program = program;
		this.productTool = productTool;
		setTitle( program.getResourceBundle().getString( BundleKey.TOOL, "product-" + ProgramProductType.SOURCES ) );

		Button addButton = new Button( "", program.getIconLibrary().getIcon( "add" ) );
		addButton.setOnMousePressed( (e) -> addRepo() );
		addButton.setOnTouchPressed( (e) -> addRepo() );

		getButtonBox().addAll( addButton );
	}

	@Override
	protected void updateState() {
		ProductTool.log.debug( "Update product repos" );
		productTool.getProgram().getTaskManager().submit( new RefreshProductRepos( productTool ) );
	}

	private void addRepo() {
		RepoCard card = new RepoCard();
		card.setName( "New Product Market" );
		card.setRepo( "" );
		card.setEnabled( true );
		card.setRemovable( true );
		program.getProductManager().addRepo( card );

		updateState();
	}

	void setRepos( List<RepoCard> repos ) {
		// Create a repo pane for each card
		List<RepoPane> panes = repos.stream().map( ( r ) -> new RepoPane( productTool, r ) ).collect( Collectors.toList() );

		getChildren().clear();
		getChildren().addAll( panes );

		updateRepoStates();
	}

	void updateRepoStates() {
		getChildren().forEach( (n) -> ((RepoPane)n).updateRepoState() );
	}

	public void updateRepoState( RepoCard card ) {
		for( Node node : getChildren() ) {
			RepoPane panel = (RepoPane)node;
			if( panel.getSource().equals( card ) ) panel.updateRepoState();
		}
	}

}
