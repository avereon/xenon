package com.avereon.xenon.tool.settings.editor;

import com.avereon.settings.SettingsEvent;
import com.avereon.util.LogUtil;
import com.avereon.xenon.ProgramProduct;
import com.avereon.xenon.tool.settings.Setting;
import com.avereon.xenon.tool.settings.SettingEditor;
import com.avereon.xenon.util.FontUtil;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import org.slf4j.Logger;

import java.lang.invoke.MethodHandles;

public class FontSettingEditor extends SettingEditor {

	private static final Logger log = LogUtil.get( MethodHandles.lookup().lookupClass() );

	private Label label;

	private Button button;

	public FontSettingEditor( ProgramProduct product, Setting setting ) {
		super( product, setting );
	}

	@Override
	public void addComponents( GridPane pane, int row ) {
		String rbKey = setting.getBundleKey();
		String value = setting.getSettings().get( key, "SansSerif|12" );

		label = new Label( product.rb().text( "settings", rbKey ) );

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
		if( event.getType() == SettingsEvent.Type.CHANGED && key.equals( event.getKey() ) ) updateFont( event.getNewValue().toString() );
	}

	private void updateFont( String value ) {
		Font font = FontUtil.decode( value );
		log.debug( "Setting font updated: " + font );
		button.setText( font.getName() + " " + font.getSize() );
		button.setFont( Font.font( font.getFamily(), FontUtil.getFontWeight( font.getStyle() ), FontUtil.getFontPosture( font.getStyle() ), -1 ) );
//		button.setOnAction( ( event ) -> {
//			FontSelectorDialog dialog = new FontSelectorDialog( font );
//			Optional<Font> optional = dialog.showAndWait();
//			optional.ifPresent( font1 -> {
//				log.debug( "Setting font selected: " + font1 );
//				setting.getSettings().set( setting.getKey(), FontUtil.encode( font1 ) );
//			} );
//		} );
	}

}
