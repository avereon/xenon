package com.avereon.xenon.tool.settings.editor.paint;

import com.avereon.product.Rb;
import com.avereon.xenon.RbKey;
import javafx.scene.paint.Color;

import java.util.List;

public class StandardPalette extends BasePaintPalette{

	public StandardPalette( ) {
		super( Rb.text( RbKey.LABEL, "palette-standard" ), List.of( Color.GRAY, Color.BROWN, Color.RED, Color.ORANGE, Color.YELLOW, Color.GREEN, Color.BLUE, Color.PURPLE) );
	}

}
