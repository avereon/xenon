package com.xeomar.xenon.util;

import com.xeomar.util.LineParser;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

public final class TextUtil {

	public static final int LEFT = -1;

	public static final int CENTER = 0;

	public static final int RIGHT = 1;

	public static final String DEFAULT_ENCODING = "UTF-8";

	private static final char DEFAULT_PAD_CHAR = ' ';

	public static final boolean isEmpty( String string ) {
		if( string == null ) return true;
		if( string.trim().length() == 0 ) return true;
		return false;
	}

	public static final boolean areEqual( String string1, String string2 ) {
		if( string1 == null && string2 == null ) return true;
		if( string1 == null && string2 != null ) return false;
		if( string1 != null && string2 == null ) return false;
		return string1.equals( string2 );
	}

	public static final boolean areEqualIgnoreCase( String string1, String string2 ) {
		if( string1 == null && string2 == null ) return true;
		if( string1 == null && string2 != null ) return false;
		if( string1 != null && string2 == null ) return false;
		return string1.equalsIgnoreCase( string2 );
	}

	public static final boolean areSame( String string1, String string2 ) {
		if( isEmpty( string1 ) && isEmpty( string2 ) ) return true;
		if( string1 == null && string2 != null ) return false;
		if( string1 != null && string2 == null ) return false;
		return string1.equals( string2 );
	}

	public static final int compare( String string1, String string2 ) {
		if( string1 == null && string2 == null ) return 0;
		if( string1 == null && string2 != null ) return -1;
		if( string1 != null && string2 == null ) return 1;
		return string1.compareTo( string2 );
	}

	public static final int compareIgnoreCase( String string1, String string2 ) {
		if( string1 == null && string2 == null ) return 0;
		if( string1 == null && string2 != null ) return -1;
		if( string1 != null && string2 == null ) return 1;
		return string1.compareToIgnoreCase( string2 );
	}

	public static final String cleanNull( String string ) {
		if( string == null ) return null;
		string = string.trim();
		return "".equals( string ) ? null : string;
	}

	public static final String cleanEmpty( String string ) {
		return string == null ? "" : string.trim();
	}

	/**
	 * Concatenate multiple objects together using a fast string building object.
	 *
	 * @param objects
	 * @return
	 */
	public static final String concatenate( Object... objects ) {
		StringBuilder builder = new StringBuilder();

		for( Object object : objects ) {
			builder.append( object == null ? "null" : object.toString() );
		}

		return builder.toString();
	}

	/**
	 * Return the string representation of the MD5 sum of the specified string.
	 * This method is implemented using NIO buffers so it should be efficient for
	 * large strings without causing memory or CPU issues.
	 *
	 * @param string
	 * @return
	 */
	public static final byte[] getMD5Sum( String string ) {
		if( string == null ) return null;

		int width = 512;
		try {
			byte[] buffer = new byte[width];
			MessageDigest digest = MessageDigest.getInstance( "MD5" );
			CharBuffer chars = CharBuffer.wrap( string );
			ByteBuffer bytes = ByteBuffer.allocateDirect( width );

			CharsetEncoder encoder = Charset.forName( DEFAULT_ENCODING ).newEncoder();
			while( chars.hasRemaining() ) {
				encoder.encode( chars, bytes, false );
				bytes.flip();
				bytes.get( buffer, 0, bytes.limit() );
				digest.update( buffer, 0, bytes.limit() );
				bytes.flip();
			}
			return digest.digest();
		} catch( NoSuchAlgorithmException exception ) {
			return null;
		}
	}

	public static final String toString( Object[] array ) {
		return toString( Arrays.asList( array ) );
	}

	public static final String toString( Object[] array, int offset ) {
		return toString( array, offset, array.length - offset );
	}

	public static final String toString( Object[] array, int offset, int length ) {
		Object[] items = new Object[length];
		System.arraycopy( array, offset, items, 0, length );
		return toString( Arrays.asList( items ) );
	}

