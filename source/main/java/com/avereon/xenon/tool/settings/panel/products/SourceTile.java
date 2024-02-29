package com.avereon.xenon.tool.settings.panel.products;

import com.avereon.product.RepoCard;
import com.avereon.util.TextUtil;
import com.avereon.xenon.UiFactory;
import com.avereon.xenon.XenonProgramProduct;
import com.avereon.xenon.product.RepoState;
import com.avereon.xenon.task.Task;
import com.avereon.zarra.javafx.Fx;
import javafx.geometry.HPos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import lombok.CustomLog;
import org.controlsfx.control.ToggleSwitch;

import java.util.Objects;

@CustomLog
public class SourceTile extends BaseTile {

	private final RepoState source;

	private Label iconLabel;

	private final Label nameLabel;

	private final TextField nameField;

	private final Label urlLabel;

	private final TextField urlField;

	private ToggleSwitch enableSwitch;

	private final Button removeButton;

	private boolean editName;

	private boolean editUrl;

	private final HBox nameBox;

	private final HBox urlBox;

	public SourceTile( XenonProgramProduct product, ProductsSettingsPanel parent, RepoState source ) {
		super( product, parent );

		setHgap( UiFactory.PAD );
		setVgap( UiFactory.PAD );

		this.source = source;

		setId( "tool-product-market" );

		Node marketIcon = getProgram().getIconLibrary().getIcon( source.getIcons(), "market", ProductsSettingsPanel.ICON_SIZE );

		iconLabel = new Label( null, marketIcon );
		iconLabel.setId( "tool-product-market-icon" );

		nameLabel = new Label( source.getName() );
		nameLabel.setId( "tool-product-market-name" );
		nameLabel.minWidthProperty().bind( nameLabel.prefWidthProperty() );
		nameLabel.setOnMousePressed( ( event ) -> setEditName( source.isRemovable() ) );
		nameField = new TextField( source.getName() );
		nameField.setId( "tool-product-market-name-editor" );
		nameField.focusedProperty().addListener( ( value, oldValue, newValue ) -> {if( !newValue ) commitEditName();} );
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
		urlField.focusedProperty().addListener( ( value, oldValue, newValue ) -> {if( !newValue ) commitEditUrl();} );
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
		enableSwitch.selectedProperty().addListener( ( observable, oldValue, newValue ) -> toggleEnabled( newValue ) );

		removeButton = new Button( "", getProgram().getIconLibrary().getIcon( "remove" ) );
		removeButton.setOnAction( ( event ) -> removeRepo() );

		GridPane.setRowSpan( iconLabel, 2 );
		GridPane.setHgrow( nameBox, Priority.ALWAYS );
		GridPane.setHalignment( enableSwitch, HPos.RIGHT );
		GridPane.setHgrow( urlBox, Priority.ALWAYS );
		GridPane.setHalignment( removeButton, HPos.RIGHT );

		add( iconLabel, 0, 0 );
		add( nameBox, 1, 0 );
		add( enableSwitch, 2, 0 );
		add( urlBox, 1, 1 );
		add( removeButton, 2, 1 );
	}

	private RepoCard getSource() {
		return source;
	}

	@Override
	public void updateTileState() {
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
		Fx.run( this::updateTileState );
	}

	public void setEditUrl( boolean editUrl ) {
		if( editUrl && editName ) commitEditName();
		this.editUrl = editUrl;
		Fx.run( this::updateTileState );
	}

	private void commitEditName() {
		//log.atConfig().withCause( new Throwable() ).log( "Commit name: editName=%s editUrl=%s", editName, editUrl );
		if( !editName ) return;
		source.setName( nameField.getText() );
		if( isValidRepoState( source ) ) getProductManager().addRepo( source );
		setEditName( false );
	}

	private void commitEditUrl() {
		//log.atConfig().withCause( new Throwable() ).log( "Update url: editName=%s editUrl=%s", editName, editUrl );
		if( !editUrl ) return;
		source.setUrl( urlField.getText() );
		 if( isValidRepoState( source ) ) getProductManager().addRepo( source );
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
		getProductManager().setRepoEnabled( source, enabled );
		Fx.run( this::updateTileState );
	}

	private void removeRepo() {
		getProgram().getTaskManager().submit( Task.of( "Remove repo", () -> {
			try {
				getProductManager().removeRepo( source );
				getProductSettingsPanel().updateState( false );
			} catch( Exception exception ) {
				log.atWarning().withCause( exception ).log( "Error removing repository" );
			}
		} ) );
	}

}
