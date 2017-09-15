package com.xeomar.xenon.tool.settings.editor;

import com.xeomar.xenon.product.Product;
import com.xeomar.xenon.settings.SettingsEvent;
import com.xeomar.xenon.tool.settings.Setting;
import com.xeomar.xenon.tool.settings.SettingEditor;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

public class InfoLineSettingEditor extends SettingEditor implements EventHandler<KeyEvent>, ChangeListener<Boolean> {

	protected enum Type {
		AREA,
		FIELD
	}

	private Type type;

	private TextInputControl text;

	public InfoLineSettingEditor( Product product, Setting setting ) {
		this( product, setting, Type.FIELD );
	}

	public InfoLineSettingEditor( Product product, Setting setting, Type type ) {
		super( product, setting );
		this.type = type;
	}

	@Override
	public void addComponents( GridPane pane, int row ) {
		String rbKey = setting.getBundleKey();

		switch( type ) {
			case AREA: {
				text = new TextArea();
				break;
			}
			default: {
				text = new TextField();
				break;
			}
		}
		text.setEditable( false );
		text.setText( product.getResourceBundle().getString( "settings", rbKey ) );
		text.setId( rbKey );

		// Set component state
		setDisable( setting.isDisable() );
		setVisible( setting.isVisible() );

		// Add the components
		GridPane.setHgrow( text, Priority.ALWAYS );
		GridPane.setColumnSpan( text, GridPane.REMAINING );
		pane.addRow( row, text );
	}

	@Override
	public void setDisable( boolean disable ) {
		text.setDisable( disable );
	}

	@Override
	public void setVisible( boolean visible ) {
		text.setVisible( visible );
	}

	@Override
	public void settingsEvent( SettingsEvent event ) {
		// No need to change the editor
	}

	@Override
	public void handle( KeyEvent event ) {
		// No need to change the editor
	}

	/**
	 * Focus listener
	 */
	@Override
	public void changed( ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue ) {
		// No need to change the editor
	}

}