	public static final String toString( Object[] array, String delimiter ) {
		return toString( Arrays.asList( array ), delimiter );
	}

	public static final String toString( Object[] array, String delimiter, int offset ) {
		return toString( array, delimiter, offset, array.length - offset );
	}

	public static final String toString( Object[] array, String delimiter, int offset, int length ) {
		Object[] items = new Object[length];
		System.arraycopy( array, offset, items, 0, length );
		return toString( Arrays.asList( items ), delimiter );
	}

	public static final String toString( List<? extends Object> list ) {
		return toString( list, "[", "]" );
	}

	public static final String toString( List<? extends Object> list, String delimiter ) {
		return toString( list, delimiter, null );
	}

	public static final String toString( List<? extends Object> list, String prefix, String suffix ) {
		if( list == null ) return null;

		boolean delimiter = suffix == null;

		StringBuilder builder = new StringBuilder();

		for( Object object : list ) {
			if( !delimiter && builder.length() > 0 ) builder.append( " " );
			if( !delimiter || builder.length() > 0 ) builder.append( prefix );
			builder.append( object.toString() );
			if( !delimiter ) builder.append( suffix );
		}

		return builder.toString();
	}

	/**
	 * Returns a printable string representation of a byte by converting byte
	 * values less than 32 or greater than 126 to the integer value surrounded by
	 * brackets.
	 * <p>
	 * Example: An escape char (27) would be returned as: [27]
	 * <p>
	 * Example: The letter A would be returned as: A
	 *
	 * @param data The byte to convert.
	 * @return A printable string representation of the byte.
	 */
	public static final String toPrintableString( byte data ) {
		String result = null;
		int dataAsInt = data;
		if( dataAsInt >= 32 && dataAsInt <= 126 ) {
			result = String.valueOf( (char)dataAsInt );
		} else {
			short value = (short)dataAsInt;
			result = "[" + String.valueOf( (int)( value < 0 ? value + 256 : value ) ) + "]";
			if( value == 13 ) result += "\n";
		}

		return result;
	}

	/**
	 * Returns a printable string representation of a character by converting char
	 * values less than or equal to 32 or greater than or equal to 126 to the
	 * integer value surrounded by brackets.
	 * <p>
	 * Example: An escape char (27) would be returned as: [27]
	 * <p>
	 * Example: The letter A would be returned as: A
	 *
	 * @param data The character to convert.
	 * @return A printable string representation of the character.
	 */
	public static final String toPrintableString( char data ) {

		if( data >= 32 && data <= 126 ) {
			return String.valueOf( data );
		} else {
			short value = (short)data;
			return "[" + String.valueOf( (int)( value < 0 ? value + 65536 : value ) ) + "]";
		}
	}

	public static final String toPrintableString( byte[] data ) {
		return toPrintableString( data, 0, data.length );
	}

	public static final String toPrintableString( byte[] data, int offset, int length ) {
		if( data == null ) return null;
		StringBuilder builder = new StringBuilder();
		int count = offset + length;
		for( int index = offset; index < count; index++ ) {
			byte value = data[index];
			builder.append( toPrintableString( (char)( value < 0 ? value + 256 : value ) ) );
		}
		return builder.toString();
	}

	public static final String toPrintableString( char[] data ) {
		if( data == null ) return null;
		StringBuilder builder = new StringBuilder();
		int count = data.length;
		for( int index = 0; index < count; index++ ) {
			builder.append( toPrintableString( data[index] ) );
		}
		return builder.toString();
	}

	public static final String toPrintableString( String data ) {
		return toPrintableString( data.toCharArray() );
	}

	/**
	 * Convert an array of bytes to a HEX encoded string.
	 *
	 * @param bytes The bytes to convert to hex.
	 * @return A hex encoded string of the byte array.
	 */
	public static final String toHexEncodedString( byte[] bytes ) {
		int value = 0;
		int count = bytes.length;
		String string = null;
		StringBuilder builder = new StringBuilder();
		for( int index = 0; index < count; index++ ) {
			value = bytes[index];
			string = Integer.toHexString( value < 0 ? value + 256 : value );
			if( string.length() == 1 ) builder.append( "0" );
			builder.append( string );
		}
		return builder.toString();
	}

