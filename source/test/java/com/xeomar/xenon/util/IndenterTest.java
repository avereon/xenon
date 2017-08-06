package com.xeomar.xenon.util;

import junit.framework.TestCase;

import java.io.IOException;
import java.io.StringWriter;

public class IndenterTest extends TestCase {

	// For convenience.
	private static final String IND = Indenter.DEFAULT_INDENT_STRING;

	public void testCreateIndent() {
		assertEquals( IND, Indenter.createIndent() );
	}

	public void testCreateIndentWithSize() {
		assertEquals( "", Indenter.createIndent( -1 ) );
		assertEquals( "", Indenter.createIndent( 0 ) );
		assertEquals( IND + "", Indenter.createIndent( 1 ) );
		assertEquals( IND + "" + IND + "", Indenter.createIndent( 2 ) );
		assertEquals( IND + "" + IND + "" + IND + "", Indenter.createIndent( 3 ) );
	}

	public void testCreateIndentWithString() {
		assertEquals( "", Indenter.createIndent( -1, "<>" ) );
		assertEquals( "", Indenter.createIndent( 0, "<>" ) );
		assertEquals( "<>", Indenter.createIndent( 1, "<>" ) );
		assertEquals( "<><>", Indenter.createIndent( 2, "<>" ) );
		assertEquals( "<><><>", Indenter.createIndent( 3, "<>" ) );
	}

	public void testWriteIndent() throws Exception {
		StringWriter writer = new StringWriter();
		Indenter.writeIndent( writer );
		assertEquals( IND, writer.toString() );
	}

	public void testWriteIndentWithSize() throws Exception {
		assertWriteIndent( "", -1 );
		assertWriteIndent( "", 0 );
		assertWriteIndent( IND + "", 1 );
		assertWriteIndent( IND + "" + IND + "", 2 );
		assertWriteIndent( IND + "" + IND + "" + IND + "", 3 );
	}

	public void testWriteIndentWithString() throws Exception {
		assertWriteIndentWithString( "<>", "<>" );
		assertWriteIndentWithString( "<><>", "<><>" );
		assertWriteIndentWithString( "<><><>", "<><><>" );
	}

	public void testWriteIndentWithSizeAndString() throws Exception {
		assertWriteIndentWithSizeAndString( "", -1, "<>" );
		assertWriteIndentWithSizeAndString( "", 0, "<>" );
		assertWriteIndentWithSizeAndString( "<>", 1, "<>" );
		assertWriteIndentWithSizeAndString( "<><>", 2, "<>" );
		assertWriteIndentWithSizeAndString( "<><><>", 3, "<>" );
	}

	public void testIndent() {
		assertEquals( null, Indenter.indent( null ) );

		assertEquals( IND, Indenter.indent( "" ) );
		assertEquals( IND + "a", Indenter.indent( "a" ) );

		assertEquals( IND + "\n" + IND, Indenter.indent( "\n" ) );
		assertEquals( IND + "a\n" + IND + "a", Indenter.indent( "a\na" ) );

		assertEquals( IND + "\n" + IND + "\n" + IND, Indenter.indent( "\n\n" ) );
		assertEquals( IND + "a\n" + IND + "a\n" + IND + "a", Indenter.indent( "a\na\na" ) );
	}

	public void testIndentWithSize() {
		assertEquals( null, Indenter.indent( null, 2 ) );

		assertEquals( IND + IND, Indenter.indent( "", 2 ) );
		assertEquals( IND + IND + "a", Indenter.indent( "a", 2 ) );

		assertEquals( IND + IND + "\n" + IND + IND, Indenter.indent( "\n", 2 ) );
		assertEquals( IND + IND + "a\n" + IND + IND + "a", Indenter.indent( "a\na", 2 ) );

		assertEquals( IND + IND + "\n" + IND + IND + "\n" + IND + IND, Indenter.indent( "\n\n", 2 ) );
		assertEquals( IND + IND + "a\n" + IND + IND + "a\n" + IND + IND + "a", Indenter.indent( "a\na\na", 2 ) );
	}

	public void testIndentWithString() {
		assertEquals( null, Indenter.indent( null, "<>" ) );

		assertEquals( "<>", Indenter.indent( "", "<>" ) );
		assertEquals( "<>a", Indenter.indent( "a", "<>" ) );

		assertEquals( "<>\n<>", Indenter.indent( "\n", "<>" ) );
		assertEquals( "<>a\n<>a", Indenter.indent( "a\na", "<>" ) );

		assertEquals( "<>\n<>\n<>", Indenter.indent( "\n\n", "<>" ) );
		assertEquals( "<>a\n<>a\n<>a", Indenter.indent( "a\na\na", "<>" ) );
	}

