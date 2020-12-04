package com.avereon.xenon.tool.settings.editor;

import com.avereon.settings.SettingsEvent;
import com.avereon.xenon.ProgramProduct;
import com.avereon.xenon.tool.settings.Setting;
import com.avereon.xenon.tool.settings.SettingEditor;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

import java.util.List;

public abstract class InfoSettingEditor extends SettingEditor implements EventHandler<KeyEvent>, ChangeListener<Boolean> {

	protected enum Type {
		AREA,
		FIELD
	}

	private final Type type;

	private TextInputControl text;

	private List<Node> nodes;

	public InfoSettingEditor( ProgramProduct product, String bundleKey, Setting setting, Type type ) {
		super( product, bundleKey, setting );
		this.type = type;
	}

	@Override
	public void addComponents( GridPane pane, int row ) {
		String rbKey = setting.getBundleKey();

		if( type == Type.AREA ) {
			text = new TextArea();
			text.getStyleClass().add( "settings-infoarea" );
		} else {
			text = new TextField();
			text.getStyleClass().add( "settings-infoline" );
		}
		text.setText( product.rb().text( "settings", rbKey ) );
		text.setEditable( false );
		text.setId( rbKey );

		nodes = List.of( text );

		// Set component state
		setDisable( setting.isDisable() );
		setVisible( setting.isVisible() );

		// Add the components
		GridPane.setHgrow( text, Priority.ALWAYS );
		GridPane.setColumnSpan( text, GridPane.REMAINING );
		pane.addRow( row, text );
	}

	@Override
	public List<Node> getComponents() {
		return nodes;
	}

	@Override
	public void handle( SettingsEvent event ) {
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
