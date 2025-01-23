package com.avereon.xenon.tool.settings;

import com.avereon.settings.MapSettings;
import com.avereon.settings.Settings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SettingDataTest {

	private SettingsPage page;

	private SettingGroup group;

	private SettingData data;

	private Settings settings;
	private Settings groupSettings;
	private Settings dataSettings;

	@BeforeEach
	void setup() {
		page = new SettingsPage( null );
		group = new SettingGroup( page );
		data = new SettingData( group );

		settings = new MapSettings();
		groupSettings = new MapSettings();
		dataSettings = new MapSettings();

		page.setSettings( settings );
	}

	@Test
	void testGetSettings() {
		assertThat( data.getSettings() ).isEqualTo( settings );
	}

	@Test
	void testGetSettingsWithPath() {
		assertThat( data.getSettings() ).isEqualTo( settings );

		Settings flags = settings.getNode( "child/flags" );
		assertThat( data.getSettings() ).isEqualTo( settings );

		data.setPath( "child/flags" );
		assertThat( data.getSettings() ).isEqualTo( flags );
	}

}
