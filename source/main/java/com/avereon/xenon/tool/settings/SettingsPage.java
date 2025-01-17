package com.avereon.xenon.tool.settings;

import com.avereon.data.Node;
import com.avereon.settings.Settings;
import com.avereon.xenon.RbKey;
import com.avereon.xenon.XenonProgramProduct;
import lombok.Getter;
import lombok.Setter;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class SettingsPage extends Node {

	public static final SettingsPage EMPTY = new SettingsPage( null );

	public static final String SETTINGS = "settings";

	public static final String ID = "id";

	private static final String PATH = "path";

	private static final String ICON = "icon";

	private static final String TITLE = "title";

	private static final String ORDER = "order";

	private static final String GROUPS = "groups";

	private static final String PAGES = "pages";

	private static final String PRODUCT = "product";

	private static final String PANEL = "panel";

	private static final Map<String, Class<? extends SettingsPanel>> panels;

	private final SettingsPage parent;

	@Setter
	@Getter
	private String rbKey = RbKey.SETTINGS;

	static {
		panels = new ConcurrentHashMap<>();
	}

	SettingsPage( SettingsPage parent ) {
		this.parent = parent;

		setValue( GROUPS, new CopyOnWriteArrayList<>() );
		setValue( PAGES, new ConcurrentHashMap<>() );

		definePrimaryKey( ID );
		defineNaturalKey( TITLE );

		setModified( false );
	}

	/**
	 * Register a new setting page panel.
	 *
	 * @param key The panel key
	 * @param panel The page panel class
	 */
	public static void addPanel( String key, Class<? extends SettingsPanel> panel ) {
		panels.putIfAbsent( key, panel );
	}

	public static Class<? extends SettingsPanel> getPanel( String key ) {
		return panels.get( key );
	}

	@SuppressWarnings( "unchecked" )
	public SettingsPage getParent() {
		return parent;
	}

	public String getId() {
		return getValue( ID );
	}

	public void setId( String id ) {
		setValue( ID, id );
	}

	public String getPath() {
		return getValue( PATH );
	}

	public void setPath( String path ) {
		setValue( PATH, path );
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

	public Integer getOrder() {
		return getValue( ORDER );
	}

	public void setOrder( Integer order ) {
		setValue( ORDER, order );
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

	public XenonProgramProduct getProduct() {
		return getValue( PRODUCT );
	}

	public void setProduct( XenonProgramProduct product ) {
		setValue( PRODUCT, product );
	}

	public Settings getSettings() {
		if( parent != null ) return parent.getSettings();
		return getValue( SETTINGS );
	}

	public void setSettings( Settings settings ) {
		setValue( SETTINGS, settings );
	}

	public String getPanel() {
		return getValue( PANEL );
	}

	public void setPanel( String panel ) {
		setValue( PANEL, panel );
	}

}
