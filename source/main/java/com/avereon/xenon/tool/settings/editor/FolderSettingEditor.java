package com.avereon.xenon.tool.settings.editor;

import com.avereon.settings.SettingsEvent;
import com.avereon.util.FileUtil;
import com.avereon.xenon.ProgramProduct;
import com.avereon.xenon.UiFactory;
import com.avereon.xenon.tool.settings.Setting;
import com.avereon.xenon.tool.settings.SettingEditor;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.DirectoryChooser;

import java.io.File;

public class FolderSettingEditor extends SettingEditor implements EventHandler<KeyEvent>, ChangeListener<Boolean> {

	private Label label;

	private TextField field;

	private Button button;

	public FolderSettingEditor( ProgramProduct product, Setting setting ) {
		super( product, setting );
	}

	@Override
	public void addComponents( GridPane pane, int row ) {
		String rbKey = setting.getBundleKey();
		String value = setting.getSettings().get( key );

		label = new Label( product.rb().text( "settings", rbKey ) );

		field = new TextField();
		field.setText( value );
		field.setId( rbKey );

		button = new Button();
		button.setText( product.rb().text( "settings", "browse" ) );
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
		buttonBox.setHgap( UiFactory.PAD );
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
	public void handle( SettingsEvent event ) {
		if( event.getEventType() == SettingsEvent.CHANGED && key.equals( event.getKey() ) ) field.setText( event.getNewValue().toString() );
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
				field.setText( setting.getSettings().get( key ) );
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

		DirectoryChooser chooser = new DirectoryChooser();
		chooser.setTitle( product.rb().text( "settings", "select-folder" ) );

		if( fileName != null ) chooser.setInitialDirectory( FileUtil.findValidParent( new File( fileName ) ) );
		File selectedFile = chooser.showDialog( product.getProgram().getWorkspaceManager().getActiveStage() );
		if( selectedFile != null ) {
			field.setText( selectedFile.toString() );
			setting.getSettings().set( key, field.getText() );
		}
	}

}