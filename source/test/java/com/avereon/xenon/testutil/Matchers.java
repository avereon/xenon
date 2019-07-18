package com.avereon.xenon.testutil;

import org.hamcrest.CustomTypeSafeMatcher;
import org.hamcrest.Matcher;

public class Matchers {

	public static Matcher<String> regex( String pattern ) {
		return new CustomTypeSafeMatcher<String>( "matching regex pattern " + pattern ) {

			@Override
			protected boolean matchesSafely( String item ) {
				return item.matches( pattern );
			}

		};
	}

}
