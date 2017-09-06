package com.xeomar.xenon;

import com.xeomar.xenon.event.SettingsLoadedEvent;
import com.xeomar.xenon.event.SettingsSavedEvent;
import com.xeomar.xenon.product.Product;
import com.xeomar.xenon.resource.Resource;
import com.xeomar.xenon.resource.type.ProgramSettingsType;
import com.xeomar.xenon.settings.DelayedStoreSettings;
import com.xeomar.xenon.settings.Settings;
import com.xeomar.xenon.settings.SettingsEvent;
import com.xeomar.xenon.settings.SettingsListener;
import com.xeomar.xenon.tool.Guide;
import com.xeomar.xenon.tool.GuideNode;
import com.xeomar.xenon.tool.settings.SettingsPage;
import com.xeomar.xenon.tool.settings.SettingsPageParser;
import com.xeomar.xenon.util.Controllable;
import javafx.scene.control.TreeItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class SettingsManager implements Controllable<SettingsManager> {

	private static final Logger log = LoggerFactory.getLogger( SettingsManager.class );

	private Program program;

	private Map<String, File> paths;

	private Map<File, Settings> settingsMap;

	private SettingsListener settingsWatcher;

	private final Map<String, SettingsPage> settingsPages;

	public SettingsManager( Program program ) {
		this.program = program;
		this.settingsPages = new ConcurrentHashMap<>();
		this.settingsWatcher = new SettingsWatcher( program );

		paths = new ConcurrentHashMap<>();
		paths.put( "program", new File( program.getDataFolder(), ProgramSettings.PROGRAM ) );
		paths.put( "resource", new File( program.getDataFolder(), ProgramSettings.RESOURCE ) );
	}

	public Settings getSettings( String path ) {
		return getSettings( new File( paths.get( "program" ), path ), null );
	}

	public Settings getProgramSettings() {
		return getSettings( getSettingsFile( "program", "program" ), "PROGRAM" );
	}

	public Settings getResourceSettings( Resource resource ) {
		if( resource.getUri() == null ) return null;

		String id = IdGenerator.getId();
		return getSettings( getSettingsFile( "resource", id ), "RESOURCE" );
	}

	public Settings getSettings( File file, String scope ) {
		Settings settings = new DelayedStoreSettings( program.getExecutor(), file );
		settings.addSettingsListener( settingsWatcher );
		settingsMap.put( file, settings );
		return settings;
	}

	public Collection<SettingsPage> getSettingsPages() {
		return Collections.unmodifiableCollection( settingsPages.values() );
	}

	public Set<SettingsPage> addSettingsPages( Product product, Settings settings, String path ) {
		Set<SettingsPage> pages = Collections.emptySet();
		try {
			pages = new SettingsPageParser( product, settings ).parse( path );
			addSettingsPages( pages );
		} catch( IOException exception ) {
			log.error( "Error loading settings page: " + path, exception );
		}
		return pages;
	}

	public void addSettingsPages( Set<SettingsPage> pages ) {
		synchronized( settingsPages ) {
			log.warn( "Adding settings pages..." );

			// Add pages to the map, don't allow overrides
			for( SettingsPage page : pages ) {
				settingsPages.putIfAbsent( page.getId(), page );
			}

			updateSettingsGuide( Collections.unmodifiableMap( settingsPages ) );
		}
	}

	public void removeSettingsPages( Set<SettingsPage> pages ) {
		synchronized( settingsPages ) {
			log.warn( "Removing settings pages..." );

			for( SettingsPage page : pages ) {
				settingsPages.remove( page.getId() );
			}

			updateSettingsGuide( Collections.unmodifiableMap( settingsPages ) );
		}
	}

	private void updateSettingsGuide( Map<String, SettingsPage> pages ) {
		// Get the settings program resource
		Resource settingsResource = program.getResourceManager().createResource( ProgramSettingsType.URI );
		try {
			program.getResourceManager().openResourcesAndWait( settingsResource );
		} catch( Exception exception ) {
			log.error( "Error getting settings resource", exception );
		}

		// Get the resource guide
		Guide guide = settingsResource.getResource( Guide.GUIDE_KEY );
		if( guide == null ) throw new NullPointerException( "Guide is null but should not be" );

		// Get the guide root node
		TreeItem<GuideNode> root = guide.getRoot();
		if( root == null ) guide.setRoot( root = new TreeItem<>( new GuideNode(), program.getIconLibrary().getIcon( "settings" ) ) );

		addChildNodes(root, pages);
	}

	private void addChildNodes( TreeItem<GuideNode> root, Map<String, SettingsPage> pages ) {
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
		root.getChildren().clear();

		// Add the general node to the guide
		addGuideNode( root, pages.get( "general" ) );

		// Add the remaining nodes to the guide
		for( String title : titles ) {
			addGuideNode( root,  pages.get( titledKeys.get( title ) ) );
		}
	}

	private void addGuideNode( TreeItem<GuideNode> root, SettingsPage page ) {
		if( page == null ) return;

		GuideNode guideNode = new GuideNode();
		guideNode.setId( page.getId() );
		guideNode.setName( page.getTitle() );

		TreeItem<GuideNode> child = new TreeItem<>( guideNode, program.getIconLibrary().getIcon( page.getIcon() ) );
		root.getChildren().add( child );

		addChildNodes( child, page.getPages() );
	}

	@Override
	public boolean isRunning() {
		return settingsMap != null;
	}

	@Override
	public SettingsManager start() {
		settingsMap = new ConcurrentHashMap<>();
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
		for( Settings settings : settingsMap.values() ) {
			//wrapper.close();
		}
		settingsMap = null;
		return this;
	}

	@Override
	public SettingsManager awaitStop( long timeout, TimeUnit unit ) throws InterruptedException {
		return this;
	}

	private File getSettingsFile( String pathKey, String name ) {
		return new File( paths.get( pathKey ), name + Program.SETTINGS_EXTENSION );
	}

	private static class SettingsWatcher implements SettingsListener {

		private Program program;

		private SettingsWatcher( Program program ) {
			this.program = program;
		}

		@Override
		public void event( SettingsEvent event ) {
			switch( event.getType() ) {
				case LOADED: {
					program.fireEvent( new SettingsLoadedEvent( this, event.getRoot() ) );
					break;
				}
				case SAVED: {
					program.fireEvent( new SettingsSavedEvent( this, event.getRoot() ) );
					break;
				}
			}
		}

	}

}
