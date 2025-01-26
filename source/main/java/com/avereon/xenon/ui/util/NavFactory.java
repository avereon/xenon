package com.avereon.xenon.ui.util;

import lombok.AccessLevel;
import lombok.Getter;

import java.util.*;
import java.util.stream.StreamSupport;

public abstract class NavFactory {

	public static final String SEPARATOR = "|";

	public static final String OPEN_GROUP = "[";

	public static final String CLOSE_GROUP = "]";

	public static final String COMMA_SPLITTER = ",";

	public static final String SPACE_SPLITTER = " ";

	public static final String DELIMITERS = SEPARATOR + OPEN_GROUP + CLOSE_GROUP + COMMA_SPLITTER + SPACE_SPLITTER;

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
		if( next.isOpenGroup() ) {
			queue.poll();
			next = parseToken( queue );
			while( next != null && !next.isCloseGroup() ) {
				token.addToken( next );
				next = parseToken( queue );
			}
		}

		return token;
	}

	@Getter
	public static class Token {

		private final String id;

		private final List<Token> children;

		private final boolean action;

		private final boolean separator;

		@Getter(AccessLevel.PRIVATE)
		private final boolean openGroup;

		@Getter(AccessLevel.PRIVATE)
		private final boolean closeGroup;

		public Token( String id ) {
			this.id = id;
			this.children = new ArrayList<>();

			this.separator = SEPARATOR.equals( id );
			this.openGroup = OPEN_GROUP.equals( id );
			this.closeGroup = CLOSE_GROUP.equals( id );
			this.action = !(separator | openGroup | closeGroup);
		}

		public void addToken( Token token ) {
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
				childString.append( "[" );
				for( Token child : children ) {
					boolean first = childString.toString().isBlank();
					if( !first ) childString.append( ", " );
					childString.append( child.id );
				}
				childString.append( "]" );
			}
			return id + childString;
		}
	}

}
