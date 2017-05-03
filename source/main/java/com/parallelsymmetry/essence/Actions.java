package com.parallelsymmetry.essence;

import javafx.scene.control.Button;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

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

		item.setOnAction( action );
		item.setMnemonicParsing( true );
		item.setText( action.getName() );
		item.setGraphic( action.getIcon() );
		item.setAccelerator( parseShortcut( action.getShortcut() ) );

		action.getMnemonicNameValue().addListener( ( event ) -> item.setText( action.getName() ) );

		return item;
	}

	public static Button createToolBarButton( Program program, String id ) {
		return createToolBarButton( program.getActionLibrary().getAction( id ) );
	}

	public static Button createToolBarButton( ActionProxy action ) {
		Button button = new Button();

		button.setOnAction( action );
		button.setGraphic( action.getIcon() );

		return button;
	}

	public static Region createSpring() {
		Region spring = new Region();
		HBox.setHgrow( spring, Priority.ALWAYS );
		return spring;
	}

	private static KeyCombination parseShortcut( String shortcut ) {
		if( shortcut == null ) return null;

		// Make sure the shortcut definition is in upper case
		shortcut = shortcut.toUpperCase();

		// Split the shortcut if it contains a dash
		String key = shortcut;
		String modifiers = "";
		if( shortcut.contains( SHORTCUT_SEPARATOR ) ) {
			modifiers = shortcut.substring( 0, shortcut.indexOf( SHORTCUT_SEPARATOR ) );
			key = shortcut.substring( shortcut.indexOf( SHORTCUT_SEPARATOR ) + 1 );
		}

		// If there is no key, there is no shortcut
		if( "".equals( key ) ) return null;

		// If there are no modifiers, the shortcut is just a key
		if( "".equals( modifiers ) ) new KeyCodeCombination( KeyCode.valueOf( key ) );

		// If there are modifiers, then it's a compound key combination
		return new KeyCodeCombination( KeyCode.valueOf( key ), parseModifiers( modifiers ) );
	}

	private static KeyCombination.Modifier[] parseModifiers( String modifiers ) {
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
		return modifierList;
	}

}
