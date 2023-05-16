package com.avereon.xenon.tool.settings.editor;

import com.avereon.product.Rb;
import com.avereon.settings.SettingsEvent;
import com.avereon.xenon.XenonProgramProduct;
import com.avereon.xenon.tool.settings.SettingData;
import com.avereon.xenon.tool.settings.SettingEditor;
import com.avereon.xenon.ui.PaintPicker;
import com.avereon.xenon.ui.PaintPickerPane;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import lombok.CustomLog;

import java.util.List;
import java.util.stream.Collectors;

@CustomLog
public class PaintSettingEditor extends SettingEditor {

	private final Label label;

	private final PaintPicker paintPicker;

	private List<Node> nodes;

	public PaintSettingEditor( XenonProgramProduct product, String rbKey, SettingData setting ) {
		super( product, rbKey, setting );
		label = new Label();
		paintPicker = new PaintPicker();
		if( !setting.getOptions().isEmpty() ) paintPicker.getOptions().clear();
		paintPicker.getOptions().addAll( setting.getOptions().stream().map( o -> switch( o.getKey() ) {
			case "solid" -> PaintPickerPane.PaintMode.SOLID;
			case "linear" -> PaintPickerPane.PaintMode.LINEAR;
			case "radial" -> PaintPickerPane.PaintMode.RADIAL;
			case "none" -> PaintPickerPane.PaintMode.NONE;
			default -> new PaintPickerPane.PaintMode( o.getKey(), o.getName() );
		} ).collect( Collectors.toList() ) );
	}

	@Override
	public void addComponents( GridPane pane, int row ) {
		String rbKey = setting.getRbKey();
		String value = setting.getSettings().get( getKey() );

		label.setText( Rb.text( getProduct(), getRbKey(), rbKey ) );
		label.setMinWidth( Region.USE_PREF_SIZE );

		paintPicker.setId( rbKey );
		paintPicker.setPaintAsString( value );
		paintPicker.setMaxWidth( Double.MAX_VALUE );
		HBox.setHgrow( paintPicker, Priority.ALWAYS );

		nodes = List.of( label, paintPicker );

		// Add the event handlers
		paintPicker.paintAsStringProperty().addListener( this::doPickerValueChanged );

		// Set component state
		setDisable( setting.isDisable() );
		setVisible( setting.isVisible() );

		// Add the components
		pane.addRow( row, label, new HBox( paintPicker ) );
	}

	@Override
	public List<Node> getComponents() {
		return nodes;
	}

	@Override
	protected void doSettingValueChanged( SettingsEvent event ) {
		if( event.getEventType() != SettingsEvent.CHANGED || !getKey().equals( event.getKey() ) ) return;

		Object value = event.getNewValue();
		String paint = value == null ? null : String.valueOf( value );
		paintPicker.setPaintAsString( paint );
	}

	private void doPickerValueChanged( ObservableValue<? extends String> property, String oldValue, String newValue ) {
		setting.getSettings().set( setting.getKey(), newValue );
	}

}
