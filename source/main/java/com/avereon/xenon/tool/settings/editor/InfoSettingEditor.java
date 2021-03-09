package com.avereon.xenon.tool.settings.editor;

import com.avereon.product.Rb;
import com.avereon.settings.SettingsEvent;
import com.avereon.xenon.ProgramProduct;
import com.avereon.xenon.tool.settings.SettingData;
import com.avereon.xenon.tool.settings.SettingEditor;
import javafx.scene.Node;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

import java.util.List;

public abstract class InfoSettingEditor extends SettingEditor {

	protected enum Type {
		AREA,
		FIELD
	}

	private final Type type;

	private List<Node> nodes;

	public InfoSettingEditor( ProgramProduct product, String bundleKey, SettingData setting, Type type ) {
		super( product, bundleKey, setting );
		this.type = type;
	}

	@Override
	public void addComponents( GridPane pane, int row ) {
		String rbKey = setting.getBundleKey();

		TextInputControl text;
		if( type == Type.AREA ) {
			text = new TextArea();
			text.getStyleClass().add( "settings-infoarea" );
		} else {
			text = new TextField();
			text.getStyleClass().add( "settings-infoline" );
		}
		text.setText( Rb.text( getProduct(), "settings", rbKey ) );
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
	protected void doSettingValueChanged( SettingsEvent event ) {
		// No need to change the editor
	}

}
