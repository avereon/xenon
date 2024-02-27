package com.avereon.xenon.tool.settings.panel;

import com.avereon.xenon.XenonProgramProduct;
import com.avereon.xenon.tool.settings.panel.products.DisplayMode;
import com.avereon.xenon.tool.settings.panel.products.ProductsSettingsPanel;

public class ProductsUpdatesSettingsPanel extends ProductsSettingsPanel {

	public ProductsUpdatesSettingsPanel( XenonProgramProduct product ) {
		super( product, DisplayMode.UPDATES );
	}

}
