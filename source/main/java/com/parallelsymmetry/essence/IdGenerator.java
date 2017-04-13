package com.parallelsymmetry.essence;

import java.util.Random;

public class IdGenerator {

	private static final Random RANDOM = new Random();

	private static char[] map;

	static {
		// List of consonant letters with no descenders in no particular order
		map = "mhnflcdtvkxzrwbs".toCharArray();
	}

	public static String getId() {
		return toString( RANDOM.nextLong() ^ System.currentTimeMillis() );
	}

	// FIXME This only creates an 8 char id but it can create a 16 char id
	public static String toString( long value ) {
		StringBuilder builder = new StringBuilder();

		for( int count = 0; count < 8; count++ ) {
			builder.append( convertDigit( (int)(value & 0xF) ) );
			value >>= 4;
		}

		return builder.toString();
	}

	private static char convertDigit( int value ) {
		if( value < 0 || value > 15 ) throw new IllegalArgumentException( "Value must be between 0 and 15: " + value );
		return map[value];
	}

}
