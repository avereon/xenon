package com.avereon.xenon;

import com.avereon.product.ProductCard;
import com.avereon.settings.Settings;
import com.avereon.settings.SettingsEvent;
import com.avereon.settings.StoredSettings;
import com.avereon.skill.Controllable;
import com.avereon.util.IdGenerator;
import com.avereon.util.PathUtil;
import com.avereon.xenon.resource.Resource;
import com.avereon.xenon.resource.ResourceType;
import com.avereon.xenon.tool.guide.Guide;
import com.avereon.xenon.tool.guide.GuideNode;
import com.avereon.xenon.tool.settings.*;
import com.avereon.xenon.tool.settings.panel.*;
import com.avereon.zerra.event.FxEventHub;
import com.avereon.zerra.javafx.Fx;
import javafx.scene.control.SelectionMode;
import lombok.CustomLog;
import lombok.Getter;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@CustomLog
public class SettingsManager implements Controllable<SettingsManager> {

	static final String ROOT = "settings";

	private static final String GENERAL = "general";

	private final Xenon program;

	private final Guide guide;

	private final StoredSettings settings;

	@Getter
	private final FxEventHub eventBus;

	private final Map<String, SettingsPage> allSettingsPages;

	private final Map<String, SettingsPage> rootSettingsPages;

	private final Map<String, SettingOptionProvider> optionProviders;

	public SettingsManager( Xenon program ) {
		this.program = program;
		this.guide = new Guide();
		this.settings = new StoredSettings( program.getDataFolder().resolve( ROOT ) );
		this.allSettingsPages = new ConcurrentHashMap<>();
		this.rootSettingsPages = new ConcurrentHashMap<>();
		this.optionProviders = new ConcurrentHashMap<>();
		this.eventBus = new FxEventHub();
		eventBus.parent( program.getFxEventHub() );

		// Add setting editors
		putPagePanel( "asset-type", AssetTypeSettingsPanel.class );
		putPagePanel( "modules-installed", ModulesInstalledSettingsPanel.class );
		putPagePanel( "modules-available", ModulesAvailableSettingsPanel.class );
		putPagePanel( "modules-updates", ModulesUpdatesSettingsPanel.class );
		putPagePanel( "modules-sources", ModulesSourcesSettingsPanel.class );

		// Add options providers
		putOptionProvider( "program-asset-type-provider", new AssetTypeOptionProvider( program ) );

		guide.setSelectionMode( SelectionMode.MULTIPLE );

		this.settings.register( SettingsEvent.ANY, eventBus::dispatch );
	}

	public long getMaxFlushLimit() {
		return settings.getMaxFlushLimit();
	}

	public void setMaxFlushLimit( long maxFlushLimit ) {
		settings.setMaxFlushLimit( maxFlushLimit );
	}

	public long getMinFlushLimit() {
		return settings.getMinFlushLimit();
	}

	public void setMinFlushLimit( long minFlushLimit ) {
		settings.setMinFlushLimit( minFlushLimit );
	}

	public Settings getSettings( String path ) {
		return this.settings.getNode( path );
	}

	public Settings getSettings( String root, String path ) {
		return getSettings( PathUtil.resolve( root, path ) );
	}

	public Settings getProductSettings( ProductCard card ) {
		return getSettings( getSettingsPath( card ) );
	}

	private static String getSettingsPath( ProductCard card ) {
		return ProgramSettings.PRODUCT + card.getProductKey();
	}

	public void putSettingEditor( String id, Class<? extends SettingEditor> clazz ) {
		SettingEditor.addType( id, clazz );
	}

	public void putPagePanel( String id, Class<? extends SettingsPanel> clazz ) {
		SettingsPage.addPanel( id, clazz );
	}

	public Map<String, SettingOptionProvider> getOptionProviders() {
		return Collections.unmodifiableMap( optionProviders );
	}

	public SettingOptionProvider getOptionProvider( String id ) {
		return optionProviders.get( id );
	}

	public void putOptionProvider( String id, SettingOptionProvider provider ) {
		optionProviders.put( id, provider );
	}

