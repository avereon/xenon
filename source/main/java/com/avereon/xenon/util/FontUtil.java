package com.avereon.xenon.util;

import com.avereon.util.LogUtil;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import org.slf4j.Logger;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;

public final class FontUtil {

	private static final Logger log = LogUtil.get( MethodHandles.lookup().lookupClass() );

	private static List<String> monoFamilyList;

	private static String SEPARATOR = "|";

	public static String encode( Font font ) {
		if( font == null ) return null;
		return font.getFamily() + SEPARATOR + font.getStyle() + SEPARATOR + font.getSize();
	}

	public static Font decode( String string ) {
		if( string == null ) return null;

		int index;
		int start = 0;
		List<String> strings = new ArrayList<>();
		while( (index = string.indexOf( SEPARATOR, start )) > -1 ) {
			strings.add( string.substring( start, index ) );
			start = index + SEPARATOR.length();
		}
		strings.add( string.substring( start ) );

		String family = string;
		String style = FontPosture.REGULAR.name();
		String sizeString = "-1";
		switch( strings.size() ) {
			case 2: {
				family = strings.get( 0 );
				sizeString = strings.get( 1 );
				break;
			}
			case 3: {
				family = strings.get( 0 );
				style = strings.get( 1 );
				sizeString = strings.get( 2 );
				break;
			}
		}

		double size = -1;
		try {
			size = Double.parseDouble( sizeString );
		} catch( NumberFormatException exception ) {
			log.warn( "Error parsing font size", exception );
		}

		return Font.font( family, getFontWeight(style), getFontPosture( style ), size );
	}

	public static FontWeight getFontWeight( String string ) {
		string = string.toUpperCase();
		for( FontWeight weight : FontWeight.values() ) {
			if( string.contains( weight.name() ) ) return weight;
		}
		return FontWeight.NORMAL;
	}

	public static FontPosture getFontPosture( String string ) {
		string = string.toUpperCase();
		for( FontPosture posture : FontPosture.values() ) {
			if( string.contains( posture.name() ) ) return posture;
		}

		return FontPosture.REGULAR;
	}

	/**
	 * Return a list of all the mono-spaced fonts on the system.
	 *
	 * @return An observable list of all of the mono-spaced fonts on the system.
	 */
	public static List<String> getMonoFontFamilyNames() {
		if( monoFamilyList != null ) return monoFamilyList;

		// Compare the layout widths of two strings. One string is composed
		// of "thin" characters, the other of "wide" characters. In mono-spaced
		// fonts the widths should be the same.

		final Text thinText = new Text( "1 l" ); // note the space
		final Text thickText = new Text( "MWX" );

		List<String> fontFamilyList = Font.getFamilies();
		List<String> monoFamilyList = new ArrayList<>();

		Font font;

		for( String fontFamilyName : fontFamilyList ) {
			font = Font.font( fontFamilyName, FontWeight.NORMAL, FontPosture.REGULAR, 14.0d );
			thinText.setFont( font );
			thickText.setFont( font );
			if( thinText.getLayoutBounds().getWidth() == thickText.getLayoutBounds().getWidth() ) monoFamilyList.add( fontFamilyName );
		}

		return monoFamilyList;
	}

}
