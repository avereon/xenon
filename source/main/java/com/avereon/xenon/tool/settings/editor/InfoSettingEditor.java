package com.avereon.xenon.tool.settings.editor;

import com.avereon.product.Rb;
import com.avereon.settings.SettingsEvent;
import com.avereon.xenon.ProgramProduct;
import com.avereon.xenon.tool.settings.SettingData;
import com.avereon.xenon.tool.settings.SettingEditor;
import javafx.scene.Node;
import javafx.scene.control.Label;
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

	public InfoSettingEditor( ProgramProduct product, String rbKey, SettingData setting, Type type ) {
		super( product, rbKey, setting );
		this.type = type;
	}

	@Override
	public void addComponents( GridPane pane, int row ) {
		String rbKey = setting.getRbKey();

		Label text;
		text = new Label();
		text.getStyleClass().add( type == Type.AREA ? "settings-infoarea" : "settings-infoline" );
		text.setText( Rb.text( getProduct(), getRbKey(), rbKey ) );
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
