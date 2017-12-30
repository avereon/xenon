package com.xeomar.xenon.tool.settings.editor;

import com.xeomar.settings.SettingsEvent;
import com.xeomar.xenon.ProgramProduct;
import com.xeomar.xenon.UiManager;
import com.xeomar.xenon.tool.settings.Setting;
import com.xeomar.xenon.tool.settings.SettingEditor;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.FileChooser;

import java.io.File;

public class FileSettingEditor extends SettingEditor implements EventHandler<KeyEvent>, ChangeListener<Boolean> {

	private Label label;

	private TextField field;

	private Button button;

	public FileSettingEditor( ProgramProduct product, Setting setting ) {
		super( product, setting );
	}

	@Override
	public void addComponents( GridPane pane, int row ) {
		String rbKey = setting.getBundleKey();
		String value = setting.getSettings().getString( key, null );

		label = new Label( product.getResourceBundle().getString( "settings", rbKey ) );

		field = new TextField();
		field.setText( value );
		field.setId( rbKey );

		button = new Button();
		button.setText( product.getResourceBundle().getString( "settings", "browse" ) );
		button.setOnAction( ( event ) -> getFile() );

		// Add the change handlers
		field.focusedProperty().addListener( this );
		field.setOnKeyPressed( this );

		// Set component state
		setDisable( setting.isDisable() );
		setVisible( setting.isVisible() );

		// Add the components
		GridPane buttonBox = new GridPane();
		GridPane.setHgrow( field, Priority.ALWAYS );
		buttonBox.setHgap( UiManager.PAD );
		buttonBox.addRow( 0, field, button );

		pane.addRow( row, label, buttonBox );
	}

	@Override
	public void setDisable( boolean disable ) {
		button.setDisable( disable );
		label.setDisable( disable );
		field.setDisable( disable );
	}

	@Override
	public void setVisible( boolean visible ) {
		button.setVisible( visible );
		label.setVisible( visible );
		field.setVisible( visible );
	}

	/**
	 * Setting listener
	 *
	 * @param event
	 */
	@Override
	public void handleEvent( SettingsEvent event ) {
		if( event.getType() == SettingsEvent.Type.UPDATED && key.equals( event.getKey() ) ) field.setText( event.getNewValue() );
	}

	/**
	 * Key listener
	 *
	 * @param event
	 */
	@Override
	public void handle( KeyEvent event ) {
		switch( event.getCode() ) {
			case ESCAPE: {
				field.setText( setting.getSettings().getString( key, null ) );
				break;
			}
			case ENTER: {
				setting.getSettings().set( key, field.getText() );
				break;
			}
		}
	}

	/**
	 * Focus listener
	 *
	 * @param observable
	 * @param oldValue
	 * @param newValue
	 */
	@Override
	public void changed( ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue ) {
		if( !newValue ) setting.getSettings().set( key, field.getText() );
	}

	private void getFile() {
		String fileName = field.getText();

		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle( product.getResourceBundle().getString( "settings", "select-file" ) );
		//String label = product.getResourceBundle().getString( "settings", "image-files" );
		//fileChooser.getExtensionFilters().addAll( new FileChooser.ExtensionFilter( label, "*.png", "*.jpg", "*.gif" ) );

		if( fileName != null ) {
			File file = new File( fileName );
			fileChooser.setInitialDirectory( file.getParentFile() );
			fileChooser.setInitialFileName( file.getName() );
		}

		File selectedFile = fileChooser.showOpenDialog( product.getProgram().getWorkspaceManager().getActiveWorkspace().getStage() );
		if( selectedFile != null ) {
			field.setText( selectedFile.toString() );
			setting.getSettings().set( key, field.getText() );
		}
	}

}
