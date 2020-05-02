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

	private final Map<String, ActionProxy> actions;

	private final EventHandler<KeyEvent> shortcutHandler;

	public ActionLibrary( Program program ) {
		this.actions = new ConcurrentHashMap<>();
		shortcutHandler = this::handleEvent;

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
		register( bundle, "refresh" );
		register( bundle, "enable" );
		register( bundle, "disable" );
		register( bundle, "install" );
		register( bundle, "remove" );
		register( bundle, "add-market" );
		register( bundle, "remove-market" );

		register( bundle, "options" );
		register( bundle, "runpause" );
	}

	public ActionProxy getAction( String id ) {
		ActionProxy proxy = actions.get( id );
		if( proxy == null ) log.log( Log.WARN, "Action proxy not found: " + id );
		return proxy;
	}

	public void register( ProductBundle bundle, String id ) {
		ActionProxy proxy = new ActionProxy();

		// Create action proxy from resource bundle data
		String icon = bundle.textOr( BundleKey.ACTION, id + ".icon", "" );
		String name = bundle.textOr( BundleKey.ACTION, id + ".name", id );
		String type = bundle.textOr( BundleKey.ACTION, id + ".type", null );
		String mnemonic = bundle.textOr( BundleKey.ACTION, id + ".mnemonic", null );
		String shortcut = bundle.textOr( BundleKey.ACTION, id + ".shortcut", null );
		String description = bundle.textOr( BundleKey.ACTION, id + ".description", null );

		proxy.setId( id );
		proxy.setIcon( icon );
		proxy.setName( name );
		proxy.setType( type );
		proxy.setShortcut( shortcut );
		proxy.setMnemonic( mnemonic );
		proxy.setDescription( description );

		if( "multi-state".equals( type ) ) addStates( bundle, id, proxy );

		actions.put( id, proxy );
	}

	private void handleEvent( KeyEvent event ) {
		for( ActionProxy proxy : actions.values() ) {
			KeyCombination accelerator = proxy.getAccelerator();
			if( accelerator != null && accelerator.match( event )) {
				proxy.handle( new ActionEvent() );
				event.consume();
				break;
			}
		}
	}

	public void registerScene( Scene scene ) {
		scene.addEventFilter( KeyEvent.KEY_PRESSED, shortcutHandler );
	}

	public void unregisterScene( Scene scene ) {
		scene.removeEventFilter( KeyEvent.KEY_PRESSED, shortcutHandler );
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
