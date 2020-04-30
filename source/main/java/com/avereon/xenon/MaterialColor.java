package com.avereon.xenon;

import javafx.scene.paint.Color;

import java.util.*;

public class MaterialColor {

	private static final List<String> MAT_COLOR_IDS;

	private static final Map<String, String> MAT_COLOR_NAMES;

	private static final Map<String, Color> MAT_COLORS;

	static {
		MAT_COLOR_IDS = new ArrayList<>();
		MAT_COLOR_NAMES = new HashMap<>();
		MAT_COLORS = new HashMap<>();

		orderedInit( "red", "Red", "#F44336" );
		orderedInit( "pink", "Pink", "#E91E63");
		orderedInit( "purple", "Purple", "#9C27B0");
		orderedInit( "deepPurple", "Deep Purple", "#673AB7");
		orderedInit( "indigo", "Indigo", "#3F51B5");
		orderedInit( "blue", "Blue", "#2196F3");
		orderedInit( "lightBlue", "Light Blue", "#03A9F4");
		orderedInit( "cyan", "Cyan", "#00BCD4");
		orderedInit( "teal", "Teal", "#009688");
		orderedInit( "green", "Green", "#4CAF50");
		orderedInit( "lightGreen", "Light Green", "#8BC34A");
		orderedInit( "lime", "Lime", "#CDDC39");
		orderedInit( "yellow", "Yellow", "#FFEB3B");
		orderedInit( "amber", "Amber", "#FFC107");
		orderedInit( "orange", "Orange", "#FF9800");
		orderedInit( "deepOrange", "Deep Orange", "#FF5722");

		init( "brown", "Brown", "#725548");
		init( "gray", "Gray", "#9E9E9E");
		init( "blueGray", "Blue Gray", "#607D8B");
	}

	private static void orderedInit( String id, String name, String web ) {
		MAT_COLOR_IDS.add( id );
		MAT_COLOR_NAMES.put( id, name );
		MAT_COLORS.put( id, Color.web( web ) );
	}

	private static void init( String id, String name, String web ) {
		MAT_COLOR_NAMES.put( id, name );
		MAT_COLORS.put( id, Color.web( web ) );
	}

	public static List<String> getWheelIds() {
		return MAT_COLOR_IDS;
	}

	public static Set<String> getIds() {
		return MAT_COLORS.keySet();
	}

	public static String getName( String id ) {
		return MAT_COLOR_NAMES.get( id );
	}

	public static Color getColor( String id ) {
		return MAT_COLORS.get( id );
	}

}
