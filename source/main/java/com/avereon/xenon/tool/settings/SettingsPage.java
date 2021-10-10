package com.avereon.xenon.tool.settings;

import com.avereon.data.Node;
import com.avereon.settings.Settings;
import com.avereon.xenon.BundleKey;
import com.avereon.xenon.ProgramProduct;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class SettingsPage extends Node {

	public static final SettingsPage EMPTY = new SettingsPage( null );

	public static final String ID = "id";

	private static final String ICON = "icon";

	private static final String TITLE = "title";

	private static final String GROUPS = "groups";

	private static final String PAGES = "pages";

	private static final String PRODUCT = "product";

	private static final String SETTINGS = "settings";

	private final SettingsPage page;

	private String bundleKey = BundleKey.SETTINGS;

	private Map<String, SettingOptionProvider> optionProviders;

	SettingsPage( SettingsPage page ) {
		this.page = page;

		setValue( GROUPS, new CopyOnWriteArrayList<>() );
		setValue( PAGES, new ConcurrentHashMap<>() );

		definePrimaryKey( ID );
		defineNaturalKey( TITLE );

		setModified( false );
	}

	public String getId() {
		return getValue( ID );
	}

	public void setId( String id ) {
		setValue( ID, id );
	}

	public String getIcon() {
		return getValue( ICON );
	}

	public void setIcon( String icon ) {
		setValue( ICON, icon );
	}

	public String getTitle() {
		return getValue( TITLE );
	}

	public void setTitle( String title ) {
		setValue( TITLE, title );
	}

	public SettingGroup getGroup( String id ) {
		for( SettingGroup group : getGroups() ) {
			if( group.getId().equals( id ) ) return group;
		}
		return null;
	}

	public List<SettingGroup> getGroups() {
		return Collections.unmodifiableList( getValue( GROUPS ) );
	}

	public void addGroup( SettingGroup group ) {
		List<SettingGroup> groups = getValue( GROUPS );
		groups.add( group );
	}

	public Map<String, SettingsPage> getPages() {
		return Collections.unmodifiableMap( getValue( PAGES ) );
	}

	public void addPage( SettingsPage page ) {
		ConcurrentHashMap<String, SettingsPage> pages = getValue( PAGES );
		pages.put( page.getId(), page );
	}

	public ProgramProduct getProduct() {
		return getValue( PRODUCT );
	}

	public void setProduct( ProgramProduct product ) {
		setValue( PRODUCT, product );
	}

	public Settings getSettings() {
		if( page != null ) return page.getSettings();
		return getValue( SETTINGS );
	}

	public void setSettings( Settings settings ) {
		setValue( SETTINGS, settings );
	}

	public String getBundleKey() {
		return bundleKey;
	}

	public void setBundleKey( String bundleKey ) {
		this.bundleKey = bundleKey;
	}

	public Map<String, SettingOptionProvider> getOptionProviders() {
		return Optional.ofNullable( optionProviders ).orElse( Map.of() );
	}

	public void setOptionProviders( Map<String, SettingOptionProvider> optionProviders ) {
		this.optionProviders = optionProviders;
	}

}
