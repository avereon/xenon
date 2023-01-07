package com.avereon.xenon.tool.product;

import com.avereon.product.Rb;
import com.avereon.xenon.RbKey;
import com.avereon.xenon.Program;
import com.avereon.xenon.product.RepoState;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import lombok.CustomLog;

import java.util.List;
import java.util.stream.Collectors;

@CustomLog
class RepoPage extends ProductToolPage {

	private final Program program;

	private final ProductTool productTool;

	RepoPage( Program program, ProductTool productTool ) {
		this.program = program;
		this.productTool = productTool;
		setTitle( Rb.text( RbKey.TOOL, "product-" + ProductTool.SOURCES ) );

		Button addButton = new Button( "", program.getIconLibrary().getIcon( "add" ) );
		addButton.setOnMousePressed( ( e ) -> newRepo() );
		addButton.setOnTouchPressed( ( e ) -> newRepo() );

		getButtonBox().addAll( addButton );

		showUpdating();
	}

	@Override
	protected void showUpdating() {
		getChildren().clear();
		getChildren().addAll( new Label( "Updating..." ) );
	}

	@Override
	protected void updateState( boolean force ) {
		log.atFine().log( "Update product repos" );
		productTool.getProgram().getTaskManager().submit( new RefreshProductRepos( productTool, false ) );
	}

	private void newRepo() {
		RepoState card = new RepoState();
		card.setName( "New Product Market" );
		card.setUrl( "" );
		card.setEnabled( true );
		card.setRemovable( true );

		RepoPane pane = new RepoPane( productTool, this, card );
		getChildren().add( pane );
		pane.setEditUrl( true );
	}

	void setRepos( List<? extends RepoState> states ) {
		// Create a repo pane for each card
		List<RepoPane> panes = states.stream().map( ( state ) -> new RepoPane( productTool, this, state ) ).collect( Collectors.toList() );

		getChildren().clear();
		getChildren().addAll( panes );

		updateRepoStates();
	}

	private void updateRepoStates() {
		getChildren().forEach( node -> ((RepoPane)node).updateRepoState() );
	}

}
