package com.avereon.xenon.tool.settings;

import com.avereon.product.Rb;
import com.avereon.xenon.XenonProgramProduct;
import lombok.CustomLog;
import lombok.Getter;

import java.util.Map;

@Getter
@CustomLog
public class SettingsPagePanel extends SettingsPanel {

	private final SettingsPage page;

	//	private String[] fontNames;
	//
	//	private String[] fontStyles;
	//
	//	private String[] fontSizes;

	// TODO Add Apply button and change functionality accordingly.
	// TODO Add a default button to set individual setting back to default.
	// TODO Add an undo button to set individual setting back to previous.

	/**
	 * @param page The settings page for the panel
	 */
	public SettingsPagePanel( SettingsPage page ) {
		this( page, false );
	}

	public SettingsPagePanel( SettingsPage page, Map<String, SettingOptionProvider> optionProviders ) {
		this( page, false, optionProviders );
	}

	public SettingsPagePanel( SettingsPage page, boolean showTitle ) {
		this( page, showTitle, null );
	}

	public SettingsPagePanel( SettingsPage page, boolean showTitle, Map<String, SettingOptionProvider> optionProviders ) {
		super( page.getProduct(), optionProviders );

		this.page = page;

		//		String fontPlain = product.getResourceBundle().getString( rbKey, "font-plain" );
		//		String fontBold = product.getResourceBundle().getString( rbKey, "font-bold" );
		//		String fontItalic = product.getResourceBundle().getString( rbKey, "font-italic" );
		//		String fontBoldItalic = product.getResourceBundle().getString( rbKey, "font-bold-italic" );
		//
		//		List<String> fontFamilies = Font.getFamilies();
		//		fontNames = fontFamilies.toArray( new String[ fontFamilies.size() ] );
		//		fontStyles = new String[]{ fontPlain, fontBold, fontItalic, fontBoldItalic };
		//		fontSizes = new String[]{ "8", "10", "12", "14", "16", "18", "20", "22", "24", "26" };

		//setBorder( new Border( new BorderStroke( Color.BLUE, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderStroke.MEDIUM ) ) );

		if( showTitle ) addTitle( page.getTitle() );

		XenonProgramProduct product = page.getProduct();
		String rbKey = page.getRbKey();

		// Add the groups
		for( SettingGroup group : page.getGroups() ) {
			String name = Rb.text( product, rbKey, group.getId() );
			getChildren().add( createGroupPane( product, rbKey, page, name, group ) );
		}
	}

}
