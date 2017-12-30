package com.xeomar.xenon.tool.settings.editor;

import com.xeomar.settings.SettingsEvent;
import com.xeomar.xenon.ProgramProduct;
import com.xeomar.xenon.tool.settings.Setting;
import com.xeomar.xenon.tool.settings.SettingEditor;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.GridPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CheckBoxSettingEditor extends SettingEditor implements ChangeListener<Boolean> {

	private static Logger log = LoggerFactory.getLogger( CheckBoxSettingEditor.class );

	private CheckBox checkbox;

	public CheckBoxSettingEditor( ProgramProduct product, Setting setting ) {
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
	public void setDisable( boolean disable ) {
		checkbox.setDisable( disable );
	}

	@Override
	public void setVisible( boolean visible ) {
		checkbox.setVisible( visible );
	}

	// Checkbox listener
	@Override
	public void changed( ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue ) {
		setting.getSettings().set( key, checkbox.isSelected() );
	}

	// Setting node listener
	@Override
	public void handleEvent( SettingsEvent event ) {
		if( event.getType() == SettingsEvent.Type.UPDATED && key.equals( event.getKey() ) ) checkbox.setSelected( Boolean.parseBoolean( event.getNewValue().toString() ) );
	}

}
