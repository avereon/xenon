package com.avereon.xenon.tool.settings.editor.paint;

import com.avereon.product.Rb;
import com.avereon.xenon.RbKey;
import com.avereon.zarra.color.Colors;

import java.util.List;

public class GoogleSheetsPalette extends BasePaintPalette {

	public GoogleSheetsPalette() {
		super(
			Rb.text( RbKey.LABEL, "palette-google-sheets" ),
			List.of( Colors.parse( "#a21414" ),
				Colors.parse( "#ff0000" ),
				Colors.parse( "#ff9900" ),
				Colors.parse( "#ffff00" ),
				Colors.parse( "#00ff00" ),
				Colors.parse( "#00ffff" ),
				Colors.parse( "#4a86e8" ),
				Colors.parse( "#0000ff" ),
				Colors.parse( "#9900ff" ),
				Colors.parse( "#ff00ff" )
			)
		);
	}

}
