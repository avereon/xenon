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

	private boolean isDark;

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

		isDark = Colors.getLuminance( a ) < 0.5;
	}

	public void write( String id, String name, Writer writer ) {
		PrintWriter printer = new PrintWriter( writer );

		printHeader( id, name, printer );
		printRoot( printer );
		//printTextInput( printer );
		//printScrollBarArrows( printer );
	}

	private void printHeader( String id, String name, PrintWriter printer ) {
		printer.println( "/*" );
		printer.println( " * id=" + id );
		printer.println( " * name=" + name );
		printer.println( " */" );
		printer.println();
	}

	private void printRoot( PrintWriter printer ) {
		printer.println( ".root {" );
		printFxBase( printer );

		printFxBackground( printer );
		printFxControlInnerBackground( printer );
		printFxControlInnerBackgroundAlt( printer );

		printFxColor( printer );

		printFxAccent( printer );
		printFxDefaultButton( printer );

		printFxFocusColor( printer );
		printFxFaintFocusColor( printer );

		printFxSelectionBarNonFocused( printer );

		printExBackgrounds( printer );

		printExWorkareaTintColor( printer );
		printExWorkareaDropHintColor( printer );
		printer.println( "}" );
	}

	private void printTextInput( PrintWriter printer ) {
		printer.println( ".text-input {" );
		printer.println( derive( "-fx-prompt-text-fill", "-fx-control-inner-background", "50%", "-30%" ) );
		printer.println( "}" );
		printer.println( ".text-input {" );
		printer.println( "  -fx-prompt-text-fill: transparent;" );
		printer.println( "}" );
	}

	private void printScrollBarArrows( PrintWriter printer ) {
		printer.println( ".scroll-bar > .increment-button > .increment-arrow," );
		printer.println( ".scroll-bar > .decrement-button > .decrement-arrow {" );
		printer.println( "  -fx-background-color: -fx-mark-highlight-color, " + derive( "-fx-base", "45%", "-45%") + ";" );
		printer.println( "}" );

		printer.println( ".scroll-bar > .increment-button:hover > .increment-arrow," );
		printer.println( ".scroll-bar > .decrement-button:hover > .decrement-arrow {" );
		printer.println( "  -fx-background-color: -fx-mark-highlight-color, " + derive( "-fx-base", "50%", "-50%") + ";" );
		printer.println( "}" );

		printer.println( ".scroll-bar > .increment-button:pressed > .increment-arrow," );
		printer.println( ".scroll-bar > .decrement-button:pressed > .decrement-arrow {" );
		printer.println( "  -fx-background-color: -fx-mark-highlight-color, " + derive( "-fx-base", "55%", "-55%") + ";" );
		printer.println( "}" );
	}

	/**
	 * The base color for "objects". Instead of using -fx-base directly the
	 * theme will typically use -fx-color.
	 *
	 * @param printer The print writer
	 */
	private void printFxBase( PrintWriter printer ) {
		printer.println( "  -fx-base: " + format( a ) + ";" );
	}

	/**
	 * The background of windows.  See also -fx-text-background-color, which
	 * should be used as the -fx-text-fill value for text painted on top of
	 * -fx-background.
	 *
	 * @param printer The print writer
	 */
	private void printFxBackground( PrintWriter printer ) {
		printer.println( derive( "-fx-background", "-fx-base", "-25%", "25%" ) );
	}

	/**
	 * Used for the inside of text boxes, password boxes, lists, trees, and
	 * tables.  See also -fx-text-inner-color, which should be used as the
	 * -fx-text-fill value for text painted on top of backgrounds colored with
	 * -fx-control-inner-background.
	 *
	 * @param printer The print writer
	 * @see #printFxControlInnerBackgroundAlt(PrintWriter)
	 */
	private void printFxControlInnerBackground( PrintWriter printer ) {
		printer.println( derive( "-fx-control-inner-background", "-fx-base", "-50%", "80%" ) );
	}

	/**
	 * Version of -fx-control-inner-background for alternative rows.
	 *
	 * @param printer The print writer
	 * @see #printFxControlInnerBackground(PrintWriter)
	 */
	private void printFxControlInnerBackgroundAlt( PrintWriter printer ) {
		printer.println( derive( "-fx-control-inner-background-alt", "-fx-control-inner-background", "2.5%", "-2%" ) );
	}

	/**
	 * The color that is used to style controls. The default value is based
	 * on -fx-base, but is changed by pseudo-classes to change the base color.
	 * For example, the "hover" pseudo-class will typically set -fx-color to
	 * -fx-hover-base (see below) and the "armed" pseudo-class will typically
	 * set -fx-color to -fx-pressed-base.
	 *
	 * @param printer The print writer
	 */
	private void printFxColor( PrintWriter printer ) {
		printer.println( derive( "-fx-color", "-fx-base", "10%", "0%" ) );
	}

	/**
	 * The color for highlighting/accenting objects.  For example: selected text,
	 * selected items in menus, lists, progress bars, trees and tables.
	 *
	 * @param printer The print writer
	 */
	private void printFxAccent( PrintWriter printer ) {
		printer.println( "  -fx-accent: " + format( b ) + ";" );
	}

	/**
	 * The color for default buttons.
	 *
	 * @param printer The print writer
	 */
	private void printFxDefaultButton( PrintWriter printer ) {
		printer.println( derive( "-fx-default-button", "-fx-accent", "-40%", "40%" ) );
	}

	/**
	 * The focus indicator of objects. Typically used as the first color in
	 * -fx-background-color for the "focused" pseudo-class. Also used with insets
	 * to provide a glowing effect.
	 *
	 * @param printer The print writer
	 */
	private void printFxFocusColor( PrintWriter printer ) {
		printer.println( "  -fx-focus-color: " + format( c ) + ";" );
	}

	/**
	 * The faint focus color.
	 *
	 * @param printer The print writer
	 * @see #printFxFocusColor(PrintWriter)
	 */
	private void printFxFaintFocusColor( PrintWriter printer ) {
		printer.println( "  -fx-faint-focus-color: " + format( c, 0.25 ) + ";" );
	}

	/**
	 * Background color to use for selection of list cells, table cells, tree
	 * cells, etc., when the control doesn't have focus or is a row of a
	 * previously selected item.
	 *
	 * @param printer The print writer
	 */
	private void printFxSelectionBarNonFocused( PrintWriter printer ) {
		printer.println( "  -fx-selection-bar-non-focused: " + (isDark ? "#303030" : "#D0D0D0") + ";" );
	}

	/**
	 * The background colors for various workarea components that overlay the
	 * background.
	 *
	 * @param printer The print writer
	 */
	private void printExBackgrounds( PrintWriter printer ) {
		// -fx-base tone with alpha
		Color base = isDark ? Colors.getTone( a, 0.2 ) : Colors.getTone( a, -0.2 );
		printer.println( "  -ex-background-text: " + format( base.deriveColor( 0, 1, 1, 0.2 ) ) + ";" );
		printer.println( "  -ex-background-tabs: " + format( base.deriveColor( 0, 1, 1, 0.4 ) ) + ";" );
		printer.println( "  -ex-background-tags: " + format( base.deriveColor( 0, 1, 1, 0.6 ) ) + ";" );
		printer.println( "  -ex-background-note: " + format( base.deriveColor( 0, 1, 1, 0.8 ) ) + ";" );
	}

	/**
	 * The tint color for the workarea. This color is used to overlay the
	 * workarea background and can be changed in the settings tool.
	 *
	 * @param printer The print writer
	 */
	private void printExWorkareaTintColor( PrintWriter printer ) {
		printer.println( "  -ex-workspace-tint-color: " + format( a.deriveColor( 0, 1, 1, 0.5 ) ) + ";" );
	}

	private void printExWorkareaDropHintColor( PrintWriter printer ) {
		Color hint = (isDark ? Color.WHITE : Color.BLACK).deriveColor( 0, 1, 1, 0.2 );
		printer.println( "  -ex-workspace-drop-hint: " + format( hint ) + ";" );
	}

	private String derive( String key, String source, String dark, String light ) {
		return "  " + key + ": " + derive( source, dark, light ) + ";";
	}

	private String derive( String source, String dark, String light ) {
		return "derive(" + source + ", " + (isDark ? dark : light) + ")";
	}

	private String format( Color color ) {
		return color.toString().replace( "0x", "#" ).toUpperCase();
	}

	private String format( Color color, double alpha ) {
		return format( color.deriveColor( 0, 1, 1, alpha ) );
	}

}
