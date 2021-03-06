package com.avereon.xenon;

import com.avereon.product.ProductCard;
import com.avereon.settings.Settings;
import com.avereon.settings.SettingsEvent;
import com.avereon.settings.StoredSettings;
import com.avereon.skill.Controllable;
import com.avereon.util.PathUtil;
import com.avereon.xenon.tool.guide.Guide;
import com.avereon.xenon.tool.guide.GuideNode;
import com.avereon.xenon.tool.settings.SettingOptionProvider;
import com.avereon.xenon.tool.settings.SettingsPage;
import com.avereon.xenon.tool.settings.SettingsPageParser;
import com.avereon.xenon.tool.settings.SettingsTool;
import com.avereon.zerra.event.FxEventHub;
import com.avereon.zerra.javafx.Fx;
import javafx.scene.control.SelectionMode;
import lombok.CustomLog;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@CustomLog
public class SettingsManager implements Controllable<SettingsManager> {

	private static final String ROOT = "settings";

	private static final String GENERAL = "general";

	private final Program program;

	private final Guide guide;

	private final StoredSettings settings;

	private final FxEventHub eventBus;

	private final Map<String, SettingsPage> allSettingsPages;

	private final Map<String, SettingsPage> rootSettingsPages;

	private final Map<String, SettingOptionProvider> optionProviders;

	public SettingsManager( Program program ) {
		this.program = program;
		this.guide = new Guide();
		this.settings = new StoredSettings( program.getDataFolder().resolve( ROOT ) );
		this.allSettingsPages = new ConcurrentHashMap<>();
		this.rootSettingsPages = new ConcurrentHashMap<>();
		this.optionProviders = new ConcurrentHashMap<>();
		this.eventBus = new FxEventHub();

		guide.setSelectionMode( SelectionMode.MULTIPLE );

		this.settings.register( SettingsEvent.ANY, e -> eventBus.dispatch( e ) );
	}

	public Settings getSettings( String path ) {
		Settings settings = this.settings.getNode( path );
		//settings.addSettingsListener( e -> eventBus.dispatch( e ) );
		return settings;
	}

	public Settings getSettings( String root, String path ) {
		return getSettings( PathUtil.resolve( root, path ) );
	}

	//	public Settings getProductSettings( Product product ) {
	//		return getSettings( getSettingsPath( product.getCard() ) );
	//	}

	public Settings getProductSettings( ProductCard card ) {
		return getSettings( getSettingsPath( card ) );
	}

	private static String getSettingsPath( ProductCard card ) {
		return ProgramSettings.PRODUCT + card.getProductKey();
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

	public Map<String, SettingsPage> addSettingsPages( ProgramProduct product, Settings settings, String path ) {
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
			log.atFine().log( "Adding settings pages..." );

			// Add pages to the map, don't allow overrides
			for( SettingsPage page : pages.values() ) {
				page.setSettings( settings );
				rootSettingsPages.putIfAbsent( page.getId(), page );
			}

			Fx.run( this::updateSettingsGuide );
		}
	}

	public void removeSettingsPages( Map<String, SettingsPage> pages ) {
		synchronized( rootSettingsPages ) {
			log.atFine().log( "Removing settings pages..." );

			for( SettingsPage page : pages.values() ) {
				rootSettingsPages.remove( page.getId() );
			}

			Fx.run( this::updateSettingsGuide );
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
		// Clear the guide nodes
		guide.clear( node );

		for( SettingsPage page : pages.values() ) {
			GuideNode pageNode = addGuideNode( node, pages.get( page.getId() ) );
			int order = GENERAL.equals( page.getId() ) ? 0 : 1;
			pageNode.setOrder( order );
		}
	}

	private GuideNode addGuideNode( GuideNode parent, SettingsPage page ) {
		if( page == null ) return null;

		allSettingsPages.put( page.getId(), page );

		GuideNode guideNode = guide.addNode( parent, new GuideNode( program, page.getId(), page.getTitle(), page.getIcon() ) );
		createGuide( guideNode, page.getPages() );

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

	public FxEventHub getEventBus() {
		return eventBus;
	}

	private static class SettingsTitleComparator implements Comparator<SettingsPage> {

		@Override
		public int compare( SettingsPage o1, SettingsPage o2 ) {
			if( GENERAL.equals( o1.getId() ) ) return -1;
			if( GENERAL.equals( o2.getId() ) ) return 1;
			return o1.getTitle().compareTo( o2.getTitle() );
		}

	}

}
