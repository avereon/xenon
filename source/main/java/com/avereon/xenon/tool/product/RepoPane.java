package com.avereon.xenon.tool.product;

import com.avereon.product.RepoCard;
import com.avereon.util.TextUtil;
import com.avereon.xenon.Program;
import com.avereon.xenon.UiFactory;
import com.avereon.xenon.product.RepoState;
import com.avereon.xenon.task.Task;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import org.controlsfx.control.ToggleSwitch;
import org.tbee.javafx.scene.layout.MigPane;

import java.util.Objects;

class RepoPane extends MigPane {

	private ProductTool productTool;

	private RepoState source;

	private RepoPage page;

	private Label iconLabel;

	private Label nameLabel;

	private TextField nameField;

	private Label urlLabel;

	private TextField urlField;

	private ToggleSwitch enableSwitch;

	private Button removeButton;

	private boolean editName;

	private boolean editUrl;

	private HBox nameBox;

	private HBox urlBox;

	public RepoPane( ProductTool productTool, RepoPage page, RepoState source ) {
		super( "insets 0, gap " + UiFactory.PAD );

		this.productTool = productTool;
		this.page = page;
		this.source = source;

		setId( "tool-product-market" );

		Program program = productTool.getProgram();
		Node marketIcon = program.getIconLibrary().getIcon( source.getIcons(), "market", ProductTool.ICON_SIZE );

		iconLabel = new Label( null, marketIcon );
		iconLabel.setId( "tool-product-market-icon" );

		nameLabel = new Label( source.getName() );
		nameLabel.setId( "tool-product-market-name" );
		nameLabel.minWidthProperty().bind( nameLabel.prefWidthProperty() );
		nameLabel.setOnMousePressed( ( event ) -> setEditName( source.isRemovable() ) );
		nameField = new TextField( source.getName() );
		nameField.setId( "tool-product-market-name-editor" );
		nameField.focusedProperty().addListener( ( value, oldValue, newValue ) -> {if( oldValue ) commitEditName();} );
		nameField.setOnKeyPressed( ( event ) -> {
			if( event.getCode() == KeyCode.ENTER ) commitEditName();
			if( event.getCode() == KeyCode.ESCAPE ) cancelEditName();
		} );

		nameBox = new HBox( nameLabel );
		nameBox.setOnMousePressed( ( event ) -> setEditName( source.isRemovable() ) );
		HBox.setHgrow( nameLabel, Priority.ALWAYS );
		HBox.setHgrow( nameField, Priority.ALWAYS );

		urlLabel = new Label( source.getUrl() );
		urlLabel.setId( "tool-product-market-uri" );
		urlLabel.minWidthProperty().bind( urlLabel.prefWidthProperty() );
		urlLabel.setOnMousePressed( ( event ) -> setEditUrl( source.isRemovable() ) );
		urlField = new TextField( source.getUrl() );
		urlField.setId( "tool-product-market-uri-editor" );
		urlField.focusedProperty().addListener( ( value, oldValue, newValue ) -> {if( oldValue ) commitEditUrl();} );
		urlField.setOnKeyPressed( ( event ) -> {
			if( event.getCode() == KeyCode.ENTER ) commitEditUrl();
			if( event.getCode() == KeyCode.ESCAPE ) cancelEditUrl();
		} );

		urlBox = new HBox( urlLabel );
		urlBox.setOnMousePressed( ( event ) -> setEditUrl( source.isRemovable() ) );
		HBox.setHgrow( urlLabel, Priority.ALWAYS );
		HBox.setHgrow( urlField, Priority.ALWAYS );

		enableSwitch = new ToggleSwitch();
		enableSwitch.setSelected( source.isEnabled() );
		enableSwitch.selectedProperty().addListener( ( observable, oldValue, newValue ) -> toggleEnabled(newValue) );

		removeButton = new Button( "", program.getIconLibrary().getIcon( "remove" ) );
		removeButton.setOnAction( ( event ) -> removeRepo() );

		add( iconLabel, "spany, aligny center" );
		add( nameBox, "growx, pushx" );
		add( enableSwitch, "w min" );
		add( urlBox, "newline, growx" );
		add( removeButton );
	}

	private RepoCard getSource() {
		return source;
	}

	void updateRepoState() {
		nameLabel.setText( source.getName() );
		nameLabel.setDisable( !source.isEnabled() );
		nameField.setText( source.getName() );
		nameBox.getChildren().replaceAll( ( n ) -> (editName ? nameField : nameLabel) );

		urlLabel.setText( source.getUrl() );
		urlLabel.setDisable( !source.isEnabled() );
		urlField.setText( source.getUrl() );
		urlBox.getChildren().replaceAll( ( n ) -> (editUrl ? urlField : urlLabel) );

		removeButton.setDisable( !source.isRemovable() );

		if( editName ) this.nameField.requestFocus();
		if( editUrl ) this.urlField.requestFocus();
	}

	private void setEditName( boolean editName ) {
		if( editName && editUrl ) commitEditUrl();
		this.editName = editName;
		updateRepoState();
	}

	void setEditUrl( boolean editUrl ) {
		if( editUrl && editName ) commitEditName();
		this.editUrl = editUrl;
		updateRepoState();
	}

	private void commitEditName() {
		if( !editName ) return;
		source.setName( nameField.getText() );
		if( isValidRepoState( source ) ) productTool.getProgram().getProductManager().addRepo( source );
		setEditName( false );
	}

	private void commitEditUrl() {
		if( !editUrl ) return;
		source.setUrl( urlField.getText() );
		if( isValidRepoState( source ) ) productTool.getProgram().getProductManager().addRepo( source );
		// TODO Load the repo metadata...
		setEditUrl( false );
	}

	private boolean isValidRepoState( RepoState state ) {
		boolean nameValid = !(Objects.isNull( state.getName() ) || state.getName().isBlank());
		boolean urlValid = !(Objects.isNull( state.getName() ) || state.getName().isBlank());
		return nameValid && urlValid;
	}

	private void cancelEditName() {
		setEditName( false );
	}

	private void cancelEditUrl() {
		if( TextUtil.isEmpty( urlField.getText() ) ) removeRepo();
		setEditUrl( false );
	}

	private void toggleEnabled( boolean enabled ) {
		productTool.getProgram().getProductManager().setRepoEnabled( source, enabled );
		updateRepoState();
	}

	private void removeRepo() {
		productTool.getProgram().getTaskManager().submit( Task.of( "Remove repo", () -> {
			try {
				productTool.getProgram().getProductManager().removeRepo( source );
				page.updateState( false );
			} catch( Exception exception ) {
				ProductTool.log.warn( "Error removing repository", exception );
			}
		} ) );
	}

}
