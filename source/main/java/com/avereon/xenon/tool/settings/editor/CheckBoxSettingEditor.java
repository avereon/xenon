package com.avereon.xenon.tool.settings.editor;

import com.avereon.settings.SettingsEvent;
import com.avereon.xenon.ProgramProduct;
import com.avereon.xenon.tool.settings.SettingData;
import com.avereon.xenon.tool.settings.SettingEditor;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.GridPane;

import java.util.List;

public class CheckBoxSettingEditor extends SettingEditor {

	private CheckBox checkbox;

	private List<Node> nodes;

	public CheckBoxSettingEditor( ProgramProduct product, String bundleKey, SettingData setting ) {
		super( product, bundleKey, setting );
	}

	@Override
	public void addComponents( GridPane pane, int row ) {
		String rbKey = setting.getBundleKey();
		boolean selected = setting.getSettings().get( getKey(), Boolean.class, false );

		String label = product.rb().text( "settings", rbKey );

		checkbox = new CheckBox();
		checkbox.setSelected( selected );
		checkbox.setText( label );
		checkbox.setId( rbKey );

		nodes = List.of( checkbox );

		// Add the change handlers
		checkbox.selectedProperty().addListener( this::doCheckboxValueChanged );

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

	@Override
	protected void doSettingValueChanged( SettingsEvent event ) {
		if( event.getEventType() == SettingsEvent.CHANGED && getKey().equals( event.getKey() ) ) checkbox.setSelected( Boolean.parseBoolean( event.getNewValue().toString() ) );
	}

	private void doCheckboxValueChanged( ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue ) {
		setting.getSettings().set( getKey(), checkbox.isSelected() );
	}

}
