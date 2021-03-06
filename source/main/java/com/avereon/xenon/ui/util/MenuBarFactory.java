package com.avereon.xenon.ui.util;

import com.avereon.xenon.ActionProxy;
import com.avereon.xenon.Program;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;

import java.util.List;

public class MenuBarFactory extends BarFactory {

	public static final String SHORTCUT_SEPARATOR = "-";

	public static final String MENU_ID_PREFIX = "menu-";

	public static MenuBar createMenuBar( Program program, String descriptor ) {
		List<Token> tokens = parseDescriptor( descriptor );

		MenuBar menubar = new MenuBar();
		for( Token token : tokens ) {
			menubar.getMenus().add( createMenu( program, token, false ) );
		}

		return menubar;
	}

	public static Menu createMenu( Program program, String descriptor, boolean submenu ) {
		return createMenu( program, parseDescriptor( descriptor ).get(0), submenu );
	}

	public static Menu createMenu( Program program, Token token, boolean submenu ) {
		ActionProxy action = program.getActionLibrary().getAction( token.getId() );

		Menu menu = new Menu();
		menu.setId( MENU_ID_PREFIX + action.getId() );
		menu.getStyleClass().add( MENU_ID_PREFIX + action.getId() );
		menu.setMnemonicParsing( true );
		menu.setText( action.getMnemonicName() );
		if( submenu ) menu.setGraphic( program.getIconLibrary().getIcon( action.getIcon() ) );
		//item.setAccelerator( parseShortcut( action.getShortcut() ) );

		action.mnemonicNameProperty().addListener( ( event ) -> menu.setText( action.getName() ) );

		for( Token child : token.getChildren() ) {
			menu.getItems().add( createMenuItem( program, child ) );
		}

		return menu;
	}

	private static MenuItem createMenuItem( Program program, Token item ) {
		if( item.isSeparator() ) {
			return new SeparatorMenuItem();
		} else if( item.getChildren().isEmpty() ) {
			return createMenuItem( program, program.getActionLibrary().getAction( item.getId() ) );
		} else {
			return createMenu( program, item, true );
		}
	}

	private static MenuItem createMenuItem( Program program, ActionProxy action ) {
		String type = action.getType();
		if( type == null ) type = "normal";

		MenuItem item;
		switch( type ) {
			case "checkbox": {
				item = new CheckMenuItem();
				break;
			}
			default: {
				item = new MenuItem();
				break;
			}
		}

		item.setId( "menuitem-" + action.getId() );
		item.setOnAction( action );
		item.setMnemonicParsing( true );
		item.setDisable( !action.isEnabled() );
		item.setText( action.getMnemonicName() );
		item.setGraphic( program.getIconLibrary().getIcon( action.getIcon() ) );
		item.setAccelerator( parseShortcut( action.getShortcut() ) );

		action.enabledProperty().addListener( ( event ) -> item.setDisable( !action.isEnabled() ) );
		action.mnemonicNameProperty().addListener( ( event ) -> item.setText( action.getName() ) );
		action.iconProperty().addListener( ( event ) -> item.setGraphic( program.getIconLibrary().getIcon( action.getIcon() ) ) );

		return item;
	}

	public static KeyCombination parseShortcut( String shortcut ) {
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
