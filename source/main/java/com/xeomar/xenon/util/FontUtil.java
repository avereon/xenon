package com.xeomar.xenon.util;

import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.List;

public final class FontUtil {

	private static List<String> monoFamilyList;

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

		final Text thinTxt = new Text( "1 l" ); // note the space
		final Text thikTxt = new Text( "MWX" );

		List<String> fontFamilyList = Font.getFamilies();
		List<String> monoFamilyList = new ArrayList<>();

		Font font;

		for( String fontFamilyName : fontFamilyList ) {
			font = Font.font( fontFamilyName, FontWeight.NORMAL, FontPosture.REGULAR, 14.0d );
			thinTxt.setFont( font );
			thikTxt.setFont( font );
			if( thinTxt.getLayoutBounds().getWidth() == thikTxt.getLayoutBounds().getWidth() ) {
				monoFamilyList.add( fontFamilyName );
				System.out.println( "Font family name: " + fontFamilyName );
			}
		}

		return monoFamilyList;
	}

}
