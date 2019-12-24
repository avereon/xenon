package com.avereon.xenon;

import com.avereon.event.EventHub;
import com.avereon.product.Product;
import com.avereon.product.ProductCard;
import com.avereon.settings.Settings;
import com.avereon.settings.SettingsEvent;
import com.avereon.settings.StoredSettings;
import com.avereon.util.Controllable;
import com.avereon.util.LogUtil;
import com.avereon.util.PathUtil;
import com.avereon.xenon.tool.guide.Guide;
import com.avereon.xenon.tool.guide.GuideNode;
import com.avereon.xenon.tool.settings.SettingsPage;
import com.avereon.xenon.tool.settings.SettingsPageParser;
import com.avereon.xenon.tool.settings.SettingsTool;
import javafx.application.Platform;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TreeItem;
import org.slf4j.Logger;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class SettingsManager implements Controllable<SettingsManager> {

	private static final Logger log = LogUtil.get( MethodHandles.lookup().lookupClass() );

	private static final String ROOT = "settings";

	private Program program;

	private Guide guide;

	private StoredSettings settings;

	private EventHub<SettingsEvent> settingsWatcher;

	private final Map<String, SettingsPage> allSettingsPages;

	private final Map<String, SettingsPage> rootSettingsPages;

	public SettingsManager( Program program ) {
		this.program = program;
		this.guide = new Guide();
		this.settings = new StoredSettings( program.getDataFolder().resolve( ROOT ) );
		this.allSettingsPages = new ConcurrentHashMap<>();
		this.rootSettingsPages = new ConcurrentHashMap<>();
		this.settingsWatcher = new EventHub<SettingsEvent>().parent( program.getEventHub() );
	}

	public Settings getSettings( String path ) {
		Settings settings = this.settings.getNode( path );
		settings.addSettingsListener( settingsWatcher );
		return settings;
	}

	public Settings getSettings( String root, String path ) {
		return getSettings( PathUtil.resolve( root, path ) );
	}

	public Settings getProductSettings( Product product ) {
		return getSettings( getSettingsPath( product.getCard() ) );
	}

	public Settings getProductSettings( ProductCard card ) {
		return getSettings( getSettingsPath( card ) );
	}

	private static final String getSettingsPath( ProductCard card ) {
		return ProgramSettings.PRODUCT + card.getProductKey();
	}

	public Map<String, SettingsPage> addSettingsPages( Product product, Settings settings, String path ) {
		Map<String, SettingsPage> pages = Collections.emptyMap();
		try {
			pages = new SettingsPageParser( product, settings ).parse( path );
			addSettingsPages( pages );
		} catch( IOException exception ) {
			log.error( "Error loading settings page: " + path, exception );
		}
		return pages;
	}

	public void addSettingsPages( Map<String, SettingsPage> pages ) {
		synchronized( rootSettingsPages ) {
			log.debug( "Adding settings pages..." );

			// Add pages to the map, don't allow overrides
			for( SettingsPage page : pages.values() ) {
				rootSettingsPages.putIfAbsent( page.getId(), page );
			}

			Platform.runLater( this::updateSettingsGuide );
		}
	}

	public void removeSettingsPages( Map<String, SettingsPage> pages ) {
		synchronized( rootSettingsPages ) {
			log.debug( "Removing settings pages..." );

			for( SettingsPage page : pages.values() ) {
				rootSettingsPages.remove( page.getId() );
			}

			Platform.runLater( this::updateSettingsGuide );
		}
	}

	public SettingsPage getSettingsPage( String id ) {
		return allSettingsPages.getOrDefault( id, allSettingsPages.get( SettingsTool.GENERAL ) );
	}

	public Guide getSettingsGuide() {
		return guide;
	}

	private void updateSettingsGuide() {
		Map<String, SettingsPage> pages = Collections.unmodifiableMap( rootSettingsPages );

		// Get the settings program asset
		try {
			guide.setSelectionMode( SelectionMode.MULTIPLE );

			// Create the guide tree
			createGuide( guide.getRoot(), pages );
		} catch( Exception exception ) {
			log.error( "Error getting settings asset", exception );
		}
	}

	private void createGuide( TreeItem<GuideNode> parent, Map<String, SettingsPage> pages ) {
		// Create a map of the title keys except the general key, it gets special handling
		Map<String, String> titledKeys = new HashMap<>();
		for( SettingsPage page : pages.values() ) {
			if( "general".equals( page.getId() ) ) continue;
			titledKeys.put( page.getTitle(), page.getId() );
		}

		// Create a sorted list of the titles other than General
		List<String> titles = new ArrayList<>( titledKeys.keySet() );
		Collections.sort( titles );

		// Clear the guide nodes
		parent.getChildren().clear();

		// Add the general node to the guide
		addGuideNode( parent, pages.get( "general" ) );

		// Add the remaining nodes to the guide
		for( String title : titles ) {
			addGuideNode( parent, pages.get( titledKeys.get( title ) ) );
		}
	}

	private void addGuideNode( TreeItem<GuideNode> parent, SettingsPage page ) {
		if( page == null ) return;

		allSettingsPages.put( page.getId(), page );

		GuideNode guideNode = new GuideNode();
		guideNode.setId( page.getId() );
		guideNode.setIcon( page.getIcon() );
		guideNode.setName( page.getTitle() );

		TreeItem<GuideNode> child = new TreeItem<>( guideNode, program.getIconLibrary().getIcon( page.getIcon() ) );
		parent.getChildren().add( child );

		createGuide( child, page.getPages() );
	}

	@Override
	public boolean isRunning() {
		return true;
	}

	@Override
	public SettingsManager start() {
		return this;
	}

	//	@Override
	//	public SettingsManager awaitStart( long timeout, TimeUnit unit ) throws InterruptedException {
	//		return this;
	//	}
	//
	//	@Override
	//	public SettingsManager restart() {
	//		stop();
	//		start();
	//		return this;
	//	}
	//
	//	@Override
	//	public SettingsManager awaitRestart( long timeout, TimeUnit unit ) throws InterruptedException {
	//		return this;
	//	}

	@Override
	public SettingsManager stop() {
		settings.flush();
		return this;
	}

	//	@Override
	//	public SettingsManager awaitStop( long timeout, TimeUnit unit ) throws InterruptedException {
	//		return this;
	//	}

}
