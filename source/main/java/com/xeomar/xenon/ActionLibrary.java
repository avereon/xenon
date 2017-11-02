package com.xeomar.xenon;

import com.xeomar.razor.LogUtil;
import com.xeomar.xenon.product.ProductBundle;
import org.slf4j.Logger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ActionLibrary {

	private static Logger log = LogUtil.get( ActionLibrary.class );

	private Map<String, ActionProxy> actions;

	public ActionLibrary( ProductBundle bundle ) {
		this.actions = new ConcurrentHashMap<>();

		// Create default actions
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
		register( bundle, "settings" );

		register( bundle, "view" );
		register( bundle, "workspace-new" );
		//		register( bundle, "tool-new" );
		//		register( bundle, "view-default" );
		//		register( bundle, "view-split-horizontal" );
		//		register( bundle, "view-split-vertical" );
		//		register( bundle, "view-merge-north" );
		//		register( bundle, "view-merge-south" );
		//		register( bundle, "view-merge-east" );
		//		register( bundle, "view-merge-west" );
		register( bundle, "statusbar-show" );

		register( bundle, "help" );
		register( bundle, "help-content" );
		register( bundle, "welcome" );
		register( bundle, "notice" );
		//		register( bundle, "products" );
		register( bundle, "update" );
		register( bundle, "about" );

		//		register( bundle, "development" );
		//		register( bundle, "settings-reset" );
		//		register( bundle, "workers" );
		//		register( bundle, "restart" );

		register( bundle, "workarea" );
		register( bundle, "workarea-new" );
		register( bundle, "workarea-rename" );
		register( bundle, "workarea-close" );
	}

	public ActionProxy getAction( String id ) {
		ActionProxy proxy = actions.get( id );
		if( proxy == null ) log.warn( "Action proxy not found: " + id );
		return proxy;
	}

	public void register( ProductBundle bundle, String id ) {
		ActionProxy proxy = new ActionProxy();

		// Create action proxy from resource bundle data
		String icon = bundle.getString( "action", id + ".icon" );
		String name = bundle.getString( "action", id + ".name" );
		String type = bundle.getString( "action", id + ".type" );
		String mnemonic = bundle.getString( "action", id + ".mnemonic" );
		String shortcut = bundle.getString( "action", id + ".shortcut" );

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
