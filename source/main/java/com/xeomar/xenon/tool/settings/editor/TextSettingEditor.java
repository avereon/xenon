package com.xeomar.xenon.tool.settings.editor;

import com.xeomar.xenon.product.Product;
import com.xeomar.xenon.settings.SettingsEvent;
import com.xeomar.xenon.tool.settings.Setting;
import com.xeomar.xenon.tool.settings.SettingEditor;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

public class TextSettingEditor extends SettingEditor implements EventHandler<KeyEvent>, ChangeListener<Boolean> {

	private Label label;

	private TextField field;

	private boolean password;

	public TextSettingEditor( Product product, Setting setting ) {
		this( product, setting, false );
	}

	TextSettingEditor( Product product, Setting setting, boolean password){
		super( product, setting );
		this.password = password;
	}

	@Override
	public void addComponents( GridPane pane, int row ) {
		String rbKey = setting.getBundleKey();
		String value = setting.getSettings().getString( key, null );

		label = new Label( product.getResourceBundle().getString( "settings", rbKey ) );

		field = password ? new PasswordField() : new TextField();
		field.setText( value );
		field.setId( rbKey );

		// Add the change handlers
		field.focusedProperty().addListener( this );
		field.setOnKeyPressed( this );

		// Add the components.
		GridPane.setHgrow( field, Priority.ALWAYS );
		pane.addRow( row, label, field );
	}

	@Override
	public void setDisable( boolean disable ) {
		label.setDisable( disable );
		field.setDisable( disable );
	}

	@Override
	public void setVisible( boolean visible ) {
		label.setVisible( visible );
		field.setVisible( visible );
	}

	@Override
	public void settingsEvent( SettingsEvent event ) {
		if( event.getType() == SettingsEvent.Type.UPDATED && key.equals( event.getKey() ) ) field.setText( event.getNewValue() );
	}

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

	/* Focus listener */
	@Override
	public void changed( ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue ) {
		if( !newValue ) setting.getSettings().set( key, field.getText() );
	}

}
