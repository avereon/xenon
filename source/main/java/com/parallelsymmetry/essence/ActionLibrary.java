package com.parallelsymmetry.essence;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ActionLibrary {

	private static Logger log = LoggerFactory.getLogger( ActionLibrary.class );

	private Map<String, ActionProxy> actions;

	private IconLibrary icons;

	public ActionLibrary( ProductBundle bundle, IconLibrary icons ) {
		this.actions = new ConcurrentHashMap<>();
		this.icons = icons;

		// Create default actions
		register( bundle, icons, "file" );
		register( bundle, icons, "new" );
		register( bundle, icons, "open" );
		register( bundle, icons, "save" );
		//register( bundle, icons, "save-as" );
		//register( bundle, icons, "copy-as" );
		register( bundle, icons, "close" );
		register( bundle, icons, "exit" );

		register( bundle, icons, "edit" );
		register( bundle, icons, "undo" );
		register( bundle, icons, "redo" );
		register( bundle, icons, "cut" );
		register( bundle, icons, "copy" );
		register( bundle, icons, "paste" );
		register( bundle, icons, "delete" );
		register( bundle, icons, "indent" );
		register( bundle, icons, "unindent" );
		register( bundle, icons, "properties" );
		register( bundle, icons, "settings" );

		register( bundle, icons, "view" );
//		register( bundle, icons, "window-new" );
//		register( bundle, icons, "tool-new" );
		//		register( bundle, icons, "view-default" );
		//		register( bundle, icons, "view-split-horizontal" );
		//		register( bundle, icons, "view-split-vertical" );
		//		register( bundle, icons, "view-merge-north" );
		//		register( bundle, icons, "view-merge-south" );
		//		register( bundle, icons, "view-merge-east" );
		//		register( bundle, icons, "view-merge-west" );

		register( bundle, icons, "help" );
		//		register( bundle, icons, "welcome" );
		//		register( bundle, icons, "system-properties" );
		//		register( bundle, icons, "products" );
		//		register( bundle, icons, "updates" );
		register( bundle, icons, "about" );

		//		register( bundle, icons, "development" );
		//		register( bundle, icons, "settings-reset" );
		//		register( bundle, icons, "workers" );
		//		register( bundle, icons, "restart" );

		//		register( bundle, icons, "workarea" );
		//		register( bundle, icons, "workarea-new" );
		//		register( bundle, icons, "workarea-copy" );
		//		register( bundle, icons, "workarea-close" );
	}

	public ActionProxy getAction( String id ) {
		ActionProxy proxy = actions.get( id );
		if( proxy == null ) log.warn( "Action proxy not found: " + id );
		return proxy;
	}

	public void register( ProductBundle bundle, IconLibrary icons, String id ) {
		ActionProxy proxy = new ActionProxy();

		// Create action proxy from resource bundle data
		IconRenderer icon = icons.getIcon( id + ".icon" );
		String name = bundle.getActionString( id + ".name" );
		String mnemonic = bundle.getActionString( id + ".mnemonic" );
		String shortcut = bundle.getActionString( id + ".shortcut" );

		int mnemonicValue = -1;
		try {
			mnemonicValue = Integer.parseInt( mnemonic );
		} catch( NumberFormatException exception ) {
			// Intentionally ignore exception
		}

		proxy.setId( id );
		proxy.setIcon( icon );
		proxy.setName( name );
		proxy.setMnemonic( mnemonicValue );
		proxy.setShortcut( shortcut );

		actions.put( id, proxy );
	}

}
