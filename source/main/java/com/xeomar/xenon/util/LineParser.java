package com.xeomar.xenon.util;

public class LineParser {

	private String content;

	private int length;

	private int next;

	private String line;

	private String term;

	public LineParser( String content ) {
		this.content = content;
		this.length = content == null ? -1 : content.length();
	}

	public boolean more() {
		return next <= length;
	}

	public String next() {
		parseNextLine();
		return line;
	}

	public String getLine() {
		return line;
	}

	public String getTerminator() {
		return term;
	}

	public String getRemaining() {
		return content.substring( next );
	}

	private void parseNextLine() {
		if( !more() ) {
			line = null;
			term = null;
			return;
		}

		int index = next;

		int c = index >= length ? -1 : content.charAt( index );
		while( c != '\n' && c != '\r' && c != -1 ) {
			index++;
			c = index >= length ? -1 : content.charAt( index );
		}

		int start = next;
		next = index;

		// Leave index at the end of the line and move next to the next line.
		if( c == '\r' ) {
			int c2 = index + 1 >= length ? -1 : content.charAt( index + 1 );
			next += c2 == '\n' ? 2 : 1;
		} else {
			next++;
		}

		line = content.substring( start, index );
		term = content.substring( index, next < length ? next : length );
	}

}
