package com.xeomar.xenon;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.MessageDigest;
import java.util.Random;

public class IdGenerator {

	private static final Logger log = LoggerFactory.getLogger( IdGenerator.class );

	private static final Random RANDOM = new Random();

	private static char[] map;

	static {
		// The character map must contain 16 characters, no more, no less
		// List of consonant letters with no descenders in no particular order
		map = "mhnflcdtvkxzrwbs".toCharArray();
	}

	public static String getId() {
		return getId( RANDOM.nextLong() ^ System.currentTimeMillis() );
	}

	public static String getId( String string ) {
		long id = 0;

		try {
			MessageDigest digest = MessageDigest.getInstance( "SHA-256" );
			byte[] hash = digest.digest( string.getBytes( "UTF-8" ) );
			for( int index = 0; index < hash.length; index++ ) {
				long value = hash[index];
				value = value << (index%8 * 8);
				id ^= value;
			}
		} catch( Exception exception ) {
			log.error( "Error computing id for: " + string, exception );
		}

		return getId( id );
	}

	public static String getId( long value ) {
		StringBuilder builder = new StringBuilder();

		for( int count = 0; count < 16; count++ ) {
			builder.append( map[ (int)(value & 0xF) ] );
			value >>= 4;
		}

		return builder.toString();
	}

}
