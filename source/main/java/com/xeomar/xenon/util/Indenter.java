package com.xeomar.xenon.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.Writer;

public class Indenter {

	public static final String DEFAULT_INDENT_STRING = "\t";

	public static final String createIndent() {
		return DEFAULT_INDENT_STRING;
	}

	public static final String createIndent( int count ) {
		return createIndent( count, DEFAULT_INDENT_STRING );
	}

	public static final String createIndent( int count, String indent ) {
		if( count == 1 ) return indent;

		int size = count * indent.length();
		if( size < 1 ) return "";

		int length = indent.length();
		char[] buffer = new char[size];
		char[] chars = indent.toCharArray();
		for( int index = 0; index < count; index++ ) {
			System.arraycopy( chars, 0, buffer, index * length, length );
		}

		return new String( buffer );
	}

	public static final void writeIndent( Writer writer ) throws IOException {
		writer.write( DEFAULT_INDENT_STRING );
	}

	public static final void writeIndent( Writer writer, int count ) throws IOException {
		writeIndent( writer, count, DEFAULT_INDENT_STRING );
	}

	public static final void writeIndent( Writer writer, String indent ) throws IOException {
		writeIndent( writer, 1, indent );
	}

	public static final void writeIndent( Writer writer, int count, String indent ) throws IOException {
		if( count < 1 ) return;
		writer.write( createIndent( count, indent ) );
	}

	public static final String indent( String content ) {
		return indent( content, 1 );
	}

	public static final String indent( String content, int size ) {
		return indent( content, size, DEFAULT_INDENT_STRING );
	}

	public static final String indent( String content, String indent ) {
		return indent( content, 1, indent );
	}

	public static final String indent( String content, int size, String indent ) {
		if( content == null ) return null;

		String line = null;
		LineParser parser = new LineParser( content );
		StringBuilder builder = new StringBuilder();
		String text = createIndent( size, indent );

		while( ( line = parser.next() ) != null ) {
			builder.append( text );
			builder.append( line );
			builder.append( parser.getTerminator() );
		}

		return builder.toString();
	}

	public static final boolean canUnindent( String content ) {
		return canUnindent( content, 1 );
	}

	public static final boolean canUnindent( String content, boolean ignoreBlanks ) {
		return canUnindent( content, 1, ignoreBlanks );
	}

	public static final boolean canUnindent( String content, int size ) {
		return canUnindent( content, size, DEFAULT_INDENT_STRING );
	}

	public static final boolean canUnindent( String content, int size, boolean ignoreBlanks ) {
		return canUnindent( content, size, DEFAULT_INDENT_STRING, ignoreBlanks );
	}

	public static final boolean canUnindent( String content, int size, String indent ) {
		return canUnindent( content, size, indent, false );
	}

	/**
	 * Check if a block of lines can be unindented. A line can be unindented if it
	 * is empty (no spaces, tabs or other characters before the line termination)
	 * or it starts with the indent string.
	 *
	 * @param content
	 * @param size
	 * @param indent
	 * @return True if all line in the content can be unindented, false otherwise.
	 */
	public static final boolean canUnindent( String content, int size, String indent, boolean ignoreBlanks ) {
		if( content == null ) return false;

		String line = null;
		LineParser parser = new LineParser( content );
		String text = createIndent( size, indent );

		while( ( line = parser.next() ) != null ) {
			if( ignoreBlanks && "".equals( line ) ) continue;
			if( !line.startsWith( text ) ) return false;
		}

		return true;
	}

	public static final String unindent( String content ) {
		return unindent( content, 1 );
	}

	public static final String unindent( String content, int size ) {
		return unindent( content, size, DEFAULT_INDENT_STRING );
	}

	public static final String unindent( String content, int size, String indent ) {
		if( content == null ) return null;

		String line = null;
		LineParser parser = new LineParser( content );
		StringBuilder builder = new StringBuilder();
		String text = createIndent( size, indent );

		while( ( line = parser.next() ) != null ) {
			if( line.startsWith( text ) ) builder.append( line.substring( text.length() ) );
			builder.append( parser.getTerminator() );
		}

		return builder.toString();
	}

	public static final String trim( String content, String trimChars ) {
		if( content == null || content.length() == 0 ) return content;

		int length = content.length();
		int start = 0;
		int end = length - 1;
		char[] value = new char[length];
		char[] trims = new char[trimChars.length()];

		content.getChars( 0, value.length, value, 0 );
		trimChars.getChars( 0, trims.length, trims, 0 );

		char c = 0;
		boolean left = true;
		while( left & start <= end ) {
			c = value[start];
			boolean found = false;
			for( int charIndex = 0; charIndex < trims.length; charIndex++ ) {
				if( c == trims[charIndex] ) found = true;
			}
			left = found;
			if( found ) start++;
		}

		c = 0;
		boolean right = true;
		while( right & end >= start ) {
			c = value[end];
			boolean found = false;
			for( int charIndex = 0; charIndex < trims.length; charIndex++ ) {
				if( c == trims[charIndex] ) found = true;
			}
			right = found;
			if( found ) end--;
		}

		String result = content;
		if( ( start > 0 ) || ( end < length - 1 ) ) {
			result = new String( value, start, end - start + 1 );
		}

		return result;
	}

	public static final String trimLines( String content, String trimChars ) {
		if( content == null || content.length() == 0 ) return content;

		StringBuilder lineBuilder = null;
		StringBuilder builder = new StringBuilder();
		StringReader stringReader = new StringReader( content );
		BufferedReader bufferedReader = new BufferedReader( stringReader );

		int lineNumber = 0;
		try {
			lineNumber++;
			String line = bufferedReader.readLine();
			while( line != null && line.length() == 0 ) {
				builder.append( trim( "\n", trimChars ) );
				line = bufferedReader.readLine();
			}
			if( line != null ) {
				while( line != null ) {
					lineBuilder = new StringBuilder( line );
					lineBuilder.append( "\n" );
					builder.append( trim( lineBuilder.toString(), trimChars ) );
					line = bufferedReader.readLine();
				}
			}
		} catch( IOException exception ) {
			throw new RuntimeException( "Could not unindent at line: " + lineNumber );
		}

		return builder.toString();
	}

}
