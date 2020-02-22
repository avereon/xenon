package com.avereon.xenon.tool.settings;

import com.avereon.settings.Settings;
import com.avereon.data.Node;
import com.avereon.product.Product;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class SettingsPage extends Node {

	public static final String ID = "id";

	private static final String ICON = "icon";

	private static final String TITLE = "title";

	private static final String GROUPS = "groups";

	private static final String PAGES = "pages";

	private static final String PRODUCT = "product";

	private static final String SETTINGS = "settings";

	public SettingsPage() {
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

	public Product getProduct() {
		return getValue( PRODUCT );
	}

	public void setProduct( Product product ) {
		setValue( PRODUCT, product );
	}

	public Settings getSettings() {
		return getValue( SETTINGS );
	}

	public void setSettings( Settings settings ) {
		setValue( SETTINGS, settings );
	}

}
