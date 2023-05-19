package com.avereon.xenon.tool.settings;

import com.avereon.xenon.ProgramTestCase;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class SettingsPageParserTest extends ProgramTestCase {

	@Test
	void testParsePages() throws Exception {
		Map<String, SettingsPage> pages = SettingsPageParser.parse( getProgram(), "/settings.pages.xml" );
		assertThat( pages.get( "general" ) ).isNotNull();
		assertThat( pages.size() ).isEqualTo( 1 );
	}

	@Test
	void testParseOptions() throws Exception {
		Map<String, SettingsPage> pages = SettingsPageParser.parse( getProgram(), "/settings.pages.xml" );
		assertThat( pages.get( "general" ).getGroup( "workspace" ).getSetting( "refresh-in-minutes" ).getOptions().size() ).isEqualTo( 7 );
	}

}
