package com.avereon.xenon.tool.settings.editor;

import com.avereon.settings.SettingsEvent;
import com.avereon.xenon.ProgramProduct;
import com.avereon.xenon.tool.settings.Setting;
import com.avereon.xenon.tool.settings.SettingEditor;
import com.avereon.xenon.tool.settings.SettingOption;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;

import java.util.List;

public class ComboBoxSettingEditor extends SettingEditor implements ChangeListener<SettingOption> {

	private Label label;

	private ComboBox<SettingOption> combobox;

	private List<Node> nodes;

	public ComboBoxSettingEditor( ProgramProduct product, String bundleKey, Setting setting ) {
		super( product, bundleKey, setting );
	}

	@Override
	public void addComponents( GridPane pane, int row ) {
		String rbKey = setting.getBundleKey();
		String value = setting.getSettings().get( key );

		label = new Label( product.rb().text( getBundleKey(), rbKey ) );
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
		combobox.valueProperty().addListener( this );

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

	// Selection change listener
	@Override
	public void changed( ObservableValue<? extends SettingOption> observable, SettingOption oldValue, SettingOption newValue ) {
		setting.getSettings().set( setting.getRbKey(), newValue.getOptionValue() );
	}

	// Setting listener
	@Override
	public void handle( SettingsEvent event ) {
		if( event.getEventType() == SettingsEvent.CHANGED && key.equals( event.getKey() ) ) {
			Object newValue = event.getNewValue();
			SettingOption option = setting.getOption( newValue == null ? null : newValue.toString() );
			combobox.getSelectionModel().select( option );
		}
	}

}
