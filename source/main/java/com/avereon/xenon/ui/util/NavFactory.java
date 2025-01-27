package com.avereon.xenon.ui.util;

import lombok.Getter;
import lombok.Setter;

import java.util.*;
import java.util.stream.StreamSupport;

public abstract class NavFactory {

	public static final String SEPARATOR = "|";

	public static final String MENU_OPEN = "{";

	public static final String MENU_CLOSE = "}";

	public static final String TRAY_OPEN = "[";

	public static final String TRAY_CLOSE = "]";

	public static final String COMMA_SPLITTER = ",";

	public static final String SPACE_SPLITTER = " ";

	public static final String DELIMITERS = SEPARATOR + MENU_OPEN + MENU_CLOSE + TRAY_OPEN + TRAY_CLOSE + COMMA_SPLITTER + SPACE_SPLITTER;

	/**
	 * <p>
	 * Parse a bar descriptor string into groups of tokens that can be
	 * interpreted into a menu bar or toolbar. The descriptor is a comma
	 * separated list of tokens. A token is an action key or an action key and a
	 * list of child tokens.
	 * </p>
	 * <p>
	 * For example, the <code>&quot;paste&quot;</code> identifier can be a
	 * token all by itself. Here is an example of a simple edit menu with a list
	 * of child tokens: <code>&quot;edit[cut,copy,paste]&quot;</code>
	 * </p>
	 *
	 * @param descriptor The bar descriptor
	 * @return The bar tokens
	 */
	public static List<Token> parseDescriptor( String descriptor ) {
		List<String> sTokens = StreamSupport
			.stream( Spliterators.spliteratorUnknownSize( new StringTokenizer( descriptor, DELIMITERS, true ).asIterator(), Spliterator.ORDERED ), false )
			.map( String::valueOf )
			.filter( s -> !COMMA_SPLITTER.equals( s ) )
			.filter( s -> !SPACE_SPLITTER.equals( s ) )
			.toList();
		return parseTokens( new LinkedList<>( sTokens ) );
	}

	private static List<Token> parseTokens( Queue<String> queue ) {
		List<Token> tokens = new ArrayList<>();

		Token token;
		while( (token = parseToken( queue )) != null ) {
			tokens.add( token );
		}

		return tokens;
	}

	private static Token parseToken( Queue<String> queue ) {
		if( queue.isEmpty() ) return null;

		Token token = new Token( queue.poll() );

		Token next = new Token( queue.peek() );

		if( next.isOpenToken() ) {
			// Set the parent token type to the type of the open token
			token.type = next.type;
			queue.poll();
			next = parseToken( queue );
			while( next != null && !next.isCloseToken() ) {
				token.addChildToken( next );
				next = parseToken( queue );
			}
		}

		return token;
	}

	@Getter
	public static class Token {

		public enum Type {
			ACTION,
			MENU,
			SEPARATOR,
			TRAY
		}

		private final String id;

		private final List<Token> children;

		private final boolean openToken;

		private final boolean closeToken;

		@Setter
		private Type type;

		public Token( String id ) {
			this.id = id;
			this.type = Type.ACTION;
			this.children = new ArrayList<>();

			this.openToken = TRAY_OPEN.equals( id ) || MENU_OPEN.equals( id );
			this.closeToken = TRAY_CLOSE.equals( id ) || MENU_CLOSE.equals( id );

			if( SEPARATOR.equals( id ) ) type = Type.SEPARATOR;
			if( TRAY_OPEN.equals( id ) || TRAY_CLOSE.equals( id ) ) type = Type.TRAY;
			if( MENU_OPEN.equals( id ) || MENU_CLOSE.equals( id ) ) type = Type.MENU;
		}

		public void addChildToken( Token token ) {
			this.children.add( token );
		}

		@Override
		public int hashCode() {
			return id.hashCode();
		}

		@Override
		public boolean equals( Object object ) {
			if( !(object instanceof Token that) ) return false;
			return Objects.equals( this.id, that.id );
		}

		@Override
		public String toString() {
			StringBuilder childString = new StringBuilder();
			if( !children.isEmpty() ) {
				String open = switch( type ) {
					case MENU -> MENU_OPEN;
					case TRAY -> TRAY_OPEN;
					default -> "";
				};
				String close = switch( type ) {
					case MENU -> MENU_CLOSE;
					case TRAY -> TRAY_CLOSE;
					default -> "";
				};
				childString.append( open );
				for( Token child : children ) {
					boolean first = childString.length() == 1;
					if( !first ) childString.append( "," );
					childString.append( child.id );
				}
				childString.append( close );
			}
			return id + childString;
		}

	}

}
