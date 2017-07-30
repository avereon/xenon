package com.xeomar.xenon;

import com.xeomar.xenon.product.ProductBundle;
import org.slf4j.Logger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ActionLibrary {

	private static Logger log = LogUtil.get( ActionLibrary.class );

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
		register( bundle, icons, "save-as" );
		register( bundle, icons, "copy-as" );
		register( bundle, icons, "save-all" );
		register( bundle, icons, "close" );
		register( bundle, icons, "close-all" );
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
		register( bundle, icons, "settings" );

		register( bundle, icons, "view" );
		register( bundle, icons, "workspace-new" );
		//		register( bundle, icons, "tool-new" );
		//		register( bundle, icons, "view-default" );
		//		register( bundle, icons, "view-split-horizontal" );
		//		register( bundle, icons, "view-split-vertical" );
		//		register( bundle, icons, "view-merge-north" );
		//		register( bundle, icons, "view-merge-south" );
		//		register( bundle, icons, "view-merge-east" );
		//		register( bundle, icons, "view-merge-west" );
		register( bundle, icons, "statusbar-show" );

		register( bundle, icons, "help" );
		register( bundle, icons, "help-content" );
		register( bundle, icons, "welcome" );
		//		register( bundle, icons, "products" );
		register( bundle, icons, "update" );
		register( bundle, icons, "about" );

		//		register( bundle, icons, "development" );
		//		register( bundle, icons, "settings-reset" );
		//		register( bundle, icons, "workers" );
		//		register( bundle, icons, "restart" );

		register( bundle, icons, "workarea" );
		register( bundle, icons, "workarea-new" );
		register( bundle, icons, "workarea-rename" );
		register( bundle, icons, "workarea-close" );
	}

	public ActionProxy getAction( String id ) {
		ActionProxy proxy = actions.get( id );
		if( proxy == null ) log.warn( "Action proxy not found: " + id );
		return proxy;
	}

	public void register( ProductBundle bundle, IconLibrary icons, String id ) {
		ActionProxy proxy = new ActionProxy();

		// Create action proxy from resource bundle data
		String icon = bundle.getString( "action", id + ".icon" );
		String name = bundle.getString( "action",id + ".name" );
		String type = bundle.getString( "action",id + ".type" );
		String mnemonic = bundle.getString( "action",id + ".mnemonic" );
		String shortcut = bundle.getString( "action",id + ".shortcut" );

		int mnemonicValue = ActionProxy.NO_MNEMONIC;
		try {
			mnemonicValue = Integer.parseInt( mnemonic );
		} catch( NumberFormatException exception ) {
			// Intentionally ignore exception
		}

		proxy.setId( id );
		proxy.setIcon( icon );
		proxy.setName( name );
		proxy.setType( type );
		proxy.setMnemonic( mnemonicValue );
		proxy.setShortcut( shortcut );

		actions.put( id, proxy );
	}

}
