package com.xeomar.xenon.tool;

import com.xeomar.xenon.settings.MockSettings;
import com.xeomar.xenon.tool.settings.SettingGroup;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class SettingGroupTest {

	@Test
	public void testConstructor() {
		MockSettings settings = new MockSettings();
		SettingGroup group = new SettingGroup( settings );
		assertThat( group.isEnabled(), is( true ) );
		assertThat( group.isVisible(), is( true ) );
	}

}
