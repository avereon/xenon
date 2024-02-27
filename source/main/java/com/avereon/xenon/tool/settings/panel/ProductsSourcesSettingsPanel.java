package com.avereon.xenon.tool.settings.panel;

import com.avereon.xenon.XenonProgramProduct;
import com.avereon.xenon.tool.settings.panel.products.DisplayMode;
import com.avereon.xenon.tool.settings.panel.products.ProductsSettingsPanel;

public class ProductsSourcesSettingsPanel extends ProductsSettingsPanel {

	public ProductsSourcesSettingsPanel( XenonProgramProduct product ) {
		super( product, DisplayMode.SOURCES );
	}

}