	/**
	 * Encode a string to a hex string.
	 *
	 * @param string
	 * @return
	 */
	public static final String hexEncode( String string ) {
		return secureHexEncode( string.toCharArray() );
	}

	/**
	 * Decode a hex string to a string.
	 *
	 * @param string
	 * @return
	 */
	public static final String hexDecode( String string ) {
		char[] value = secureHexDecode( string );
		return value == null ? null : new String( value );
	}

	public static final String secureHexByteEncode( byte[] bytes ) {
		if( bytes == null ) return null;

		int value = 0;
		int count = bytes.length;
		String string = null;
		StringBuilder builder = new StringBuilder();
		for( int index = 0; index < count; index++ ) {
			value = bytes[index];
			string = Integer.toHexString( value < 0 ? value + 256 : value );
			if( string.length() == 1 ) builder.append( "0" );
			builder.append( string );
		}

		return builder.toString();
	}

	public static final byte[] secureHexByteDecode( String string ) {
		if( string == null ) return null;

		int count = string.length();
		if( count % 2 != 0 ) throw new IllegalArgumentException( "Invalid string length: " + count );

		// Divide the count by two.
		count /= 2;

		byte[] bytes = new byte[count];
		for( int index = 0; index < count; index += 1 ) {
			bytes[index] = (byte)Integer.parseInt( string.substring( index * 2, index * 2 + 2 ), 16 );
		}

		return bytes;
	}

	/**
	 * <p>
	 * Encode the char[] into a hex string. Secure means that at no time is the
	 * char[] converted, in its entirety, to or from a String. See the security
	 * note below.
	 * <p>
	 * Security note: Objects of type String are immutable, meaning there are no
	 * methods defined that allow you to change (overwrite) or zero out the
	 * contents of a String after usage. This feature makes String objects
	 * unsuitable for storing security sensitive information, such as passwords.
	 * The String objects can easily be discovered using standard debugging tools
	 * or other methods that can inspect the JVM memory. Security sensitive
	 * information should always be collected and stored in a char array instead.
	 *
	 * @param chars
	 * @return
	 */
	public static final String secureHexEncode( char[] chars ) {
		if( chars == null ) return null;

		int count = chars.length;
		String string = null;
		StringBuilder builder = new StringBuilder();
		for( int index = 0; index < count; index++ ) {
			string = Integer.toHexString( chars[index] );
			builder.append( rightJustify( string, 4, '0' ) );
		}

		return builder.toString();
	}

	/**
	 * Decode a hex string into a char[]. Secure means that at no time is the
	 * char[] converted, in its entirety, to or from a String. See the security
	 * note below.
	 * <p>
	 * Security note: Objects of type String are immutable, meaning there are no
	 * methods defined that allow you to change (overwrite) or zero out the
	 * contents of a String after usage. This feature makes String objects
	 * unsuitable for storing security sensitive information, such as passwords.
	 * The String objects can easily be discovered using standard debugging tools
	 * or other methods that can inspect the JVM memory. Security sensitive
	 * information should always be collected and stored in a char array instead.
	 *
	 * @param string
	 * @return
	 */
	public static final char[] secureHexDecode( String string ) {
		if( string == null ) return null;

		int count = string.length();
		if( count % 4 != 0 ) throw new IllegalArgumentException( "Invalid string length: " + count );

		// Divide the count by four.
		count /= 4;

		char[] chars = new char[count];
		for( int index = 0; index < count; index += 1 ) {
			chars[index] = (char)Integer.parseInt( string.substring( index * 4, index * 4 + 4 ), 16 );
		}

		return chars;
	}

