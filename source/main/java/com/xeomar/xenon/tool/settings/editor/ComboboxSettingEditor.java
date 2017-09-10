package com.xeomar.xenon.tool.settings.editor;

import com.xeomar.xenon.product.Product;
import com.xeomar.xenon.settings.SettingsEvent;
import com.xeomar.xenon.tool.settings.Setting;
import com.xeomar.xenon.tool.settings.SettingEditor;
import com.xeomar.xenon.tool.settings.SettingOption;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

import java.util.List;

public class ComboboxSettingEditor extends SettingEditor {

	private Label label;

	private ComboBox<SettingOption> combobox;

	public ComboboxSettingEditor( Product product, Setting setting ) {
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

		SettingOption selected = setting.getOption( value );
		if( selected == null ) {
			combobox.getSelectionModel().clearSelection();
		} else {
			combobox.getSelectionModel().select( selected );
		}

		// Add the change handlers


		// Add the components.
		GridPane.setHgrow( combobox, Priority.ALWAYS );
		pane.addRow( row, label, combobox );

	}

	@Override
	public void setEnabled( boolean enabled ) {

	}

	@Override
	public void setVisible( boolean visible ) {

	}

	// Setting listener
	@Override
	public void event( SettingsEvent event ) {

	}

}
