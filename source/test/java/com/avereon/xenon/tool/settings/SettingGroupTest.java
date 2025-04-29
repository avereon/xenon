package com.avereon.xenon.tool.settings;

import com.avereon.settings.MapSettings;
import com.avereon.settings.Settings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SettingGroupTest {

	private SettingsPage page;

	private SettingGroup group;

	private Settings settings;

	@BeforeEach
	void setup() {
		page = new SettingsPage( null );
		group = new SettingGroup( page );

		settings = new MapSettings();
		page.setSettings( settings );
	}

	@Test
	void testGetSettings() {
		assertThat( group.getSettings() ).isEqualTo( settings );
	}

	@Test
	void testGetSettingsWithPath() {
		assertThat( group.getSettings() ).isEqualTo( settings );

		Settings flags = settings.getNode( "child/flags" );
		assertThat( group.getSettings() ).isEqualTo( settings );

		group.setPath( "child/flags" );
		assertThat( group.getSettings() ).isEqualTo( flags );
	}

}
