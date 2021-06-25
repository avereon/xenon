package com.avereon.xenon.test.tool.settings;

import com.avereon.xenon.test.ProgramTestCase;
import com.avereon.xenon.tool.settings.SettingsPage;
import com.avereon.xenon.tool.settings.SettingsPageParser;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class SettingsPageParserTest extends ProgramTestCase {

	@Test
	void testParsePages() throws Exception {
		Map<String, SettingsPage> pages = SettingsPageParser.parse( program, "/settings.pages.xml" );
		assertNotNull( pages.get( "general" ) );
		assertThat( pages.size(), is( 1 ) );
	}

	@Test
	void testParseOptions() throws Exception {
		Map<String, SettingsPage> pages = SettingsPageParser.parse( program, "/settings.pages.xml" );
		assertThat( pages.get( "general" ).getGroup( "workspace" ).getSetting( "refresh-in-minutes" ).getOptions().size(), is( 7 ) );
	}

}
