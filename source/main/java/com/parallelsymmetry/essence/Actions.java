package com.parallelsymmetry.essence;

import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;

public class Actions {

	public static final String SHORTCUT_SEPARATOR = "-";

	public static Menu createMenu( Program program, String id ) {
		return createMenu( program.getActionLibrary().getAction( id ) );
	}

	public static Menu createMenu( ActionProxy action ) {
		Menu item = new Menu();

		item.setMnemonicParsing( true );
		item.setText( action.getName() );
		//item.setGraphic( action.getIcon() );
		//item.setAccelerator( parseShortcut( action.getShortcut() ) );

		action.getMnemonicNameValue().addListener( ( event ) -> item.setText( action.getName() ) );

		return item;
	}

	public static MenuItem createMenuItem( Program program, String id ) {
		return createMenuItem( program.getActionLibrary().getAction( id ) );
	}

	public static MenuItem createMenuItem( ActionProxy action ) {
		MenuItem item = new MenuItem();

		item.setMnemonicParsing( true );
		item.setText( action.getName() );
		item.setGraphic( action.getIcon() );
		item.setAccelerator( parseShortcut( action.getShortcut() ) );

		action.getMnemonicNameValue().addListener( ( event ) -> item.setText( action.getName() ) );

		return item;
	}

	private static KeyCombination parseShortcut( String shortcut ) {
		if( shortcut == null ) return null;

		shortcut = shortcut.toUpperCase();

		System.out.println( "Parsing shortcut: " + shortcut );

		// Split the shortcut if it contains a dash
		String key = shortcut;
		String modifiers = "";
		if( shortcut.contains( SHORTCUT_SEPARATOR ) ) {
			modifiers = shortcut.substring( 0, shortcut.indexOf( SHORTCUT_SEPARATOR ) );
			key = shortcut.substring( shortcut.indexOf( SHORTCUT_SEPARATOR ) +1 );
		}
		if( "".equals( key ) ) return null;
		if( "".equals( modifiers ) ) new KeyCodeCombination( KeyCode.getKeyCode( key ) );

		KeyCombination.Modifier[] modifierList = new KeyCombination.Modifier[ modifiers.length() ];
		int index = 0;
		for( char modifierLetter : modifiers.toCharArray() ) {
			switch( modifierLetter ) {
				case 'C': {
					modifierList[ index ] = KeyCombination.CONTROL_DOWN;
					break;
				}
				case 'A': {
					modifierList[ index ] = KeyCombination.ALT_DOWN;
					break;
				}
				case 'S': {
					modifierList[ index ] = KeyCombination.SHIFT_DOWN;
					break;
				}
				case 'M': {
					modifierList[ index ] = KeyCombination.META_DOWN;
					break;
				}
				case 'T': {
					modifierList[ index ] = KeyCombination.SHORTCUT_DOWN;
					break;
				}
			}
			index++;
		}

		System.out.println( "Key code: " + KeyCode.getKeyCode( key ) );

		return new KeyCodeCombination( KeyCode.getKeyCode( key ), modifierList );
	}

}
