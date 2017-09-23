package com.xeomar.xenon.tool.settings.editor;

import com.xeomar.xenon.product.Product;
import com.xeomar.xenon.settings.SettingsEvent;
import com.xeomar.xenon.tool.settings.Setting;
import com.xeomar.xenon.tool.settings.SettingEditor;
import com.xeomar.xenon.util.Colors;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

public class ColorSettingEditor extends SettingEditor implements EventHandler<ActionEvent> {

	private Label label;

	private ColorPicker colorPicker;

	public ColorSettingEditor( Product product, Setting setting ) {
		super( product, setting );
	}

	@Override
	public void addComponents( GridPane pane, int row ) {
		String rbKey = setting.getBundleKey();
		String value = setting.getSettings().getString( key, "#000000ff" );

		label = new Label( product.getResourceBundle().getString( "settings", rbKey ) );

		colorPicker = new ColorPicker();
		colorPicker.setValue( Colors.web( value ) );
		colorPicker.setId( rbKey );
		colorPicker.setMaxWidth( Double.MAX_VALUE );

		// Add the event handlers
		colorPicker.setOnAction( this );

		// Set component state
		setDisable( setting.isDisable() );
		setVisible( setting.isVisible() );

		// Add the components
		GridPane.setHgrow( colorPicker, Priority.ALWAYS );
		pane.addRow( row, label, colorPicker );
	}

	@Override
	public void setDisable( boolean disable ) {
		label.setDisable( disable );
		colorPicker.setDisable( disable );
	}

	@Override
	public void setVisible( boolean visible ) {
		label.setVisible( visible );
		colorPicker.setVisible( visible );
	}

	@Override
	public void handle( ActionEvent event ) {
		setting.getSettings().set( setting.getKey(), Colors.web( colorPicker.getValue() ) );
	}

	@Override
	public void settingsEvent( SettingsEvent event ) {
		if( event.getType() == SettingsEvent.Type.UPDATED && key.equals( event.getKey() ) ) colorPicker.setValue( Colors.web( event.getNewValue() ) );
	}

}
