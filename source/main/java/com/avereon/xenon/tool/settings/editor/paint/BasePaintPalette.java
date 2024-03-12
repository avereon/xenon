package com.avereon.xenon.tool.settings.editor.paint;

import com.avereon.zarra.color.Colors;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

import java.util.List;

public abstract class BasePaintPalette implements PaintPalette {

	private final String name;

	private final Paint[][] paints;

	private final int rowCount;

	private final int columnCount;

	public BasePaintPalette( String name, List<Color> colors ) {
		this( name, colors, 4, 4 );
	}

	public BasePaintPalette( String name, List<Color> colors, int shadeCount, int tintCount ) {
		this.name = name;
		rowCount = shadeCount + 1 + tintCount;
		columnCount = colors.size();

		int row = 0;
		int column = 0;
		paints = new Paint[ rowCount ][ columnCount ];

		for( Color base : colors ) {
			// Shades
			double shadeFactorOffset = 1.0 / shadeCount;

			for( int index = 0; index < shadeCount; index++ ) {
				paints[ row ][ column ] = Colors.getShade( base, 1.0 - index * shadeFactorOffset );
				row++;
			}

			// Hue
			paints[ row ][ column ] = base;
			row++;

			// Tints
			double tintFactorOffset = 1.0 / tintCount;
			for( int index = 1; index <= tintCount; index++ ) {
				paints[ row ][ column ] = Colors.getTint( base, index * tintFactorOffset );
				row++;
			}

			column++;
			row = 0;
		}
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public int columnCount() {
		return columnCount;
	}

	@Override
	public int rowCount() {
		return rowCount;
	}

	@Override
	public Paint getPaint( int row, int column ) {
		return paints[ row ][ column ];
	}

}
