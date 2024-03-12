package com.avereon.xenon.tool.settings.editor.paint;

import javafx.scene.paint.Paint;

public interface PaintPalette {

	String getName();

	int columnCount();

	int rowCount();

	Paint getPaint( int row, int column );

}