	public static final boolean isInteger( String text ) {
		if( text == null ) return false;

		try {
			Integer.parseInt( text );
		} catch( NumberFormatException exception ) {
			return false;
		}
		return true;
	}

	public static final boolean isLong( String text ) {
		if( text == null ) return false;

		try {
			Long.parseLong( text );
		} catch( NumberFormatException exception ) {
			return false;
		}
		return true;
	}

	public static final boolean isFloat( String text ) {
		if( text == null ) return false;

		try {
			Float.parseFloat( text );
		} catch( NumberFormatException exception ) {
			return false;
		}
		return true;
	}

	public static final boolean isDouble( String text ) {
		if( text == null ) return false;

		try {
			Double.parseDouble( text );
		} catch( NumberFormatException exception ) {
			return false;
		}
		return true;
	}

	public static final String capitalize( String string ) {
		if( string == null ) return null;
		char[] chars = string.toCharArray();
		if( chars.length > 0 ) chars[0] = Character.toUpperCase( chars[0] );
		return new String( chars );
	}

	public static final String justify( int alignment, String text, int width ) {
		return justify( alignment, text, width, DEFAULT_PAD_CHAR );
	}

	public static final String justify( int alignment, String text, int width, char chr ) {
		return justify( alignment, text, width, chr, 0 );
	}

	public static final String justify( int alignment, String text, int width, char chr, int pad ) {
		switch( alignment ) {
			case CENTER:
				return centerJustify( text, width, chr, pad );
			case RIGHT:
				return rightJustify( text, width, chr, pad );
			default:
				return leftJustify( text, width, chr, pad );
		}
	}

	public static final String pad( int width ) {
		return pad( width, DEFAULT_PAD_CHAR );
	}

	public static final String pad( int width, char chr ) {
		if( width <= 0 ) return "";
		char[] pad = new char[width];
		Arrays.fill( pad, chr );
		return new String( pad );
	}

	public static final String leftJustify( String text, int width ) {
		return leftJustify( text, width, DEFAULT_PAD_CHAR );
	}

	public static final String leftJustify( String text, int width, char chr ) {
		return leftJustify( text, width, chr, 0 );
	}

	public static final String leftJustify( String text, int width, char chr, int pad ) {
		if( text == null ) return pad( width );
		if( text.length() > width ) return text.substring( 0, width );

		int right = width - text.length();
		StringBuilder builder = new StringBuilder( width );
		builder.append( text );
		if( right <= pad ) {
			builder.append( pad( right ) );
		} else {
			builder.append( pad( pad ) );
			builder.append( pad( right - pad, chr ) );
		}
		return builder.toString();
	}

	public static final String centerJustify( String text, int width ) {
		return centerJustify( text, width, DEFAULT_PAD_CHAR );
	}

	public static final String centerJustify( String text, int width, char chr ) {
		return centerJustify( text, width, chr, 0 );
	}

	public static final String centerJustify( String text, int width, char chr, int pad ) {
		if( text == null ) return pad( width );
		if( text.length() > width ) return text.substring( 0, width );

		int left = ( width - text.length() ) / 2;
		int right = ( width - text.length() ) - left;

		StringBuilder builder = new StringBuilder( width );
		if( left <= pad ) {
			builder.append( pad( left ) );
		} else {
			builder.append( pad( left - pad, chr ) );
			builder.append( pad( pad ) );
		}
		builder.append( text );
		if( right <= pad ) {
			builder.append( pad( right ) );
		} else {
			builder.append( pad( pad ) );
			builder.append( pad( right - pad, chr ) );
		}
		return builder.toString();
	}

	public static final String rightJustify( String text, int width ) {
		return rightJustify( text, width, DEFAULT_PAD_CHAR );
	}

	public static final String rightJustify( String text, int width, char chr ) {
		return rightJustify( text, width, chr, 0 );
	}