	public void testIndentWithSizeAndString() {
		assertEquals( null, Indenter.indent( null, 1, "<>" ) );

		assertEquals( "<><><>", Indenter.indent( "", 3, "<>" ) );
		assertEquals( "<><><>a", Indenter.indent( "a", 3, "<>" ) );

		assertEquals( "<><><>\n<><><>", Indenter.indent( "\n", 3, "<>" ) );
		assertEquals( "<><><>a\n<><><>a", Indenter.indent( "a\na", 3, "<>" ) );

		assertEquals( "<><><>\n<><><>\n<><><>", Indenter.indent( "\n\n", 3, "<>" ) );
		assertEquals( "<><><>a\n<><><>a\n<><><>a", Indenter.indent( "a\na\na", 3, "<>" ) );
	}

	public void testCanUnindent() {
		assertFalse( Indenter.canUnindent( null ) );

		assertFalse( Indenter.canUnindent( "a" + IND ) );
		assertFalse( Indenter.canUnindent( IND + "a\nb" + IND ) );
		assertFalse( Indenter.canUnindent( IND + "a\n" + IND + "b\nc" + IND ) );

		assertFalse( Indenter.canUnindent( "" ) );
		assertFalse( Indenter.canUnindent( IND + "\n" ) );
		assertFalse( Indenter.canUnindent( IND + "\n" + IND + "\n" ) );
		assertFalse( Indenter.canUnindent( IND + "\n" + IND + "\n" + IND + "\n" ) );

		assertTrue( Indenter.canUnindent( "", true ) );
		assertTrue( Indenter.canUnindent( IND + "\n", true ) );
		assertTrue( Indenter.canUnindent( IND + "\n" + IND + "\n", true ) );
		assertTrue( Indenter.canUnindent( IND + "\n" + IND + "\n" + IND + "\n", true ) );

		assertTrue( Indenter.canUnindent( IND + "a" ) );
		assertTrue( Indenter.canUnindent( IND + "a\n" + IND + "b" ) );
		assertTrue( Indenter.canUnindent( IND + "a\n" + IND + "b\n" + IND + "c" ) );
	}

	public void testCanUnindentWithSize() {
		assertFalse( Indenter.canUnindent( null ) );

		assertFalse( Indenter.canUnindent( "a" + IND + IND + "", 2 ) );
		assertFalse( Indenter.canUnindent( IND + IND + "a\nb" + IND + IND + "", 2 ) );
		assertFalse( Indenter.canUnindent( IND + IND + "a\n" + IND + IND + "b\nc" + IND + IND + "", 2 ) );

		assertFalse( Indenter.canUnindent( "", 2 ) );
		assertFalse( Indenter.canUnindent( IND + IND + "\n", 2 ) );
		assertFalse( Indenter.canUnindent( IND + IND + "\n" + IND + IND + "\n", 2 ) );
		assertFalse( Indenter.canUnindent( IND + IND + "\n" + IND + IND + "\n" + IND + IND + "\n", 2 ) );

		assertTrue( Indenter.canUnindent( "", 2, true ) );
		assertTrue( Indenter.canUnindent( IND + IND + "\n", 2, true ) );
		assertTrue( Indenter.canUnindent( IND + IND + "\n" + IND + IND + "\n", 2, true ) );
		assertTrue( Indenter.canUnindent( IND + IND + "\n" + IND + IND + "\n" + IND + IND + "\n", 2, true ) );

		assertTrue( Indenter.canUnindent( IND + IND + "a", 2 ) );
		assertTrue( Indenter.canUnindent( IND + IND + "a\n" + IND + IND + "b", 2 ) );
		assertTrue( Indenter.canUnindent( IND + IND + "a\n" + IND + IND + "b\n" + IND + IND + "c", 2 ) );
	}

	public void testCanUnindentWithString() {
		assertFalse( Indenter.canUnindent( null ) );

		assertFalse( Indenter.canUnindent( "a<><><>", 3, "<>" ) );
		assertFalse( Indenter.canUnindent( "<><><>a\nb<><><>", 3, "<>" ) );
		assertFalse( Indenter.canUnindent( "<><><>a\n<><><>b\nc<><><>", 3, "<>" ) );

		assertFalse( Indenter.canUnindent( "", 3, "<>" ) );
		assertFalse( Indenter.canUnindent( "<><><>\n", 3, "<>" ) );
		assertFalse( Indenter.canUnindent( "<><><>\n" + "<><><>\n", 3, "<>" ) );
		assertFalse( Indenter.canUnindent( "<><><>\n" + "<><><>\n" + "<><><>\n", 3, "<>" ) );

		assertTrue( Indenter.canUnindent( "", 3, "<>", true ) );
		assertTrue( Indenter.canUnindent( "<><><>\n", 3, "<>", true ) );
		assertTrue( Indenter.canUnindent( "<><><>\n" + "<><><>\n", 3, "<>", true ) );
		assertTrue( Indenter.canUnindent( "<><><>\n" + "<><><>\n" + "<><><>\n", 3, "<>", true ) );

		assertTrue( Indenter.canUnindent( "<><><>a", 3, "<>" ) );
		assertTrue( Indenter.canUnindent( "<><><>a\n" + "<><><>b", 3, "<>" ) );
		assertTrue( Indenter.canUnindent( "<><><>a\n" + "<><><>b\n" + "<><><>c", 3, "<>" ) );
	}

