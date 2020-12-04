package com.avereon.xenon.tool.settings.editor;

import com.avereon.settings.SettingsEvent;
import com.avereon.xenon.ProgramProduct;
import com.avereon.xenon.tool.settings.Setting;
import com.avereon.xenon.tool.settings.SettingEditor;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.GridPane;

import java.util.List;

public class CheckBoxSettingEditor extends SettingEditor implements ChangeListener<Boolean> {

	private CheckBox checkbox;

	private List<Node> nodes;

	public CheckBoxSettingEditor( ProgramProduct product, String bundleKey, Setting setting ) {
		super( product, bundleKey, setting );
	}

	@Override
	public void addComponents( GridPane pane, int row ) {
		String rbKey = setting.getBundleKey();
		boolean selected = setting.getSettings().get( key, Boolean.class, false );

		String label = product.rb().text( "settings", rbKey );

		checkbox = new CheckBox();
		checkbox.setSelected( selected );
		checkbox.setText( label );
		checkbox.setId( rbKey );

		nodes = List.of( checkbox );

		// Add the change handlers
		checkbox.selectedProperty().addListener( this );

		// Set component state
		setDisable( setting.isDisable() );
		setVisible( setting.isVisible() );

		// Add the components
		GridPane.setColumnSpan( checkbox, GridPane.REMAINING );
		pane.addRow( row, checkbox );
	}

	@Override
	public List<Node> getComponents() {
		return nodes;
	}

	// Checkbox listener
	@Override
	public void changed( ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue ) {
		setting.getSettings().set( key, checkbox.isSelected() );
	}

	// Setting node listener
	@Override
	public void handle( SettingsEvent event ) {
		if( event.getEventType() == SettingsEvent.CHANGED && key.equals( event.getKey() ) ) checkbox.setSelected( Boolean.parseBoolean( event.getNewValue().toString() ) );
	}

}
