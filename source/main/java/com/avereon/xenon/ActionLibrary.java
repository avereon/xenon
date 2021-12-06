package com.avereon.xenon;

import com.avereon.product.Product;
import com.avereon.product.Rb;
import javafx.scene.Scene;
import javafx.scene.input.KeyCombination;
import lombok.CustomLog;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@CustomLog
public class ActionLibrary {

	private final Map<String, ActionProxy> actionsById;

	public ActionLibrary( Program product ) {
		this.actionsById = new ConcurrentHashMap<>();

		// Create default actions
		register( product, "program" );
		register( product, "file" );
		register( product, "new" );
		register( product, "open" );
		register( product, "reload" );
		register( product, "save" );
		register( product, "save-as" );
		register( product, "save-all" );
		register( product, "rename" );
		register( product, "print" );
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
		register( product, "search" );
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
		if( proxy == null ) log.atWarning().log( "Action proxy not found: %s", id );
		return proxy;
	}

	public void register( Product product, String id ) {
		ActionProxy proxy = new ActionProxy();

		// Create action proxy from resource bundle data
		String icon = Rb.textOr( product, RbKey.ACTION, id + ProgramAction.ICON_SUFFIX, "" );
		String name = Rb.textOr( product, RbKey.ACTION, id + ProgramAction.NAME_SUFFIX, id );
		String type = Rb.textOr( product, RbKey.ACTION, id + ProgramAction.TYPE_SUFFIX, null );
		String mnemonic = Rb.textOr( product, RbKey.ACTION, id + ProgramAction.MNEMONIC_SUFFIX, null );
		String shortcut = Rb.textOr( product, RbKey.ACTION, id + ProgramAction.SHORTCUT_SUFFIX, null );
		String command = Rb.textOr( product, RbKey.ACTION, id + ProgramAction.COMMAND_SUFFIX, null );
		String description = Rb.textOr( product, RbKey.ACTION, id + ProgramAction.DESCRIPTION_SUFFIX, null );
		String[] tags = Rb.textOr( product, RbKey.ACTION, id + ProgramAction.TAGS_SUFFIX, "" ).split( "," );

		for( int index = 0; index < tags.length; index++ ) {
			tags[index] = tags[index].trim().toLowerCase();
		}

		proxy.setId( id );
		proxy.setIcon( icon );
		proxy.setName( name );
		proxy.setType( type );
		proxy.setMnemonic( mnemonic );
		proxy.setShortcut( shortcut );
		proxy.setCommand( command );
		proxy.setTags( tags );
		if( command != null ) proxy.setName( name + " (" + command + ")" );
		proxy.setDescription( description );
		if( "multi-state".equals( type ) ) addStates( product, id, proxy );

		actionsById.put( id, proxy );
	}

	public void registerScene( Scene scene ) {
		actionsById.values().forEach( a -> doAcceleratorInstall( a, scene ) );
	}

	public void unregisterScene( Scene scene ) {
		actionsById.values().forEach( a -> doAcceleratorUninstall( a, scene ) );
	}

	private void doAcceleratorInstall( ActionProxy proxy, Scene scene ) {
		final Map<KeyCombination, Runnable> accelerators = scene.getAccelerators();
		final KeyCombination accelerator = proxy.getAccelerator();
		if( accelerator != null ) accelerators.computeIfAbsent( accelerator, k -> proxy::fire );
	}

	private void doAcceleratorUninstall( ActionProxy proxy, Scene scene ) {
		final Map<KeyCombination, Runnable> accelerators = scene.getAccelerators();
		final KeyCombination accelerator = proxy.getAccelerator();
		if( accelerator != null ) accelerators.computeIfPresent( accelerator, ( k, v ) -> null );
	}

	private void addStates( Product product, String id, ActionProxy proxy ) {
		String[] states = Rb.textOr( product, RbKey.ACTION, id + ".states", "" ).split( "," );
		for( String state : states ) {
			String stateName = Rb.textOr( product, RbKey.ACTION, id + "." + state + ".name", "" );
			String stateIcon = Rb.textOr( product, RbKey.ACTION, id + "." + state + ".icon", "" );
			proxy.addState( state, stateName, stateIcon );
		}
	}

}
