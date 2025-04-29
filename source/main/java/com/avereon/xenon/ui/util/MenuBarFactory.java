package com.avereon.xenon.ui.util;

import com.avereon.xenon.ActionProxy;
import com.avereon.xenon.Xenon;
import com.avereon.xenon.workspace.Workarea;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.StackPane;
import lombok.CustomLog;

import java.util.ArrayList;
import java.util.List;

@CustomLog
public class MenuBarFactory extends NavFactory {

	public static final String SHORTCUT_SEPARATOR = "-";

	public static final String MENU_ID_PREFIX = "menu-";

	public static final String MENU_ITEM_ID_PREFIX = "menu-item-";

	public static final String MENU_BUTTON_ID_PREFIX = "menu-button-";

	public static MenuBar createMenuBar( Xenon program, String descriptor ) {
		return createMenuBar( program, descriptor, false );
	}

	public static MenuBar createMenuBar( Xenon program, String descriptor, boolean showActionIcon ) {
		// Build the program menu
		List<Menu> menus = MenuBarFactory.createMenus( program, descriptor, showActionIcon );

		MenuBar bar = new MenuBar( menus.toArray( new Menu[ 0 ] ) );
		StackPane.setAlignment( bar, Pos.CENTER_LEFT );
		bar.getStyleClass().add( "menu-bar" );

		return bar;
	}

	public static ContextMenu createContextMenu( Xenon program, String descriptor, boolean showActionIcon ) {
		ContextMenu menu = new ContextMenu();
		parseDescriptor( descriptor ).forEach( t -> menu.getItems().add( createMenuBarItem( program, t, showActionIcon ) ) );
		return menu;
	}

	public static ContextMenu createContextMenu( Xenon program, Token menuToken, boolean showActionIcon ) {
		ContextMenu menu = new ContextMenu();
		menuToken.getChildren().forEach( t -> menu.getItems().add( createMenuBarItem( program, t, showActionIcon ) ) );
		return menu;
	}

	public static List<Menu> createMenus( Xenon program, String descriptor, boolean showActionIcon ) {
		return parseDescriptor( descriptor ).stream().map( t -> createMenu( program, t, showActionIcon ) ).toList();
	}

	public static Menu createMenu( Xenon program, String descriptor, boolean showActionIcon ) {
		return createMenu( program, parseDescriptor( descriptor ).get( 0 ), showActionIcon );
	}

	public static Menu createMenu( Xenon program, Token token, boolean showActionIcon ) {
		ActionProxy action = program.getActionLibrary().getAction( token.getId() );

		if( action == null ) throw new IllegalArgumentException( "No action found for id: " + token.getId() );

		Menu menu = new Menu();
		menu.setId( MENU_ID_PREFIX + action.getId() );
		menu.setMnemonicParsing( true );
		menu.setText( action.getNameWithMnemonic() );
		if( showActionIcon ) menu.setGraphic( program.getIconLibrary().getIcon( action.getIcon() ) );
		//menu.setAccelerator( parseShortcut( action.getShortcut() ) );

		action.mnemonicNameProperty().addListener( ( event ) -> menu.setText( action.getName() ) );

		for( Token child : token.getChildren() ) {
			menu.getItems().add( createMenuBarItem( program, child, showActionIcon ) );
		}

		return menu;
	}

	public static MenuButton createMenuButton( Xenon program, String descriptor, boolean showActionIcon ) {
		return createMenuButton( program, parseDescriptor( descriptor ).get( 0 ), showActionIcon );
	}

	public static MenuButton createMenuButton( Xenon program, Token token ) {
		return createMenuButton( program, token, true, true );

	}

	public static MenuButton createMenuButton( Xenon program, Token token, boolean showActionIcon ) {
		return createMenuButton( program, token, showActionIcon, true );
	}

	public static MenuButton createMenuButton( Xenon program, String descriptor, boolean showActionIcon, boolean showActionText ) {
		return createMenuButton( program, parseDescriptor( descriptor ).get( 0 ), showActionIcon, showActionText );
	}

	public static MenuButton createMenuButton( Xenon program, Token token, boolean showActionIcon, boolean showActionText ) {
		ActionProxy action = program.getActionLibrary().getAction( token.getId() );

		if( action == null ) throw new IllegalArgumentException( "No action found for id: " + token.getId() );

		MenuButton menu = new MenuButton();
		menu.setId( MENU_BUTTON_ID_PREFIX + action.getId() );
		menu.setMnemonicParsing( true );
		if( showActionText ) menu.setText( action.getNameWithMnemonic() );
		if( showActionIcon ) menu.setGraphic( program.getIconLibrary().getIcon( action.getIcon() ) );
		//menu.setAccelerator( parseShortcut( action.getShortcut() ) );

		action.mnemonicNameProperty().addListener( ( event ) -> menu.setText( action.getName() ) );

		for( Token child : token.getChildren() ) {
			menu.getItems().add( createMenuBarItem( program, child, showActionIcon ) );
		}

		return menu;
	}

