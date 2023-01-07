package com.avereon.xenon.test.tool.settings;

import com.avereon.xenon.test.ProgramTestCase;
import com.avereon.xenon.tool.settings.SettingsPage;
import com.avereon.xenon.tool.settings.SettingsPageParser;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class SettingsPageParserTest extends ProgramTestCase {

	@Test
	void testParsePages() throws Exception {
		Map<String, SettingsPage> pages = SettingsPageParser.parse( program, "/settings.pages.xml" );
		assertThat( pages.get( "general" ) ).isNotNull();
		assertThat( pages.size() ).isEqualTo( 1 );
	}

	@Test
	void testParseOptions() throws Exception {
		Map<String, SettingsPage> pages = SettingsPageParser.parse( program, "/settings.pages.xml" );
		assertThat( pages.get( "general" ).getGroup( "workspace" ).getSetting( "refresh-in-minutes" ).getOptions().size() ).isEqualTo( 7 );
	}

}
