package com.xeomar.xenon.tool.settings;

import com.xeomar.xenon.product.Product;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.util.List;

public class SettingsPanel extends VBox {

	public static final String SETTING_KEY = "id";

	public static final String GROUP_KEY = "id";

	public static final String PREFERENCE_KEY = "key";

	public static final String PRESENTATION = "presentation";

	private String[] fontNames;

	private String[] fontStyles;

	private String[] fontSizes;

	// TODO Add Apply button and change functionality accordingly.
	// TODO Add a default button to set individual setting back to default.
	// TODO Add an undo button to set individual setting back to previous.

	public SettingsPanel( Product product, SettingsPage page ) {
		String fontPlain = product.getResourceBundle().getString( "settings", "font-plain" );
		String fontBold = product.getResourceBundle().getString( "settings", "font-bold" );
		String fontItalic = product.getResourceBundle().getString( "settings", "font-italic" );
		String fontBoldItalic = product.getResourceBundle().getString( "settings", "font-bold-italic" );

		List<String> fontFamilies = Font.getFamilies();
		fontNames = fontFamilies.toArray( new String[ fontFamilies.size() ] );
		fontStyles = new String[]{ fontPlain, fontBold, fontItalic, fontBoldItalic };
		fontSizes = new String[]{ "8", "10", "12", "14", "16", "18", "20", "22", "24", "26" };

		setBorder( new Border( new BorderStroke( Color.TRANSPARENT, BorderStrokeStyle.NONE, CornerRadii.EMPTY, BorderStroke.MEDIUM ) ) );

		// Get the title
		String title = page.getTitle();
		if( title == null ) title = product.getResourceBundle().getString( "settings", page.getId() );

		// Add the title label
		Label titleLabel = new Label( title );
		titleLabel.setBorder( new Border( new BorderStroke( Color.RED, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderStroke.MEDIUM ) ) );
		Font labelFont = titleLabel.getFont();
		titleLabel.setFont( Font.font( labelFont.getFamily(), 2 * labelFont.getSize() ) );
		titleLabel.setContentDisplay( ContentDisplay.CENTER );
		getChildren().add( titleLabel );

		// TODO Add the groups
		for( SettingGroup group : page.getGroups() ) {
			String groupKey = group.getId();
			//					addBlankLine( this );
			//					add( createGroupPane( product, page, BundleKey.SETTINGS, groupKey == null ? null : ProductUtil.getString( product, BundleKey.SETTINGS, Setting.getBundleName( groupKey ) ), group ) );
		}

		//				add( Box.createGlue() );
	}

}