	public Map<String, SettingsPage> addSettingsPages( XenonProgramProduct product, Settings settings, String path ) {
		Map<String, SettingsPage> pages = Collections.emptyMap();
		try {
			pages = SettingsPageParser.parse( product, path );
			addSettingsPages( pages, settings );
		} catch( IOException exception ) {
			log.atSevere().withCause( exception ).log( "Error loading settings page: %s", path );
		}
		return pages;
	}

	public void addSettingsPages( Map<String, SettingsPage> pages, Settings settings ) {
		synchronized( rootSettingsPages ) {
			log.atTrace().log( "Adding settings pages..." );

			// Add pages to the map, don't allow overrides
			for( SettingsPage page : pages.values() ) {
				page.setSettings( settings );
				rootSettingsPages.putIfAbsent( page.getId(), page );
			}

			Fx.run( this::updateSettingsGuide );
			log.atDebug().log( "Settings pages added" );
		}
	}

	public void removeSettingsPages( Map<String, SettingsPage> pages ) {
		synchronized( rootSettingsPages ) {
			log.atTrace().log( "Removing settings pages..." );

			for( SettingsPage page : pages.values() ) {
				rootSettingsPages.remove( page.getId() );
			}

			Fx.run( this::updateSettingsGuide );
			log.atDebug().log( "Settings pages removed" );
		}
	}

	public List<String> getPageIds() {
		return getPageIds( rootSettingsPages );
	}

	private List<String> getPageIds( Map<String, SettingsPage> pages ) {
		List<String> ids = new ArrayList<>();

		for( SettingsPage page : pages.values() ) {
			ids.add( page.getId() );
			ids.addAll( getPageIds( page.getPages() ) );
		}

		return ids;
	}

	public Settings getAssetSettings( Resource resource ) {
		return program.getSettingsManager().getSettings( ProgramSettings.ASSET, IdGenerator.getId( String.valueOf( resource.getUri() ) ) );
	}

	public Settings getAssetTypeSettings( ResourceType type ) {
		return program.getSettingsManager().getSettings( ProgramSettings.ASSET_TYPE, IdGenerator.getId( String.valueOf( type.getKey() ) ) );
	}

	public SettingsPage getSettingsPage( String id ) {
		return allSettingsPages.getOrDefault( id, allSettingsPages.get( SettingsTool.GENERAL ) );
	}

	public Guide getSettingsGuide() {
		return guide;
	}

	private void updateSettingsGuide() {
		createGuide( null, Collections.unmodifiableMap( rootSettingsPages ) );
	}

	private void createGuide( GuideNode node, Map<String, SettingsPage> pages ) {
		// Order the pages
		List<SettingsPage> orderedPages = new ArrayList<>( pages.values() );
		orderedPages.sort( new SettingsOrderComparator() );

		// Clear the guide nodes
		guide.clear( node );

		for( SettingsPage page : pages.values() ) {
			GuideNode pageNode = addGuideNode( node, pages.get( page.getId() ) );
			pageNode.setOrder( page.getOrder() );
		}
	}

	private GuideNode addGuideNode( GuideNode parent, SettingsPage page ) {
		if( page == null ) return null;

		allSettingsPages.put( page.getId(), page );

		final GuideNode guideNode = new GuideNode( program, page.getId(), page.getTitle(), page.getIcon() );

		Fx.run( () -> {
			guide.addNode( parent, guideNode );
			createGuide( guideNode, page.getPages() );
		} );

		return guideNode;
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
	public SettingsManager stop() {
		settings.flush();
		return this;
	}

	private static class SettingsTitleComparator implements Comparator<SettingsPage> {

		@Override
		public int compare( SettingsPage o1, SettingsPage o2 ) {
			if( GENERAL.equals( o1.getId() ) ) return -1;
			if( GENERAL.equals( o2.getId() ) ) return 1;
			return o1.getTitle().compareTo( o2.getTitle() );
		}

	}

	private static class SettingsOrderComparator implements Comparator<SettingsPage> {

		@Override
		public int compare( SettingsPage o1, SettingsPage o2 ) {
			return o1.getOrder().compareTo( o2.getOrder() );
		}

	}

}
