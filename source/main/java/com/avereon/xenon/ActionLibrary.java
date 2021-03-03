package com.avereon.xenon;

import com.avereon.product.ProductBundle;
import com.avereon.util.Log;
import javafx.event.ActionEvent;
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

	public ActionLibrary( Program program ) {
		this.actionsById = new ConcurrentHashMap<>();
		this.actionsByAccelerator = new ConcurrentHashMap<>();
		this.shortcutHandler = this::handleEvent;

		ProductBundle bundle = program.rb();

		// Create default actions
		register( bundle, "program" );
		register( bundle, "file" );
		register( bundle, "new" );
		register( bundle, "open" );
		register( bundle, "save" );
		register( bundle, "save-as" );
		register( bundle, "copy-as" );
		register( bundle, "save-all" );
		register( bundle, "close" );
		register( bundle, "close-all" );
		register( bundle, "exit" );

		register( bundle, "edit" );
		register( bundle, "undo" );
		register( bundle, "redo" );
		register( bundle, "cut" );
		register( bundle, "copy" );
		register( bundle, "paste" );
		register( bundle, "delete" );
		register( bundle, "indent" );
		register( bundle, "unindent" );
		register( bundle, "properties" );

		register( bundle, "view" );
		register( bundle, "workspace-new" );
		register( bundle, "workspace-close" );
		//		register( bundle, "tool-new" );
		//		register( bundle, "view-default" );
		//		register( bundle, "view-split-horizontal" );
		//		register( bundle, "view-split-vertical" );
		//		register( bundle, "view-merge-north" );
		//		register( bundle, "view-merge-south" );
		//		register( bundle, "view-merge-east" );
		//		register( bundle, "view-merge-west" );
		register( bundle, "statusbar-show" );
		register( bundle, "settings" );

		register( bundle, "tools" );

		register( bundle, "help" );
		register( bundle, "help-content" );
		register( bundle, "welcome" );
		register( bundle, "notice" );
		register( bundle, "product" );
		register( bundle, "update" );
		register( bundle, "about" );

		register( bundle, "development" );
		register( bundle, "test-action-1" );
		register( bundle, "test-action-2" );
		register( bundle, "test-action-3" );
		register( bundle, "test-action-4" );
		register( bundle, "test-action-5" );
		register( bundle, "mock-update" );
		register( bundle, "restart" );
		register( bundle, "reset" );

		register( bundle, "workarea" );
		register( bundle, "workarea-new" );
		register( bundle, "workarea-rename" );
		register( bundle, "workarea-close" );

		register( bundle, "task" );
		register( bundle, "themes" );
		register( bundle, "wallpaper-toggle" );
		register( bundle, "wallpaper-prior" );
		register( bundle, "wallpaper-next" );

		register( bundle, "reset" );
		register( bundle, "runpause" );

		register( bundle, "refresh" );
		register( bundle, "enable" );
		register( bundle, "disable" );
		register( bundle, "install" );
		register( bundle, "remove" );
		register( bundle, "add-market" );
		register( bundle, "remove-market" );

		// Navigation
		register( bundle, "prior" );
		register( bundle, "next" );
		register( bundle, "up" );
		register( bundle, "down" );

		register( bundle, "options" );
	}

	public ActionProxy getAction( String id ) {
		ActionProxy proxy = actionsById.get( id );
		if( proxy == null ) log.log( Log.WARN, "Action proxy not found: " + id );
		return proxy;
	}

	public void register( ProductBundle bundle, String id ) {
		ActionProxy proxy = new ActionProxy();

		// Create action proxy from resource bundle data
		String icon = bundle.textOr( BundleKey.ACTION, id + Action.ICON_SUFFIX, "" );
		String name = bundle.textOr( BundleKey.ACTION, id + Action.NAME_SUFFIX, id );
		String type = bundle.textOr( BundleKey.ACTION, id + Action.TYPE_SUFFIX, null );
		String mnemonic = bundle.textOr( BundleKey.ACTION, id + Action.MNEMONIC_SUFFIX, null );
		String shortcut = bundle.textOr( BundleKey.ACTION, id + Action.SHORTCUT_SUFFIX, null );
		String command = bundle.textOr( BundleKey.ACTION, id + Action.COMMAND_SUFFIX, null );
		String description = bundle.textOr( BundleKey.ACTION, id + Action.DESCRIPTION_SUFFIX, null );

		proxy.setId( id );
		proxy.setIcon( icon );
		proxy.setName( name );
		proxy.setType( type );
		proxy.setMnemonic( mnemonic );
		proxy.setShortcut( shortcut );
		proxy.setCommand( command );
		if( command != null ) proxy.setName( name + " (" + command + ")" );
		proxy.setDescription( description );
		if( "multi-state".equals( type ) ) addStates( bundle, id, proxy );

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
		if( event.getCode().isModifierKey() ) return;
		actionsByAccelerator.keySet().stream().filter( k -> k.match( event ) ).findFirst().ifPresent( k -> actionsByAccelerator.getOrDefault( k, NOOP ).handle( new ActionEvent() ) );
	}

	private void addStates( ProductBundle bundle, String id, ActionProxy proxy ) {
		String[] states = bundle.textOr( BundleKey.ACTION, id + ".states", "" ).split( "," );
		for( String state : states ) {
			String stateName = bundle.textOr( BundleKey.ACTION, id + "." + state + ".name", "" );
			String stateIcon = bundle.textOr( BundleKey.ACTION, id + "." + state + ".icon", "" );
			proxy.addState( state, stateName, stateIcon );
		}
	}

}
