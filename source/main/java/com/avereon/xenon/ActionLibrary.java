package com.avereon.xenon;

import com.avereon.product.Product;
import com.avereon.product.Rb;
import com.avereon.util.Log;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;

import java.lang.System.Logger;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ActionLibrary {

	private static final Logger log = Log.get();

	private static final ActionProxy NOOP = new ActionProxy();

	private final Map<String, ActionProxy> actionsById;

	private final Map<KeyCombination, ActionProxy> actionsByAccelerator;

	private final EventHandler<KeyEvent> shortcutHandler;

	public ActionLibrary( Program product ) {
		this.actionsById = new ConcurrentHashMap<>();
		this.actionsByAccelerator = new ConcurrentHashMap<>();
		this.shortcutHandler = this::handleEvent;

		// Create default actions
		register( product, "program" );
		register( product, "file" );
		register( product, "new" );
		register( product, "open" );
		register( product, "save" );
		register( product, "save-as" );
		register( product, "copy-as" );
		register( product, "save-all" );
		register( product, "close" );
		register( product, "close-all" );
		register( product, "exit" );

		register( product, "edit" );
		register( product, "undo" );
		register( product, "redo" );
		register( product, "cut" );
		register( product, "copy" );
		register( product, "paste" );
		register( product, "delete" );
		register( product, "indent" );
		register( product, "unindent" );
		register( product, "properties" );

		register( product, "view" );
		register( product, "workspace-new" );
		register( product, "workspace-close" );
		//		register( bundle, "tool-new" );
		//		register( bundle, "view-default" );
		//		register( bundle, "view-split-horizontal" );
		//		register( bundle, "view-split-vertical" );
		//		register( bundle, "view-merge-north" );
		//		register( bundle, "view-merge-south" );
		//		register( bundle, "view-merge-east" );
		//		register( bundle, "view-merge-west" );
		register( product, "statusbar-show" );
		register( product, "settings" );

		register( product, "tools" );

		register( product, "help" );
		register( product, "help-content" );
		register( product, "welcome" );
		register( product, "notice" );
		register( product, "product" );
		register( product, "update" );
		register( product, "about" );

		register( product, "development" );
		register( product, "test-action-1" );
		register( product, "test-action-2" );
		register( product, "test-action-3" );
		register( product, "test-action-4" );
		register( product, "test-action-5" );
		register( product, "mock-update" );
		register( product, "restart" );
		register( product, "reset" );

		register( product, "workarea" );
		register( product, "workarea-new" );
		register( product, "workarea-rename" );
		register( product, "workarea-close" );

		register( product, "task" );
		register( product, "themes" );
		register( product, "wallpaper-toggle" );
		register( product, "wallpaper-prior" );
		register( product, "wallpaper-next" );

		register( product, "reset" );
		register( product, "runpause" );

		register( product, "refresh" );
		register( product, "enable" );
		register( product, "disable" );
		register( product, "install" );
		register( product, "remove" );
		register( product, "add-market" );
		register( product, "remove-market" );

		// Navigation
		register( product, "prior" );
		register( product, "next" );
		register( product, "up" );
		register( product, "down" );

		register( product, "options" );
	}

	public ActionProxy getAction( String id ) {
		ActionProxy proxy = actionsById.get( id );
		if( proxy == null ) log.log( Log.WARN, "Action proxy not found: " + id );
		return proxy;
	}

	public void register( Product product, String id ) {
		ActionProxy proxy = new ActionProxy();

		// Create action proxy from resource bundle data
		String icon = Rb.textOr( product, BundleKey.ACTION, id + Action.ICON_SUFFIX, "" );
		String name = Rb.textOr( product, BundleKey.ACTION, id + Action.NAME_SUFFIX, id );
		String type = Rb.textOr( product, BundleKey.ACTION, id + Action.TYPE_SUFFIX, null );
		String mnemonic = Rb.textOr( product, BundleKey.ACTION, id + Action.MNEMONIC_SUFFIX, null );
		String shortcut = Rb.textOr( product, BundleKey.ACTION, id + Action.SHORTCUT_SUFFIX, null );
		String command = Rb.textOr( product, BundleKey.ACTION, id + Action.COMMAND_SUFFIX, null );
		String description = Rb.textOr( product, BundleKey.ACTION, id + Action.DESCRIPTION_SUFFIX, null );

		proxy.setId( id );
		proxy.setIcon( icon );
		proxy.setName( name );
		proxy.setType( type );
		proxy.setMnemonic( mnemonic );
		proxy.setShortcut( shortcut );
		proxy.setCommand( command );
		if( command != null ) proxy.setName( name + " (" + command + ")" );
		proxy.setDescription( description );
		if( "multi-state".equals( type ) ) addStates( product, id, proxy );

		actionsById.put( id, proxy );

		KeyCombination accelerator = proxy.getAccelerator();
		if( accelerator != null ) actionsByAccelerator.put( accelerator, proxy );
	}

	// This handler is to capture key combinations for actions that are not in menus or toolbars
	public void registerScene( Scene scene ) {
		scene.addEventHandler( KeyEvent.KEY_PRESSED, shortcutHandler );
	}

	public void unregisterScene( Scene scene ) {
		scene.removeEventHandler( KeyEvent.KEY_PRESSED, shortcutHandler );
	}

	private KeyCombination.Modifier[] getModifiers( KeyEvent event ) {
		KeyCombination.Modifier s = event.isShiftDown() ? KeyCombination.SHIFT_DOWN : KeyCombination.SHIFT_ANY;
		KeyCombination.Modifier c = event.isControlDown() ? KeyCombination.CONTROL_DOWN : KeyCombination.CONTROL_ANY;
		KeyCombination.Modifier a = event.isAltDown() ? KeyCombination.ALT_DOWN : KeyCombination.ALT_ANY;
		KeyCombination.Modifier m = event.isMetaDown() ? KeyCombination.META_DOWN : KeyCombination.META_ANY;
		KeyCombination.Modifier t = event.isShortcutDown() ? KeyCombination.SHORTCUT_DOWN : KeyCombination.SHORTCUT_ANY;
		return new KeyCombination.Modifier[]{ s, c, a, m, t };
	}

	private void handleEvent( KeyEvent event ) {
		// Filter out modifier key events
		if( event.getCode().isModifierKey() ) return;

		// FIXME How can we tell that an event was, or will be, handled by the normal subsystem?
		// Otherwise, we will cause duplicate events for everything the subsystem also handles
		// Things we tried but didn't work
		// - Checking the event.isConsumed() flag

		// If the event has a modifier then it should be handled by the normal subsystem
		//if( getModifiers( event ).length > 0 ) return;

		//actionsByAccelerator.keySet().stream().filter( k -> k.match( event ) ).findFirst().ifPresent( k -> actionsByAccelerator.getOrDefault( k, NOOP ).handle( new ActionEvent() ) );
	}

	private void addStates( Product product, String id, ActionProxy proxy ) {
		String[] states = Rb.textOr( product, BundleKey.ACTION, id + ".states", "" ).split( "," );
		for( String state : states ) {
			String stateName = Rb.textOr( product, BundleKey.ACTION, id + "." + state + ".name", "" );
			String stateIcon = Rb.textOr( product, BundleKey.ACTION, id + "." + state + ".icon", "" );
			proxy.addState( state, stateName, stateIcon );
		}
	}

}
