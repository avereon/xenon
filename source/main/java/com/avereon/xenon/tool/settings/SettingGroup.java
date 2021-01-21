package com.avereon.xenon.tool.settings;

import com.avereon.settings.Settings;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class SettingGroup extends SettingDependant {

	private static final String ID = "id";

	private static final String SETTINGS = "settings";

	private final SettingsPage page;

	public SettingGroup( SettingsPage page ) {
		this.page = page;
		definePrimaryKey( ID );
		setValue( SETTINGS, new CopyOnWriteArrayList<Setting>() );
	}

	public Setting getSetting( String key ) {
		for( Setting setting : getSettingsList() ) {
			if( setting.getRbKey().equals( key ) ) return setting;
		}
		return null;
	}

	public SettingsPage getPage() {
		return page;
	}

	public String getId() {
		return getValue( ID );
	}

	public void setId( String id ) {
		setValue( ID, id );
	}

	public List<Setting> getSettingsList() {
		return Collections.unmodifiableList( getValue( SETTINGS ) );
	}

	public void addSetting( Setting setting ) {
		List<Setting> settings = getValue( SETTINGS );
		settings.add( setting );
	}

	public Settings getSettings() {
		return getPage().getSettings();
	}

}