	public void testUnindent() {
		assertNull( Indenter.unindent( null ) );

		assertEquals( "", Indenter.unindent( "" ) );
		assertEquals( "\n", Indenter.unindent( "\n" ) );

		assertEquals( "", Indenter.unindent( IND ) );
		assertEquals( "a", Indenter.unindent( IND + "a" ) );

		assertEquals( "\n", Indenter.unindent( IND + "\n" + IND ) );
		assertEquals( "a\nb", Indenter.unindent( IND + "a\n" + IND + "b" ) );

		assertEquals( "\n\n", Indenter.unindent( IND + "\n" + IND + "\n" + IND ) );
		assertEquals( "a\nb\nc", Indenter.unindent( IND + "a\n" + IND + "b\n" + IND + "c" ) );
	}

	public void testUnindentWithSize() {
		assertNull( Indenter.unindent( null, 2 ) );

		assertEquals( "", Indenter.unindent( "", 2 ) );
		assertEquals( "\n", Indenter.unindent( "\n", 2 ) );

		assertEquals( "", Indenter.unindent( IND + IND, 2 ) );
		assertEquals( "a", Indenter.unindent( IND + IND + "a", 2 ) );

		assertEquals( "\n", Indenter.unindent( IND + IND + "\n" + IND + IND, 2 ) );
		assertEquals( "a\nb", Indenter.unindent( IND + IND + "a\n" + IND + IND + "b", 2 ) );

		assertEquals( "\n\n", Indenter.unindent( IND + IND + "\n" + IND + IND + "\n" + IND + IND, 2 ) );
		assertEquals( "a\nb\nc", Indenter.unindent( IND + IND + "a\n" + IND + IND + "b\n" + IND + IND + "c", 2 ) );
	}

	public void testUnindentWithString() {
		assertNull( Indenter.unindent( null, 3, "<>" ) );

		assertEquals( "", Indenter.unindent( "", 3, "<>" ) );
		assertEquals( "\n", Indenter.unindent( "\n", 3, "<>" ) );

		assertEquals( "", Indenter.unindent( "<><><>", 3, "<>" ) );
		assertEquals( "a", Indenter.unindent( "<><><>a", 3, "<>" ) );

		assertEquals( "\n", Indenter.unindent( "<><><>\n<><><>", 3, "<>" ) );
		assertEquals( "a\nb", Indenter.unindent( "<><><>a\n<><><>b", 3, "<>" ) );

		assertEquals( "\n\n", Indenter.unindent( "<><><>\n<><><>\n<><><>", 3, "<>" ) );
		assertEquals( "a\nb\nc", Indenter.unindent( "<><><>a\n<><><>b\n<><><>c", 3, "<>" ) );
	}

	public void testTrim() {
		assertNull( Indenter.trim( null, null ) );
		assertEquals( "", Indenter.trim( "", "" ) );
		assertEquals( "b", Indenter.trim( "abc", "ac" ) );
	}

	public void testTrimLines() {
		assertNull( Indenter.trimLines( null, null ) );
		assertEquals( "", Indenter.trimLines( "", "" ) );

		//assertEquals( "bc", Indenter.trimLines( "abc", "ac" ) );
	}

	private void assertWriteIndent( String result, int size ) throws IOException {
		StringWriter writer = new StringWriter();
		Indenter.writeIndent( writer, size );
		assertEquals( result, writer.toString() );
	}

	private void assertWriteIndentWithString( String result, String text ) throws IOException {
		StringWriter writer = new StringWriter();
		Indenter.writeIndent( writer, text );
		assertEquals( result, writer.toString() );
	}

	private void assertWriteIndentWithSizeAndString( String result, int size, String text ) throws IOException {
		StringWriter writer = new StringWriter();
		Indenter.writeIndent( writer, size, text );
		assertEquals( result, writer.toString() );
	}

}
