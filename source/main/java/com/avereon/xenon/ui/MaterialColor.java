package com.avereon.xenon.ui;

import javafx.scene.paint.Color;

import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;

public class MaterialColor {

	public static final Color RED = Color.web( "#F44336" );

	public static final Color PINK = Color.web( "#E91E63" );

	public static final Color PURPLE = Color.web( "#9C27B0" );

	public static final Color DEEP_PURPLE = Color.web( "#673AB7" );

	public static final Color INDIGO = Color.web( "#3F51B5" );

	public static final Color BLUE = Color.web( "#2196F3" );

	public static final Color LIGHT_BLUE = Color.web( "#03A9F4" );

	public static final Color CYAN = Color.web( "#00BCD4" );

	public static final Color TEAL = Color.web( "#009688" );

	public static final Color GREEN = Color.web( "#4CAF50" );

	public static final Color LIGHT_GREEN = Color.web( "#8BC34A" );

	public static final Color LIME = Color.web( "#CDDC39" );

	public static final Color YELLOW = Color.web( "#FFEB3B" );

	public static final Color AMBER = Color.web( "#FFC107" );

	public static final Color ORANGE = Color.web( "#FF9800" );

	public static final Color DEEP_ORANGE = Color.web( "#FF5722" );

	public static final Color BROWN = Color.web( "#725548" );

	public static final Color GRAY = Color.web( "#9E9E9E" );

	public static final Color BLUE_GRAY = Color.web( "#607D8B" );

	private static final Set<Color> COLORS;

	private static final Map<Color, String> NAMES;

	static {
		COLORS = new CopyOnWriteArraySet<>();
		NAMES = new HashMap<>();

		init( RED, "Red" );
		init( PINK, "Pink" );
		init( PURPLE, "Purple" );
		init( DEEP_PURPLE, "Deep Purple" );
		init( INDIGO, "Indigo" );
		init( BLUE, "Blue" );
		init( LIGHT_BLUE, "Pale Blue" );
		init( CYAN, "Cyan" );
		init( TEAL, "Teal" );
		init( GREEN, "Green" );
		init( LIGHT_GREEN, "Pale Green" );
		init( LIME, "Lime" );
		init( YELLOW, "Yellow" );
		init( AMBER, "Amber" );
		init( ORANGE, "Orange" );
		init( DEEP_ORANGE, "Deep Orange" );

		init( BROWN, "Brown" );
		init( GRAY, "Gray" );
		init( BLUE_GRAY, "Blue Gray" );
	}

	private static void init( Color color, String name ) {
		COLORS.add( color );
		NAMES.put( color, name );
	}

	public static Set<Color> getColors() {
		return COLORS;
	}

	public static String getName( Color color ) {
		return NAMES.get( color );
	}

}
