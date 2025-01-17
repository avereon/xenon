package com.avereon.xenon.tool.settings;

import com.avereon.settings.MapSettings;
import com.avereon.settings.Settings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SettingsPageTest {

	private SettingsPage parent;

	private SettingsPage child;

	private Settings settings;

	@BeforeEach
	void setup() {
		parent = new SettingsPage( null );
		child = new SettingsPage( parent );

		settings = new MapSettings();
		parent.setSettings( settings );
	}

	@Test
	void testGetParent() {
		assertThat( parent.getParent() ).isNull();
		assertThat( child.getParent() ).isEqualTo( parent );

		assertThat( parent.getSettings() ).isEqualTo( settings );
		assertThat( child.getSettings() ).isEqualTo( settings );
	}

	@Test
	void childSettingsOverrideParentSettings() {
		// given
		assertThat( parent.getSettings() ).isEqualTo( settings );
		assertThat( child.getSettings() ).isEqualTo( settings );

		// when
		Settings childSettings = new MapSettings();
		child.setSettings( childSettings );

		// then
		assertThat( parent.getSettings() ).isEqualTo( settings );
		assertThat( child.getSettings() ).isEqualTo( childSettings );
	}

}
