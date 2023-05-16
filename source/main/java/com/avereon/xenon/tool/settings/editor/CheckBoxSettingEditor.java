package com.avereon.xenon.tool.settings.editor;

import com.avereon.product.Rb;
import com.avereon.settings.SettingsEvent;
import com.avereon.xenon.XenonProgramProduct;
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

	public CheckBoxSettingEditor( XenonProgramProduct product, String rbKey, SettingData setting ) {
		super( product, rbKey, setting );
	}

	@Override
	public void addComponents( GridPane pane, int row ) {
		String rbKey = setting.getRbKey();
		boolean selected = setting.getSettings().get( getKey(), Boolean.class, false );

		String label = Rb.text( getProduct(), getRbKey(), rbKey );

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
