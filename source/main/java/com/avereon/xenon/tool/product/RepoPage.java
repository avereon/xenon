package com.avereon.xenon.tool.product;

import com.avereon.util.LogUtil;
import com.avereon.xenon.BundleKey;
import com.avereon.xenon.Program;
import com.avereon.xenon.resource.type.ProgramProductType;
import com.avereon.xenon.update.RepoState;
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
		addButton.setOnMousePressed( ( e ) -> addRepo() );
		addButton.setOnTouchPressed( ( e ) -> addRepo() );

		getButtonBox().addAll( addButton );
	}

	@Override
	protected void updateState() {
		ProductTool.log.debug( "Update product repos" );
		productTool.getProgram().getTaskManager().submit( new RefreshProductRepos( productTool ) );
	}

	private void addRepo() {
		RepoState card = new RepoState();
		card.setName( "New Product Market" );
		card.setUrl( "" );
		card.setEnabled( true );
		card.setRemovable( true );
		program.getProductManager().addRepo( card );

		// NEXT Maybe need to do more than a simple state update here
		//updateState();

		RepoPane pane = new RepoPane( productTool, this, card );
		getChildren().add( pane );
		pane.setEditUrl( true );
	}

	void setRepos( List<? extends RepoState> repos ) {
		// Create a repo pane for each card
		List<RepoPane> panes = repos.stream().map( ( r ) -> new RepoPane( productTool, this, r ) ).collect( Collectors.toList() );

		getChildren().clear();
		getChildren().addAll( panes );

		updateRepoStates();
	}

	void updateRepoStates() {
		getChildren().forEach( n -> ((RepoPane)n).updateRepoState() );
	}

	public void updateRepoState( RepoState card ) {
		getChildren().stream().map( n -> (RepoPane)n ).filter( ( p ) -> p.getSource().equals( card ) ).forEach( RepoPane::updateRepoState );
	}

}
