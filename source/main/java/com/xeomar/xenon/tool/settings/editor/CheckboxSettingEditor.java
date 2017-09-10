package com.xeomar.xenon.tool.settings.editor;

import com.xeomar.xenon.product.Product;
import com.xeomar.xenon.settings.SettingsEvent;
import com.xeomar.xenon.tool.settings.Setting;
import com.xeomar.xenon.tool.settings.SettingEditor;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.GridPane;

public class CheckboxSettingEditor extends SettingEditor implements ChangeListener<Boolean> {

	private CheckBox checkbox;

	public CheckboxSettingEditor( Product product, Setting setting ) {
		super( product, setting );
	}

	@Override
	public void addComponents( GridPane pane, int row ) {
		String rbKey = setting.getBundleKey();
		boolean selected = setting.getSettings().getBoolean( key, false );

		String label = product.getResourceBundle().getString( "settings", rbKey );

		checkbox = new CheckBox();
		checkbox.setSelected( selected );
		checkbox.setText( label );
		checkbox.setId( rbKey );

		checkbox.selectedProperty().addListener( this );

		GridPane.setColumnSpan( checkbox, GridPane.REMAINING );
		pane.addRow( row, checkbox );
	}

	@Override
	public void setEnabled( boolean enabled ) {
		checkbox.setDisable( !enabled );
	}

	@Override
	public void setVisible( boolean visible ) {
		checkbox.setVisible( visible );
	}

	// Selected listener
	@Override
	public void changed( ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue ) {
		setting.getSettings().set( key, checkbox.isSelected() );
	}

	// Setting listener
	@Override
	public void event( SettingsEvent event ) {
		if( key.equals( event.getKey() ) ) checkbox.setSelected( Boolean.parseBoolean( event.getNewValue() ) );
	}

}
