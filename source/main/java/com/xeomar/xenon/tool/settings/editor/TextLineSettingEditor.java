package com.xeomar.xenon.tool.settings.editor;

import com.xeomar.xenon.product.Product;
import com.xeomar.xenon.settings.SettingsEvent;
import com.xeomar.xenon.tool.settings.Setting;
import com.xeomar.xenon.tool.settings.SettingEditor;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

public class TextLineSettingEditor extends SettingEditor implements EventHandler<KeyEvent>, ChangeListener<Boolean> {

	protected enum Type {
		AREA,
		FIELD,
		PASSWORD
	}

	private Type type;

	private Label label;

	private TextInputControl text;

	public TextLineSettingEditor( Product product, Setting setting ) {
		this( product, setting, Type.FIELD );
	}

	TextLineSettingEditor( Product product, Setting setting, Type type ) {
		super( product, setting );
		this.type = type;
	}

	@Override
	public void addComponents( GridPane pane, int row ) {
		String rbKey = setting.getBundleKey();
		String value = setting.getSettings().getString( key, null );

		label = new Label( product.getResourceBundle().getString( "settings", rbKey ) );

		switch( type ) {
			case AREA : {
				text = new TextArea(  );
				break;
			}
			case PASSWORD : {
				text = new PasswordField(  );
				break;
			}
			default : {
				text = new TextField(  );
				break;
			}
		}
		text.setText( value );
		text.setId( rbKey );

		// Add the change handlers
		text.focusedProperty().addListener( this );
		text.setOnKeyPressed( this );

		// Set component state
		setDisable( setting.isDisable() );
		setVisible( setting.isVisible() );

		// Add the components
		GridPane.setHgrow( text, Priority.ALWAYS );
		pane.addRow( row, label, text );
	}

	@Override
	public void setDisable( boolean disable ) {
		label.setDisable( disable );
		text.setDisable( disable );
	}

	@Override
	public void setVisible( boolean visible ) {
		label.setVisible( visible );
		text.setVisible( visible );
	}

	@Override
	public void settingsEvent( SettingsEvent event ) {
		// If the values are the same, don't set the text because it moves the cursor
		if( event.getNewValue().equals( text.getText() ) ) return;
		if( event.getType() == SettingsEvent.Type.UPDATED && key.equals( event.getKey() ) ) text.setText( event.getNewValue() );
	}

	@Override
	public void handle( KeyEvent event ) {
		switch( event.getCode() ) {
			case ESCAPE: {
				text.setText( setting.getSettings().getString( key, null ) );
				break;
			}
			case ENTER: {
				setting.getSettings().set( key, text.getText() );
				break;
			}
		}
	}

	/* Focus listener */
	@Override
	public void changed( ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue ) {
		if( !newValue ) setting.getSettings().set( key, text.getText() );
	}

}
