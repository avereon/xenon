package com.avereon.xenon.tool.settings.editor;

import com.avereon.settings.SettingsEvent;
import com.avereon.util.TextUtil;
import com.avereon.xenon.ProgramProduct;
import com.avereon.xenon.tool.settings.Setting;
import com.avereon.xenon.tool.settings.SettingEditor;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;

import java.util.List;
import java.util.Objects;

public abstract class TextSettingEditor extends SettingEditor implements EventHandler<KeyEvent>, ChangeListener<Boolean> {

	protected enum Type {
		AREA,
		FIELD,
		PASSWORD
	}

	private final Type type;

	private Label label;

	private TextInputControl text;

	private List<Node> nodes;

	TextSettingEditor( ProgramProduct product, String bundleKey, Setting setting, Type type ) {
		super( product, bundleKey, setting );
		this.type = type;
	}

	@Override
	public void addComponents( GridPane pane, int row ) {
		String rbKey = setting.getBundleKey();
		String value = setting.getSettings().get( key );

		label = new Label( product.rb().text( getBundleKey(), rbKey ) );
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
		text.focusedProperty().addListener( this );
		text.setOnKeyPressed( this );

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
	public void handle( SettingsEvent event ) {
		if( event.getEventType() == SettingsEvent.CHANGED && key.equals( event.getKey() ) ) {
			Object eventNewValue = event.getNewValue();
			String newValue = eventNewValue == null ? null : String.valueOf( eventNewValue );

			// If the values are the same, don't set the text because it moves the cursor
			if( Objects.equals( text.getText(), newValue ) ) return;
			text.setText( newValue );
		}
	}

	@Override
	public void handle( KeyEvent event ) {
		switch( event.getCode() ) {
			case ESCAPE -> text.setText( setting.getSettings().get( key ) );
			case ENTER -> updateSetting( key, text.getText() );
		}
	}

	/* Focus listener */
	@Override
	public void changed( ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue ) {
		if( !newValue ) updateSetting( key, text.getText() );
	}

	private void updateSetting( String key, String value ) {
		setting.getSettings().set( key, TextUtil.isEmpty( value ) ? null : value.trim() );
	}

}
