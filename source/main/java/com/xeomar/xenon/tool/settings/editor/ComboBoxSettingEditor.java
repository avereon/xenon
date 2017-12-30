package com.xeomar.xenon.tool.settings.editor;

import com.xeomar.settings.SettingsEvent;
import com.xeomar.xenon.ProgramProduct;
import com.xeomar.xenon.tool.settings.Setting;
import com.xeomar.xenon.tool.settings.SettingEditor;
import com.xeomar.xenon.tool.settings.SettingOption;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

import java.util.List;

public class ComboBoxSettingEditor extends SettingEditor implements ChangeListener<SettingOption> {

	private Label label;

	private ComboBox<SettingOption> combobox;

	public ComboBoxSettingEditor( ProgramProduct product, Setting setting ) {
		super( product, setting );
	}

	@Override
	public void addComponents( GridPane pane, int row ) {
		String rbKey = setting.getBundleKey();
		String value = setting.getSettings().getString( key, null );

		label = new Label( product.getResourceBundle().getString( "settings", rbKey ) );

		List<SettingOption> options = setting.getOptions();
		combobox = new ComboBox<>();
		combobox.getItems().addAll( options );
		combobox.setMaxWidth( Double.MAX_VALUE );

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
		GridPane.setHgrow( combobox, Priority.ALWAYS );
		pane.addRow( row, label, combobox );
	}

	@Override
	public void setDisable( boolean disable ) {
		label.setDisable( disable );
		combobox.setDisable( disable );
	}

	@Override
	public void setVisible( boolean visible ) {
		label.setVisible( visible );
		combobox.setVisible( visible );
	}

	// Selection change listener
	@Override
	public void changed( ObservableValue<? extends SettingOption> observable, SettingOption oldValue, SettingOption newValue ) {
		setting.getSettings().set( setting.getKey(), newValue.getOptionValue() );
	}

	// Setting listener
	@Override
	public void handleEvent( SettingsEvent event ) {
		SettingOption option = setting.getOption( event.getNewValue().toString() );
		if( event.getType() == SettingsEvent.Type.UPDATED && key.equals( event.getKey() ) ) combobox.getSelectionModel().select( option );
	}

}
