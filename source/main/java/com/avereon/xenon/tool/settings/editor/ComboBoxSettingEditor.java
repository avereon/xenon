package com.avereon.xenon.tool.settings.editor;

import com.avereon.product.Rb;
import com.avereon.settings.SettingsEvent;
import com.avereon.xenon.ProgramProduct;
import com.avereon.xenon.tool.settings.SettingData;
import com.avereon.xenon.tool.settings.SettingEditor;
import com.avereon.xenon.tool.settings.SettingOption;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;

import java.util.List;

public class ComboBoxSettingEditor extends SettingEditor {

	private ComboBox<SettingOption> combobox;

	private List<Node> nodes;

	public ComboBoxSettingEditor( ProgramProduct product, String bundleKey, SettingData setting ) {
		super( product, bundleKey, setting );
	}

	@Override
	public void addComponents( GridPane pane, int row ) {
		String rbKey = setting.getBundleKey();
		String value = setting.getSettings().get( getKey() );

		Label label = new Label( Rb.text( getProduct(), getBundleKey(), rbKey ) );
		label.setMinWidth( Region.USE_PREF_SIZE );

		List<SettingOption> options = setting.getOptions();
		combobox = new ComboBox<>();
		combobox.getItems().addAll( options );
		combobox.setMaxWidth( Double.MAX_VALUE );

		nodes = List.of( label, combobox );

		SettingOption selected = setting.getOption( value );
		if( selected == null ) {
			combobox.getSelectionModel().clearSelection();
		} else {
			combobox.getSelectionModel().select( selected );
		}

		// Add the change handlers
		combobox.valueProperty().addListener( this::doComboBoxValueChanged );

		// Set component state
		setDisable( setting.isDisable() );
		setVisible( setting.isVisible() );

		// Add the components
		pane.addRow( row, label, combobox );
	}

	@Override
	public List<Node> getComponents() {
		return nodes;
	}

	@Override
	protected void doSettingValueChanged( SettingsEvent event ) {
		if( event.getEventType() == SettingsEvent.CHANGED && getKey().equals( event.getKey() ) ) {
			Object newValue = event.getNewValue();
			SettingOption option = setting.getOption( newValue == null ? null : newValue.toString() );
			combobox.getSelectionModel().select( option );
		}
	}

	private void doComboBoxValueChanged( ObservableValue<? extends SettingOption> observable, SettingOption oldValue, SettingOption newValue ) {
		setting.getSettings().set( setting.getKey(), newValue == null ? null : newValue.getOptionValue() );
	}

}
