package com.avereon.xenon.tool.product;

import com.avereon.product.RepoCard;
import com.avereon.xenon.Program;
import com.avereon.xenon.UiFactory;
import com.avereon.xenon.task.Task;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import org.tbee.javafx.scene.layout.MigPane;

class RepoPane extends MigPane {

	private ProductTool productTool;

	private RepoCard source;

	private RepoPage page;

	private Label iconLabel;

	private Label nameLabel;

	private TextField nameField;

	private Label repoLabel;

	private TextField repoField;

	private Button enableButton;

	private Button removeButton;

	private boolean editName;

	private boolean editUrl;

	private HBox nameBox;

	private HBox repoBox;

	public RepoPane( ProductTool productTool, RepoPage page, RepoCard source ) {
		super( "insets 0, gap " + UiFactory.PAD );

		this.productTool = productTool;
		this.page = page;
		this.source = source;

		setId( "tool-product-market" );

		Program program = productTool.getProgram();

		String iconUri = source.getIcon();
		Node marketIcon = program.getIconLibrary().getIcon( iconUri, "market", ProductTool.ICON_SIZE );

		iconLabel = new Label( null, marketIcon );
		iconLabel.setId( "tool-product-market-icon" );

		nameLabel = new Label( source.getName() );
		nameLabel.setId( "tool-product-market-name" );
		nameLabel.setOnMousePressed( ( event ) -> setEditName( source.isRemovable() ) );
		nameField = new TextField( source.getName() );
		nameField.setId( "tool-product-market-name-editor" );
		nameField.focusedProperty().addListener( ( event ) -> { if( !nameField.focusedProperty().get() ) cancelEditName(); } );
		nameField.setOnKeyPressed( ( event ) -> {
			if( event.getCode() == KeyCode.ENTER ) submitRepo();
			if( event.getCode() == KeyCode.ESCAPE ) cancelEditName();
		} );

		nameBox = new HBox( nameLabel );
		HBox.setHgrow( nameLabel, Priority.ALWAYS );
		HBox.setHgrow( nameField, Priority.ALWAYS );

		repoLabel = new Label( source.getRepo() );
		repoLabel.setId( "tool-product-market-uri" );
		repoLabel.setOnMousePressed( ( event ) -> setEditUrl( source.isRemovable() ) );
		repoField = new TextField( source.getRepo() );
		repoField.setId( "tool-product-market-uri-editor" );
		repoField.focusedProperty().addListener( ( event ) -> { if( !repoField.focusedProperty().get() ) cancelEditUrl(); } );
		repoField.setOnKeyPressed( ( event ) -> {
			if( event.getCode() == KeyCode.ENTER ) submitRepo();
			if( event.getCode() == KeyCode.ESCAPE ) cancelEditUrl();
		} );

		repoBox = new HBox( repoLabel );
		HBox.setHgrow( repoLabel, Priority.ALWAYS );
		HBox.setHgrow( repoField, Priority.ALWAYS );

		enableButton = new Button( "", productTool.getProgram().getIconLibrary().getIcon( source.isEnabled() ? "disable" : "enable" ) );
		removeButton = new Button( "", program.getIconLibrary().getIcon( "remove" ) );

		add( iconLabel, "spany, aligny center" );
		add( nameBox, "growx, pushx" );
		add( enableButton );
		add( repoBox, "newline, growx" );
		add( removeButton );
	}

	RepoCard getSource() {
		return source;
	}

	void setEditName( boolean editName ) {
		this.editName = editName;
		updateRepoState();
	}

	void setEditUrl( boolean editUrl ) {
		this.editUrl = editUrl;
		updateRepoState();
	}

	void updateRepoState() {
		nameLabel.setText( source.getName() );
		nameLabel.setDisable( !source.isEnabled() );
		nameField.setText( source.getName() );

		nameBox.getChildren().replaceAll( ( n ) -> (editName ? nameField : nameLabel) );

		repoLabel.setText( source.getRepo() );
		repoLabel.setDisable( !source.isEnabled() );
		repoField.setText( source.getRepo() );

		repoBox.getChildren().replaceAll( ( n ) -> (editUrl ? repoField : repoLabel) );

		enableButton.setGraphic( productTool.getProgram().getIconLibrary().getIcon( source.isEnabled() ? "disable" : "enable" ) );
		removeButton.setDisable( !source.isRemovable() );

		enableButton.setOnAction( ( event ) -> toggleEnabled() );
		removeButton.setOnAction( ( event ) -> removeRepo() );

		if( editName ) this.nameField.requestFocus();
		if( editUrl ) this.repoField.requestFocus();
	}

	private void submitRepo() {
		// Get the values before the fields are hidden
		String repoName = nameField.getText();
		String repoUrl = repoField.getText();

		setEditName( false );
		setEditUrl( false );

		source.setName( repoName );
		source.setRepo( repoUrl );

		productTool.getProgram().getProductManager().addRepo( source );

		// TODO Load the repo metadata...

		updateRepoState();
	}

	private void cancelEditName() {
		//if( !verified ) page.getChildren().remove( this );
		setEditName( false );
	}

	private void cancelEditUrl() {
		//if( !verified ) page.getChildren().remove( this );
		setEditUrl( false );
	}

	private void toggleEnabled() {
		productTool.getProgram().getProductManager().setRepoEnabled( source, !productTool.getProgram().getProductManager().isRepoEnabled( source ) );
		updateRepoState();
	}

	private void removeRepo() {
		productTool.getProgram().getTaskManager().submit( Task.of( "Remove repo", () -> {
			try {
				productTool.getProgram().getProductManager().removeRepo( source );
				productTool.getSelectedPage().updateState();
			} catch( Exception exception ) {
				ProductTool.log.warn( "Error uninstalling product", exception );
			}
		} ) );
	}

}
