package com.xeomar.xenon.tool.settings;

import com.xeomar.xenon.product.Product;
import com.xeomar.xenon.settings.SettingsEvent;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

public class TextSettingEditor extends SettingEditor {

	private Label label;

	private TextField field;

	private boolean password;

	public TextSettingEditor( Product product, Setting setting ) {
		super( product, setting );
		this.password = password;
	}

	@Override
	public void addComponents( GridPane pane, int row ) {
		String rbKey = setting.getBundleKey();
		String value = setting.getSettings().getString( key, null );

		String title = product.getResourceBundle().getString( "settings", rbKey );

		label = new Label( title );

		field = password ? new PasswordField() : new TextField();
		field.setText( value );
		field.setId( rbKey );

		// TODO		// Add the change handlers
		//		field.addFocusListener( this );
		//		field.addKeyListener( this );

		// Add the components.
		GridPane.setHgrow( field, Priority.ALWAYS );
		pane.addRow( row, label, field );
	}

	@Override
	public void setEnabled( boolean enabled ) {
		label.setDisable( !enabled );
		field.setDisable( !enabled );
	}

	@Override
	public void setVisible( boolean visible ) {
		label.setVisible( visible );
		field.setVisible( visible );
	}

	@Override
	public void event( SettingsEvent event ) {
		if( event.getType() == SettingsEvent.Type.UPDATED && key.equals( event.getKey() ) ) field.setText( event.getNewValue() );
	}

}
