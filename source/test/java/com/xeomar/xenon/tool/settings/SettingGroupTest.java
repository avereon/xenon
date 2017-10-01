package com.xeomar.xenon.tool.settings;

import com.xeomar.xenon.settings.MapSettings;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class SettingGroupTest {

	@Test
	public void testConstructor() {
		MapSettings settings = new MapSettings();
		SettingGroup group = new SettingGroup( settings );
		assertThat( group.isDisable(), is( false ) );
		assertThat( group.isVisible(), is( false ) );
	}

}
