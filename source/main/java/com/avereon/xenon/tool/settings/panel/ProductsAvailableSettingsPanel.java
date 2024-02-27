package com.avereon.xenon.tool.settings.panel;

import com.avereon.xenon.XenonProgramProduct;
import com.avereon.xenon.tool.settings.panel.products.DisplayMode;
import com.avereon.xenon.tool.settings.panel.products.ProductsSettingsPanel;

/**
 * This settings panel is used to display the available products and allow users
 * to install, uninstall, and update products.
 */
public class ProductsAvailableSettingsPanel extends ProductsSettingsPanel {

	public ProductsAvailableSettingsPanel( XenonProgramProduct product ) {
		super( product, DisplayMode.AVAILABLE );
	}

}
