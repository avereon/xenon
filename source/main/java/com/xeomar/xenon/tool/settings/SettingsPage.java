package com.xeomar.xenon.tool.settings;

import com.xeomar.xenon.node.Node;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;

public class SettingsPage extends Node {

	public static final String KEY = "key";

	private static final String ICON = "icon";

	private static final String TITLE = "title";

	private static final String GROUPS = "groups";

	private static final String PAGES = "pages";

	public SettingsPage() {
		setValue( GROUPS, new CopyOnWriteArrayList<>() );
		setValue( PAGES, new CopyOnWriteArraySet<>() );

		definePrimaryKey( KEY );
		defineBusinessKey( TITLE );

		setModified( false );
	}

	public String getKey() {
		return getValue( KEY );
	}

	public void setKey( String key ) {
		setValue( KEY, key );
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

	public Set<SettingsPage> getPages() {
		return Collections.unmodifiableSet( getValue( PAGES ) );
	}

	public void addPage( SettingsPage page ) {
		Set<SettingsPage> pages = getValue( PAGES );
		pages.add( page );
	}

}
