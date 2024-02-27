package com.avereon.xenon.tool.settings.panel;

import com.avereon.product.Rb;
import com.avereon.xenon.RbKey;
import com.avereon.xenon.XenonProgramProduct;
import com.avereon.xenon.tool.settings.SettingsPanel;

public class ProductSettingsPanel extends SettingsPanel {

	public static final String INSTALLED = "installed";

	public static final String AVAILABLE = "available";

	public static final String UPDATES = "updates";

	public static final String SOURCES = "sources";

	static final int ICON_SIZE = 48;

	public ProductSettingsPanel( XenonProgramProduct product ) {
		super( product );

		// Add the title to the panel
		addTitle( Rb.text( product, RbKey.SETTINGS, "products" ) );
	}

}
