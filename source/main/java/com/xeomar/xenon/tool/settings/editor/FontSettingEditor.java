package com.xeomar.xenon.tool.settings.editor;

import com.xeomar.settings.SettingsEvent;
import com.xeomar.xenon.ProgramProduct;
import com.xeomar.xenon.tool.settings.Setting;
import com.xeomar.xenon.tool.settings.SettingEditor;
import com.xeomar.xenon.util.FontUtil;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import org.controlsfx.dialog.FontSelectorDialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class FontSettingEditor extends SettingEditor {

	private static Logger log = LoggerFactory.getLogger( FontSettingEditor.class );

	private Label label;

	private Button button;

	public FontSettingEditor( ProgramProduct product, Setting setting ) {
		super( product, setting );
	}

	@Override
	public void addComponents( GridPane pane, int row ) {
		String rbKey = setting.getBundleKey();
		String value = setting.getSettings().getString( key, "SansSerif|12" );

		label = new Label( product.getResourceBundle().getString( "settings", rbKey ) );

		button = new Button();
		button.setMaxWidth( Double.MAX_VALUE );
		updateFont( value );

		// Set component state
		setDisable( setting.isDisable() );
		setVisible( setting.isVisible() );

		// Add the components
		pane.addRow( row, label, button );
	}

	@Override
	public void setDisable( boolean disable ) {
		label.setDisable( disable );
		button.setDisable( disable );
	}

	@Override
	public void setVisible( boolean visible ) {
		label.setVisible( visible );
		button.setVisible( visible );
	}

	@Override
	public void handleEvent( SettingsEvent event ) {
		if( event.getType() == SettingsEvent.Type.UPDATED && key.equals( event.getKey() ) ) updateFont( event.getNewValue().toString() );
	}

	private void updateFont( String value ) {
		Font font = FontUtil.decode( value );
		log.debug( "Setting font updated: " + font );
		button.setText( font.getName() + " " + font.getSize() );
		button.setFont( Font.font( font.getFamily(), FontUtil.getFontWeight( font.getStyle() ), FontUtil.getFontPosture( font.getStyle() ), -1 ) );
		button.setOnAction( ( event ) -> {
			FontSelectorDialog dialog = new FontSelectorDialog( font );
			Optional<Font> optional = dialog.showAndWait();
			optional.ifPresent( font1 -> {
				log.debug( "Setting font selected: " + font1 );
				setting.getSettings().set( setting.getKey(), FontUtil.encode( font1 ) );
			} );
		} );
	}

}