	public static final String rightJustify( String text, int width, char chr, int pad ) {
		if( text == null ) return pad( width );
		if( text.length() > width ) return text.substring( 0, width );

		int left = width - text.length();
		StringBuilder builder = new StringBuilder( width );
		if( left <= pad ) {
			builder.append( pad( left ) );
		} else {
			builder.append( pad( left - pad, chr ) );
			builder.append( pad( pad ) );
		}
		builder.append( text );
		return builder.toString();
	}

	public static final List<String> getLines( String text ) {
		if( text == null ) return null;

		List<String> lines = new ArrayList<String>();
		LineParser parser = new LineParser( text );
		while( parser.next() != null ) {
			lines.add( parser.getLine() );
		}

		return lines;
	}

	public static final int getLineCount( String text ) {
		if( text == null ) return 0;

		int count = 0;
		LineParser parser = new LineParser( text );
		while( parser.next() != null ) {
			count++;
		}

		return count;
	}

	public static final int countLines( List<String> lines, String pattern ) {
		int result = 0;
		Pattern p = Pattern.compile( pattern );

		for( String line : lines ) {
			if( p.matcher( line ).matches() ) result++;
		}

		return result;
	}

	public static final String findLine( List<String> lines, String pattern ) {
		return findLine( lines, pattern, 0 );
	}

	public static final String findLine( List<String> lines, String pattern, int start ) {
		Pattern p = Pattern.compile( pattern );

		int count = lines.size();
		for( int index = start; index < count; index++ ) {
			String line = lines.get( index );
			if( p.matcher( line ).matches() ) return line;
		}

		return null;
	}

	public static final List<String> findLines( List<String> lines, String pattern ) {
		return findLines( lines, pattern, 0 );
	}

	public static final List<String> findLines( List<String> lines, String pattern, int start ) {
		List<String> result = new ArrayList<String>();

		Pattern p = Pattern.compile( pattern );

		int count = lines.size();
		for( int index = start; index < count; index++ ) {
			String line = lines.get( index );
			if( p.matcher( line ).matches() ) result.add( line );
		}

		return result;
	}

	public static final int findLineIndex( List<String> lines, String pattern ) {
		return findLineIndex( lines, pattern, 0 );
	}

	public static final int findLineIndex( List<String> lines, String pattern, int start ) {
		Pattern p = Pattern.compile( pattern );

		int count = lines.size();
		for( int index = start; index < count; index++ ) {
			if( p.matcher( lines.get( index ) ).matches() ) return index;
		}

		return -1;
	}

	public static final String prepend( String content, String text ) {
		if( content == null ) return null;
		if( text == null || "".equals( text ) ) return content;

		String line = null;
		LineParser parser = new LineParser( content );
		StringBuilder result = new StringBuilder();

		while( ( line = parser.next() ) != null ) {
			result.append( text );
			result.append( line );
			result.append( parser.getTerminator() );
		}

		return result.toString();
	}

	public static final String append( String content, String text ) {
		if( content == null ) return null;
		if( text == null || "".equals( text ) ) return content;

		String line = null;
		LineParser parser = new LineParser( content );
		StringBuilder result = new StringBuilder();

		while( ( line = parser.next() ) != null ) {
			result.append( line );
			result.append( text );
			result.append( parser.getTerminator() );
		}

		return result.toString();
	}

	public static final String reline( String text, int width ) {
		if( text == null ) return null;

		StringBuilder line = new StringBuilder();
		StringBuilder result = new StringBuilder();
		StringTokenizer tokenizer = new StringTokenizer( text );

		while( tokenizer.hasMoreTokens() ) {
			String token = tokenizer.nextToken();
			int lineLength = line.length();
			int next = lineLength + token.length() + 1;

			if( next <= width ) {
				if( lineLength > 0 ) line.append( " " );
				line.append( token );
			}

			if( next > width || !tokenizer.hasMoreTokens() ) {
				if( result.length() > 0 ) result.append( "\n" );
				result.append( line.toString() );
				line.delete( 0, line.length() );
				line.append( token );
			}
		}

		return result.toString();
	}

}
