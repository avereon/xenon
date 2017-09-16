package com.xeomar.xenon.tool.settings;

import com.xeomar.xenon.settings.MockSettings;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class SettingGroupTest {

	@Test
	public void testConstructor() {
		MockSettings settings = new MockSettings();
		SettingGroup group = new SettingGroup( settings );
		assertThat( group.isDisable(), is( false ) );
		assertThat( group.isVisible(), is( false ) );
	}

}
