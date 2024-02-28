package com.avereon.xenon.tool.settings.panel.products;

import com.avereon.xenon.Xenon;
import com.avereon.xenon.XenonProgramProduct;
import com.avereon.xenon.product.ProductManager;
import javafx.scene.layout.GridPane;
import lombok.Getter;

@Getter
public class BaseTile extends GridPane {

	private final Xenon program;

	private final ProductsSettingsPanel productSettingsPanel;

	private final ProductManager productManager;

	public BaseTile( XenonProgramProduct product, ProductsSettingsPanel productSettingsPanel ) {
		this.program = product.getProgram();
		this.productSettingsPanel = productSettingsPanel;
		this.productManager = program.getProductManager();
	}

}
