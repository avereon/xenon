package com.avereon.xenon.tool.settings.editor.paint;

import com.avereon.product.Rb;
import com.avereon.xenon.RbKey;
import com.avereon.zarra.color.MaterialColor;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class MaterialPaintPalette extends BasePaintPalette {

	private static final List<Color> BASE_COLORS;

	static {
		BASE_COLORS = new ArrayList<>( MaterialColor.getColors() );
		BASE_COLORS.sort( Comparator.comparingDouble( Color::getHue ) );

		// Some special colors
		BASE_COLORS.add( 1, BASE_COLORS.remove( 3 ) );
		BASE_COLORS.add( 2, BASE_COLORS.remove( 13 ) );
	}

	public MaterialPaintPalette() {
		super( Rb.text( RbKey.LABEL, "palette-material" ), BASE_COLORS );
	}

}
