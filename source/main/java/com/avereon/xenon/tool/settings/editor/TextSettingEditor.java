package com.avereon.xenon.tool.settings.editor;

import com.avereon.product.Rb;
import com.avereon.settings.SettingsEvent;
import com.avereon.util.TextUtil;
import com.avereon.xenon.ProgramProduct;
import com.avereon.xenon.tool.settings.SettingData;
import com.avereon.xenon.tool.settings.SettingEditor;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;

import java.util.List;
import java.util.Objects;

public abstract class TextSettingEditor extends SettingEditor {

	protected enum Type {
		AREA,
		FIELD,
		PASSWORD
	}

	private final Type type;

	private Label label;

	private TextInputControl text;

	private List<Node> nodes;

	TextSettingEditor( ProgramProduct product, String bundleKey, SettingData setting, Type type ) {
		super( product, bundleKey, setting );
		this.type = type;
	}

	@Override
	public void addComponents( GridPane pane, int row ) {
		String rbKey = setting.getBundleKey();
		String value = setting.getSettings().get( getKey() );

		label = new Label( Rb.text( getProduct(), getBundleKey(), rbKey ) );
		label.setMinWidth( Region.USE_PREF_SIZE );

		switch( type ) {
			case AREA -> text = new TextArea();
			case PASSWORD -> text = new PasswordField();
			default -> text = new TextField();
		}
		text.setText( value );
		text.setId( rbKey );

		nodes = List.of( label, text );

		// Add the change handlers
		text.focusedProperty().addListener( this::doFocusChanged );
		text.setOnKeyPressed( this::handleKeyEvent );

		// Set component state
		setDisable( setting.isDisable() );
		setVisible( setting.isVisible() );

		// Add the components
		pane.addRow( row, label, text );
	}

	public List<Node> getComponents() {
		return nodes;
	}

	@Override
	protected void doSettingValueChanged( SettingsEvent event ) {
		if( event.getEventType() == SettingsEvent.CHANGED && getKey().equals( event.getKey() ) ) {
			Object eventNewValue = event.getNewValue();
			String newValue = eventNewValue == null ? null : String.valueOf( eventNewValue );

			// If the values are the same, don't set the text because it moves the cursor
			if( Objects.equals( text.getText(), newValue ) ) return;
			text.setText( newValue );
		}
	}

	private void handleKeyEvent( KeyEvent event ) {
		switch( event.getCode() ) {
			case ESCAPE -> text.setText( setting.getSettings().get( getKey() ) );
			case ENTER -> updateSetting( getKey(), text.getText() );
		}
	}

	private void doFocusChanged( ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue ) {
		if( !newValue ) updateSetting( getKey(), text.getText() );
	}

	private void updateSetting( String key, String value ) {
		setting.getSettings().set( key, TextUtil.isEmpty( value ) ? null : value.trim() );
	}

}
