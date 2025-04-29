package com.avereon.xenon.tool.settings;

import com.avereon.settings.Settings;
import lombok.Getter;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Getter
public class SettingGroup extends SettingDependant {

	private static final String SETTINGS = "settings";

	private final SettingsPage page;

	public SettingGroup( SettingsPage page ) {
		this.page = page;
		definePrimaryKey( ID );
		setValue( SETTINGS, new CopyOnWriteArrayList<SettingData>() );
	}

	public SettingData getSetting( String key ) {
		for( SettingData setting : getSettingsList() ) {
			if( setting.getKey().equals( key ) ) return setting;
		}
		return null;
	}

	public List<SettingData> getSettingsList() {
		return Collections.unmodifiableList( getValue( SETTINGS ) );
	}

	public void addSetting( SettingData setting ) {
		List<SettingData> settings = getValue( SETTINGS );
		settings.add( setting );
	}

	@Override
	Settings getParentSettings() {
		return page.getSettings();
	}

}
