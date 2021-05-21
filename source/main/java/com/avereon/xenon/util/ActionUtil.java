package com.avereon.xenon.util;

import com.avereon.util.Log;
import com.avereon.xenon.ActionProxy;
import com.avereon.xenon.Program;
import com.avereon.xenon.UiFactory;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

import java.lang.System.Logger;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Deprecated
public class ActionUtil {

	private static final Logger log = Log.get();

	public static final String SEPARATOR = "|";

	public static final String SHORTCUT_SEPARATOR = "-";

	public static Menu createMenu( Program program, String id ) {
		return createMenu( program, program.getActionLibrary().getAction( id ) );
	}

	public static Menu createMenu( Program program, ActionProxy action ) {
		return createMenu( program, action, false );
	}

	public static Menu createSubMenu( Program program, String id ) {
		return createMenu( program, program.getActionLibrary().getAction( id ), true );
	}

	public static Menu createSubMenu( Program program, ActionProxy action ) {
		return createMenu( program, action, true );
	}

	private static Menu createMenu( Program program, ActionProxy action, boolean submenu ) {
		Menu item = new Menu();

		item.setId( "menu-" + action.getId() );
		item.getStyleClass().add( "menu-" + action.getId() );
		item.setMnemonicParsing( true );
		item.setText( action.getMnemonicName() );
		if( submenu ) item.setGraphic( program.getIconLibrary().getIcon( action.getIcon() ) );
		//item.setAccelerator( parseShortcut( action.getShortcut() ) );

		action.mnemonicNameProperty().addListener( ( event ) -> item.setText( action.getName() ) );

		return item;
	}

	public static MenuItem createMenuItem( Program program, String id ) {
		return createMenuItem( program, program.getActionLibrary().getAction( id ) );
	}

	public static MenuItem createMenuItem( Program program, ActionProxy action ) {
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

	public static Node createToolBarItem( Program program, ToolBar bar ) {
		return bar;
	}

	public static Node createToolBarItem( Program program, String id ) {
		boolean isSeparator = ActionUtil.SEPARATOR.equals( id );
		return isSeparator ? new Separator() : ActionUtil.createToolBarButton( program, id );
	}

	@Deprecated
	public static List<Node> createToolBarItems( Program program, String... ids ) {
		return Arrays.stream( ids ).map( i -> createToolBarItem( program, i ) ).collect( Collectors.toList());
	}

	public static Button createToolBarButton( Program program, String id ) {
		return createToolBarButton( program, program.getActionLibrary().getAction( id ) );
	}

	public static Button createToolBarButton( Program program, ActionProxy action ) {
		Button button = new Button();

		button.setOnAction( action );
		button.setDisable( !action.isEnabled() );
		button.setGraphic( program.getIconLibrary().getIcon( action.getIcon() ) );

		action.enabledProperty().addListener( ( event ) -> button.setDisable( !action.isEnabled() ) );
		action.iconProperty().addListener( ( event ) -> button.setGraphic( program.getIconLibrary().getIcon( action.getIcon() ) ) );

		return button;
	}

	public static Button createButton( Program program, ActionProxy action ) {
		return createToolBarButton( program, action );
	}

	public static Button createNamedButton( Program program, ActionProxy action ) {
		Button button = createButton( program, action );
		action.mnemonicNameProperty().addListener( ( event ) -> button.setText( action.getMnemonicName() ) );
		return button;
	}

	public static Region createSpring() {
		Region spring = new Region();
		HBox.setHgrow( spring, Priority.ALWAYS );
		return spring;
	}

	public static Region createPad() {
		Region pad = new Region();
		pad.setMinSize( UiFactory.PAD, UiFactory.PAD );
		return pad;
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
