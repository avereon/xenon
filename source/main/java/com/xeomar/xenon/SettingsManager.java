package com.xeomar.xenon;

import com.xeomar.product.Product;
import com.xeomar.product.ProductCard;
import com.xeomar.settings.Settings;
import com.xeomar.settings.SettingsEvent;
import com.xeomar.settings.SettingsListener;
import com.xeomar.settings.StoredSettings;
import com.xeomar.util.Controllable;
import com.xeomar.util.LogUtil;
import com.xeomar.util.PathUtil;
import com.xeomar.xenon.event.SettingsLoadedEvent;
import com.xeomar.xenon.event.SettingsSavedEvent;
import com.xeomar.xenon.resource.Resource;
import com.xeomar.xenon.resource.type.ProgramSettingsType;
import com.xeomar.xenon.tool.guide.Guide;
import com.xeomar.xenon.tool.guide.GuideNode;
import com.xeomar.xenon.tool.settings.SettingsPage;
import com.xeomar.xenon.tool.settings.SettingsPageParser;
import javafx.application.Platform;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TreeItem;
import org.slf4j.Logger;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class SettingsManager implements Controllable<SettingsManager> {

	private static final Logger log = LogUtil.get( MethodHandles.lookup().lookupClass() );

	private static final String ROOT = "settings";

	private Program program;

	private StoredSettings settings;

	private SettingsListener settingsWatcher;

	private final Map<String, SettingsPage> allSettingsPages;

	private final Map<String, SettingsPage> rootSettingsPages;

	public SettingsManager( Program program ) {
		this.program = program;
		this.settings = new StoredSettings( program.getDataFolder().resolve( ROOT ) );
		this.allSettingsPages = new ConcurrentHashMap<>();
		this.rootSettingsPages = new ConcurrentHashMap<>();
		this.settingsWatcher = new SettingsWatcher( program );
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
		return allSettingsPages.get( id );
	}

	private void updateSettingsGuide() {
		Map<String, SettingsPage> pages = Collections.unmodifiableMap( rootSettingsPages );

		// Get the settings program resource
		try {
			Resource settingsResource = program.getResourceManager().createResource( ProgramSettingsType.uri );
			program.getResourceManager().openResourcesAndWait( settingsResource );

			// Get the resource guide
			Guide guide = settingsResource.getResource( Guide.GUIDE_KEY );
			if( guide == null ) throw new NullPointerException( "Guide is null but should not be" );

			guide.setSelectionMode( SelectionMode.MULTIPLE );

			// Create the guide tree
			createGuide( guide.getRoot(), pages );
		} catch( Exception exception ) {
			log.error( "Error getting settings resource", exception );
		}
	}

	private void createGuide( TreeItem<GuideNode> parent, Map<String, SettingsPage> pages ) {
		// Create a map of the title keys except the general key, it gets special handling
		Map<String, String> titledKeys = new HashMap<>();
		for( SettingsPage page : pages.values() ) {
			if( "general".equals( page.getId() ) ) continue;

			String title = page.getTitle();
			String id = page.getId();

			if( title == null ) log.error( "Settings page title is null: " + id );

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

	@Override
	public SettingsManager awaitStart( long timeout, TimeUnit unit ) throws InterruptedException {
		return this;
	}

	@Override
	public SettingsManager restart() {
		stop();
		start();
		return this;
	}

	@Override
	public SettingsManager awaitRestart( long timeout, TimeUnit unit ) throws InterruptedException {
		return this;
	}

	@Override
	public SettingsManager stop() {
		settings.flush();
		return this;
	}

	@Override
	public SettingsManager awaitStop( long timeout, TimeUnit unit ) throws InterruptedException {
		return this;
	}

	private static class SettingsWatcher implements SettingsListener {

		private Program program;

		private SettingsWatcher( Program program ) {
			this.program = program;
		}

		@Override
		public void handleEvent( SettingsEvent event ) {
			String message = event.getPath();
			switch( event.getType() ) {
				case LOADED: {
					program.fireEvent( new SettingsLoadedEvent( this, message ) );
					break;
				}
				case SAVED: {
					program.fireEvent( new SettingsSavedEvent( this, message ) );
					break;
				}
				case CHANGED: {
					log.debug( "Setting changed: " + event.getPath() + ":" + event.getKey() + "=" + event.getNewValue() );
				}
			}
		}

	}

}
