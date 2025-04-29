package com.avereon.xenon.tool.settings;

import com.avereon.settings.MapSettings;
import com.avereon.settings.Settings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SettingDataTest {

	private SettingsPage page;

	private SettingGroup group;

	private SettingData settingData;

	private Settings settings;

	@BeforeEach
	void setup() {
		page = new SettingsPage( null );
		group = new SettingGroup( page );
		settingData = new SettingData( group );

		settings = new MapSettings();

		page.setSettings( settings );
	}

	@Test
	void testGetSettings() {
		assertThat( settingData.getSettings() ).isEqualTo( settings );
	}

	@Test
	void testGetSettingsWithAbsolutePath() {
		// given
		assertThat( settingData.getSettings() ).isEqualTo( settings );
		assertThat( settingData.getPath() ).isNull();

		Settings child = settings.getNode( "child" );
		Settings flags = child.getNode( "flags" );

		settingData.setPath( "/child" );
		assertThat( settingData.getSettings() ).isEqualTo( child );

		settingData.setPath( "/child/flags" );
		assertThat( settingData.getSettings() ).isEqualTo( flags );

		page.setSettings( child );
		settingData.setPath( "/child/flags" );
		assertThat( settingData.getSettings() ).isEqualTo( flags );
	}

	@Test
	void testGetSettingsWithRelativePath() {
		assertThat( settingData.getSettings() ).isEqualTo( settings );

		Settings child = settings.getNode( "child" );
		Settings flags = child.getNode( "flags" );

		settingData.setPath( "child" );
		assertThat( settingData.getSettings() ).isEqualTo( child );

		settingData.setPath( "child/flags" );
		assertThat( settingData.getSettings() ).isEqualTo( flags );

		page.setSettings( child );
		settingData.setPath( "flags" );
		assertThat( settingData.getSettings() ).isEqualTo( flags );
	}

}
