package com.avereon.xenon.tool.settings.editor;

import com.avereon.product.Rb;
import com.avereon.settings.SettingsEvent;
import com.avereon.xenon.ProgramProduct;
import com.avereon.xenon.tool.settings.SettingData;
import com.avereon.xenon.tool.settings.SettingEditor;
import com.avereon.zarra.font.FontUtil;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.scene.text.Font;
import lombok.CustomLog;

import java.util.List;

@CustomLog
public class FontSettingEditor extends SettingEditor {

	private Label label;

	private Button button;

	private List<Node> nodes;

	public FontSettingEditor( ProgramProduct product, String rbKey, SettingData setting ) {
		super( product, rbKey, setting );
	}

	@Override
	public void addComponents( GridPane pane, int row ) {
		String rbKey = setting.getRbKey();
		String value = setting.getSettings().get( getKey(), "SansSerif|12" );

		label = new Label( Rb.text( getProduct(), getRbKey(), rbKey ) );
		label.setMinWidth( Region.USE_PREF_SIZE );

		button = new Button();
		button.setMaxWidth( Double.MAX_VALUE );
		updateFont( value );

		nodes = List.of( label, button );

		// Set component state
		setDisable( setting.isDisable() );
		setVisible( setting.isVisible() );

		// Add the components
		pane.addRow( row, label, button );
	}

	@Override
	public List<Node> getComponents() {
		return nodes;
	}

	@Override
	protected void doSettingValueChanged( SettingsEvent event ) {
		if( event.getEventType() == SettingsEvent.CHANGED && getKey().equals( event.getKey() ) ) updateFont( event.getNewValue().toString() );
	}

	private void updateFont( String value ) {
		Font font = FontUtil.decode( value );
		log.atFine().log(  "Setting font updated: %s", font );
		button.setText( font.getName() + " " + font.getSize() );
		button.setFont( Font.font( font.getFamily(), FontUtil.getFontWeight( font.getStyle() ), FontUtil.getFontPosture( font.getStyle() ), -1 ) );
//		button.setOnAction( ( event ) -> {
//			FontSelectorDialog dialog = new FontSelectorDialog( font );
//			Optional<Font> optional = dialog.showAndWait();
//			optional.ifPresent( font1 -> {
//				log.log( Log.DEBUG,  "Setting font selected: " + font1 );
//				setting.getSettings().set( setting.getKey(), FontUtil.encode( font1 ) );
//			} );
//		} );
	}

}
