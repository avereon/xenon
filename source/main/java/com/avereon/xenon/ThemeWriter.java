package com.avereon.xenon;

import com.avereon.venza.color.Colors;
import javafx.scene.paint.Color;

import java.io.PrintWriter;
import java.io.Writer;

/**
 * Take some number of colors, 3-6, and generate a Xenon compatible theme.
 */
public class ThemeWriter {

	private Color a;

	private Color b;

	private Color c;

	private Color d;

	private Color e;

	private Color f;

	public ThemeWriter( Color a ) {
		this( a, null );
	}

	public ThemeWriter( Color a, Color b ) {
		this( a, b, null );
	}

	public ThemeWriter( Color a, Color b, Color c ) {
		this( a, b, c, null );
	}

	public ThemeWriter( Color a, Color b, Color c, Color d ) {
		this( a, b, c, d, null );
	}

	public ThemeWriter( Color a, Color b, Color c, Color d, Color e ) {
		this( a, b, c, d, e, null );
	}

	public ThemeWriter( Color a, Color b, Color c, Color d, Color e, Color f ) {
		this.a = a;
		this.b = b;
		this.c = c;
		this.d = d;
		this.e = e;
		this.f = f;
	}

	public void write( String id, String name, Writer writer ) {
		PrintWriter printer = new PrintWriter( writer );

		writeHeader( id, name, printer );
		printer.println( ".root {" );
		writeFxBase( printer );
		writeFxColor( printer );
		writeFxBackground( printer );
		printer.println( "}" );
	}

	private void writeHeader( String id, String name, PrintWriter printer ) {
		printer.println( "/*" );
		printer.println( " * id=" + id );
		printer.println( " * name=" + name );
		printer.println( " */" );
		printer.println();
	}

	/*
	 * The base color for "objects". Instead of using -fx-base directly the
	 * theme will typically use -fx-color.
	 */
	private void writeFxBase( PrintWriter printer ) {
		printer.println( "  -fx-base: " + format( a ) + ";" );
	}

	/*
	 * The color that is used to style controls. The default value is based
	 * on -fx-base, but is changed by pseudo-classes to change the base color.
	 * For example, the "hover" pseudo-class will typically set -fx-color to
	 * -fx-hover-base (see below) and the "armed" pseudo-class will typically
	 * set -fx-color to -fx-pressed-base.
	 */
	private void writeFxColor( PrintWriter printer ) {
		printer.println( "  -fx-color: -fx-base;" );
	}

	/*
	 * The background of windows.  See also -fx-text-background-color, which
	 * should be used as the -fx-text-fill value for text painted on top of
	 * -fx-background.
	 */
	private void writeFxBackground( PrintWriter printer ) {
		// If the base is dark use -25%, otherwise use use +25%
		String offset = Colors.getLuminance( a ) < 0.5 ? "-25%" : "25%";
		printer.println( "  -fx-background: derive(-fx-base, " + offset + ");" );
	}

	private String format( Color color ) {
		return color.toString().replace( "0x", "#" ).toUpperCase();
	}

}
