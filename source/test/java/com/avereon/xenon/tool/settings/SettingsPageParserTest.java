package com.avereon.xenon.tool.settings;

import com.avereon.xenon.ProgramTestCase;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class SettingsPageParserTest extends ProgramTestCase {

	@Test
	void testParsePages() throws Exception {
		Map<String, SettingsPage> pages = SettingsPageParser.parse( getProgram(), "/settings.pages.xml" );

		// Verify the general settings page
		SettingsPage generalSettingsPage = pages.get( "general" );
		assertThat( generalSettingsPage ).isNotNull();
		assertThat( generalSettingsPage.getId() ).isEqualTo( "general" );
		assertThat( generalSettingsPage.getPath() ).isNull();
		assertThat( generalSettingsPage.getIcon() ).isEqualTo( "setting" );
		SettingGroup generalWorkspaceGroup = generalSettingsPage.getGroup( "workspace" );
		assertThat( generalWorkspaceGroup ).isNotNull();
		assertThat( generalWorkspaceGroup.getId() ).isEqualTo( "workspace" );
		assertThat( generalWorkspaceGroup.getPath() ).isNull();
		SettingData refreshInMinutesSetting = generalWorkspaceGroup.getSetting( "refresh-in-minutes" );
		assertThat( refreshInMinutesSetting ).isNotNull();
		assertThat( refreshInMinutesSetting.getId() ).isNull();
		assertThat( refreshInMinutesSetting.getPath() ).isNull();
		assertThat( refreshInMinutesSetting.getKey() ).isEqualTo( "refresh-in-minutes" );

		// Verify the deep settings page
		SettingsPage deepSettingsPage = pages.get( "deep-settings" );
		assertThat( deepSettingsPage ).isNotNull();
		assertThat( deepSettingsPage.getId() ).isEqualTo( "deep-settings" );
		assertThat( deepSettingsPage.getPath() ).isEqualTo( "manager/flags" );
		assertThat( deepSettingsPage.getIcon() ).isEqualTo( "setting" );
		SettingGroup deepFlagsGroup = deepSettingsPage.getGroup( "flags" );
		assertThat( deepFlagsGroup ).isNotNull();
		assertThat( deepFlagsGroup.getId() ).isEqualTo( "flags" );
		assertThat( deepFlagsGroup.getPath() ).isEqualTo( "manager/flags" );
		SettingData flagSetting = deepFlagsGroup.getSetting( "flag-a" );
		assertThat( flagSetting ).isNotNull();
		assertThat( flagSetting.getId() ).isNull();
		assertThat( flagSetting.getPath() ).isEqualTo( "manager/flags" );
		assertThat( flagSetting.getKey() ).isEqualTo( "flag-a" );

		// Make sure we have the right number of pages
		assertThat( pages.size() ).isEqualTo( 2 );
	}

	@Test
	void testParseOptions() throws Exception {
		Map<String, SettingsPage> pages = SettingsPageParser.parse( getProgram(), "/settings.pages.xml" );
		assertThat( pages.get( "general" ).getGroup( "workspace" ).getSetting( "refresh-in-minutes" ).getOptions().size() ).isEqualTo( 7 );
	}

}
