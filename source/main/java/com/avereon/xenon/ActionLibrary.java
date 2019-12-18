package com.avereon.xenon;

import com.avereon.product.ProductBundle;
import com.avereon.util.LogUtil;
import org.slf4j.Logger;

import java.lang.invoke.MethodHandles;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ActionLibrary {

	private static Logger log = LogUtil.get( MethodHandles.lookup().lookupClass() );

	private Map<String, ActionProxy> actions;

	public ActionLibrary( ProductBundle bundle ) {
		this.actions = new ConcurrentHashMap<>();

		// Create default actions
		register( bundle, "settings" );
		register( bundle, "exit" );

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

		register( bundle, "edit" );
		register( bundle, "undo" );
		register( bundle, "redo" );
		register( bundle, "cut" );
		register( bundle, "copy" );
		register( bundle, "paste" );
		register( bundle, "delete" );
		register( bundle, "indent" );
		register( bundle, "unindent" );

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
		//		register( bundle, "settings-reset" );
		register( bundle, "restart" );

		register( bundle, "workarea" );
		register( bundle, "workarea-new" );
		register( bundle, "workarea-rename" );
		register( bundle, "workarea-close" );

		register( bundle, "task" );

		register( bundle, "refresh" );
		register( bundle, "enable" );
		register( bundle, "disable" );
		register( bundle, "install" );
		register( bundle, "remove" );
		register( bundle, "add-market" );
		register( bundle, "remove-market" );
	}

	public ActionProxy getAction( String id ) {
		ActionProxy proxy = actions.get( id );
		if( proxy == null ) log.warn( "Action proxy not found: " + id );
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

		proxy.setId( id );
		proxy.setIcon( icon );
		proxy.setName( name );
		proxy.setType( type );
		proxy.setShortcut( shortcut );
		try {
			proxy.setMnemonic( Integer.parseInt( mnemonic ) );
		} catch( NumberFormatException exception ) {
			proxy.setMnemonic( ActionProxy.NO_MNEMONIC );
		}

		actions.put( id, proxy );
	}

}
