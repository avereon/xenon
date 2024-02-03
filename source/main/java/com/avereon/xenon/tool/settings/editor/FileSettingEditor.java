package com.avereon.xenon.tool.settings.editor;

import com.avereon.product.Rb;
import com.avereon.settings.SettingsEvent;
import com.avereon.util.FileUtil;
import com.avereon.xenon.XenonProgramProduct;
import com.avereon.xenon.UiFactory;
import com.avereon.xenon.tool.settings.SettingData;
import com.avereon.xenon.tool.settings.SettingEditor;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.List;

public class FileSettingEditor extends SettingEditor {

	private Label label;

	private TextField field;

	private Button button;

	private List<Node> nodes;

	public FileSettingEditor( XenonProgramProduct product, String rbKey, SettingData setting ) {
		super( product, rbKey, setting );
	}

	@Override
	public void addComponents( GridPane pane, int row ) {
		String rbKey = setting.getRbKey();
		String value = setting.getSettings().get( getKey() );

		label = new Label( Rb.text( getProduct(), getRbKey(), rbKey ) );
		label.setMinWidth( Region.USE_PREF_SIZE );

		field = new TextField();
		field.setText( value );
		field.setId( rbKey );

		button = new Button();
		button.setText( Rb.text( getProduct(), getRbKey(), "browse" ) );
		button.setOnAction( ( event ) -> getFile() );

		nodes = List.of( label, field, button );

		// Add the change handlers
		field.focusedProperty().addListener( this::doFocusChanged );
		field.setOnKeyPressed( this::handleKeyEvent );

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
	public List<Node> getComponents() {
		return nodes;
	}

	/**
	 * Setting listener
	 *
	 * @param event The setting event
	 */
	@Override
	protected void doSettingValueChanged( SettingsEvent event ) {
		if( event.getEventType() == SettingsEvent.CHANGED && getKey().equals( event.getKey() ) ) field.setText( event.getNewValue().toString() );
	}

	/**
	 * Key listener
	 *
	 * @param event The key event
	 */
	private void handleKeyEvent( KeyEvent event ) {
		switch( event.getCode() ) {
			case ESCAPE -> field.setText( setting.getSettings().get( getKey() ) );
			case ENTER -> setting.getSettings().set( getKey(), field.getText() );
		}
	}

	/**
	 * Focus listener
	 *
	 * @param observable The observable value
	 * @param oldValue The old value
	 * @param newValue The new value
	 */
	private void doFocusChanged( ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue ) {
		if( !newValue ) setting.getSettings().set( getKey(), field.getText() );
	}

	private void getFile() {
		String fileName = field.getText();

		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle( Rb.text( getProduct(), getRbKey(), "select-file" ) );

		if( fileName != null ) {
			File file = new File( fileName );
			boolean exists = file.exists();
			fileChooser.setInitialDirectory( FileUtil.findValidParent( file ).toFile() );
			fileChooser.setInitialFileName( exists ? file.getName() : "" );
		}

		File selectedFile = fileChooser.showOpenDialog( product.getProgram().getWorkspaceManager().getActiveStage() );
		if( selectedFile != null ) {
			field.setText( selectedFile.toString() );
			setting.getSettings().set( getKey(), field.getText() );
		}
	}

}