	public static List<MenuButton> createMenuButtons( Xenon program, String descriptor, boolean showActionIcon, boolean showActionText ) {
		return createMenuButtons( program, parseDescriptor( descriptor ), showActionIcon, showActionText );
	}

	public static List<MenuButton> createMenuButtons( Xenon program, List<Token> tokens, boolean showActionIcon, boolean showActionText ) {
		//		ActionProxy action = program.getActionLibrary().getAction( token.getId() );
		//
		//		if( action == null ) throw new IllegalArgumentException( "No action found for id: " + token.getId() );

		//		MenuButton menu = new MenuButton();
		//		menu.setId( MENU_BUTTON_ID_PREFIX + action.getId() );
		//		menu.setMnemonicParsing( true );
		//		if( showActionText ) menu.setText( action.getNameWithMnemonic() );
		//		if( showActionIcon ) menu.setGraphic( program.getIconLibrary().getIcon( action.getIcon() ) );
		//		//menu.setAccelerator( parseShortcut( action.getShortcut() ) );
		//
		//		action.mnemonicNameProperty().addListener( ( event ) -> menu.setText( action.getName() ) );

		List<MenuButton> buttons = new ArrayList<>();

		for( Token child : tokens ) {
			MenuButton button = createMenuButton( program, child, showActionIcon );
			//button.getStyleClass().addAll("menu-button-menubar");
			buttons.add( button );
		}

		return buttons;
	}

	public static MenuItem createMenuBarItem( Xenon program, String action ) {
		return createMenuBarItem( program, new Token( action ), false );
	}

	private static MenuItem createMenuBarItem( Xenon program, Token item, boolean showActionIcon ) {
		if( item.getType() == Token.Type.SEPARATOR ) {
			MenuItem separator = new SeparatorMenuItem();
			separator.setId( "separator" );
			return separator;
		} else if( item.getChildren().isEmpty() ) {
			return createMenuBarItem( program, program.getActionLibrary().getAction( item.getId() ) );
		} else if( item.getType() == Token.Type.TRAY || item.getType() == Token.Type.MENU ) {
			return createMenu( program, item, showActionIcon );
		}

		log.atError().log( "Unknown menubar item type: %s", item );

		return new MenuItem();
	}

	private static MenuItem createMenuBarItem( Xenon program, ActionProxy action ) {
		String type = action.getType();
		if( type == null ) type = "normal";

		MenuItem item;
		switch( type ) {
			case "check", "checkbox" -> item = new CheckMenuItem();
			case "radio" -> item = new RadioMenuItem();
			default -> item = new MenuItem();
		}

		item.setId( MENU_ITEM_ID_PREFIX + action.getId() );
		item.setOnAction( action );
		item.setMnemonicParsing( true );
		item.setDisable( !action.isEnabled() );
		item.setText( action.getNameWithMnemonic() );
		item.setGraphic( program.getIconLibrary().getIcon( action.getIcon() ) );
		item.setAccelerator( parseShortcut( action.getShortcut() ) );

		if( action.getCommand() != null ) item.setText( action.getNameWithMnemonic() + " [" + action.getCommand() + "]" );

		action.enabledProperty().addListener( ( event ) -> item.setDisable( !action.isEnabled() ) );
		action.mnemonicNameProperty().addListener( ( event ) -> item.setText( action.getName() ) );
		action.iconProperty().addListener( ( event ) -> item.setGraphic( program.getIconLibrary().getIcon( action.getIcon() ) ) );

		return item;
	}

	public static MenuItem createWorkareaMenuItem( Workarea workarea ) {
		MenuItem item = new MenuItem();
		item.textProperty().bind( workarea.nameProperty() );
		item.graphicProperty().bind( workarea.iconProperty().map( i -> workarea.getProgram().getIconLibrary().getIcon( i ) ) );
		item.getStyleClass().addAll( "workarea-menu-item" );
		item.setOnAction( e -> workarea.getWorkspace().setActiveWorkarea( workarea ) );
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
				case 'C' -> modifierList[ index ] = KeyCombination.CONTROL_DOWN;
				case 'A' -> modifierList[ index ] = KeyCombination.ALT_DOWN;
				case 'S' -> modifierList[ index ] = KeyCombination.SHIFT_DOWN;
				case 'M' -> modifierList[ index ] = KeyCombination.META_DOWN;
				case 'T' -> modifierList[ index ] = KeyCombination.SHORTCUT_DOWN;
			}
			index++;
		}
		return modifierList;
	}

}
