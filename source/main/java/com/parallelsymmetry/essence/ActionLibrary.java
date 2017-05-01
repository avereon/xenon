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

		register( bundle, icons, "file" );
		register( bundle, icons, "edit" );
		register( bundle, icons, "view" );
		register( bundle, icons, "workarea-new" );
		register( bundle, icons, "help" );
		register( bundle, icons, "about" );
	}

	public ActionProxy getAction( String id ) {
		return actions.get( id );
	}

	public void register( ProductBundle bundle, IconLibrary icons, String id ) {
		ActionProxy proxy = new ActionProxy();

		// Create action proxy from resource bundle data
		IconRenderer icon = icons.getIcon( id + ".icon" );
		String name = bundle.getActionString( id + ".name" );
		String mnemonic = bundle.getActionString( id + ".mnemonic" );
		String shortcut = bundle.getActionString( id + ".shortcut" );

		proxy.setId( id );
		proxy.setIcon( icon );
		proxy.setName( name );
		proxy.setMnemonic( mnemonic );
		proxy.setShortcut( shortcut );

		actions.put( id, proxy );
	}

}
