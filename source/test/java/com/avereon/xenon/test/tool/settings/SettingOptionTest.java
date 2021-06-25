package com.avereon.xenon.test.tool.settings;

import com.avereon.xenon.tool.settings.SettingOption;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class SettingOptionTest {

	@Test
	void testConstructorWithKeyValueAnd() {
		SettingOption option = new SettingOption();
		assertThat( option, not( is( nullValue() ) ) );
	}

	@Test
	void testSetAndGetKey() {
		SettingOption option = new SettingOption();
		option.setKey( "info" );
		assertThat( option.getKey(), is( "info" ) );
	}

	@Test
	void testSetAndGetName() {
		SettingOption option = new SettingOption();
		option.setName( "Information" );
		assertThat( option.getName(), is( "Information" ) );
	}

	@Test
	void testSetAndGetValue() {
		SettingOption option = new SettingOption();
		option.setOptionValue( "3" );
		assertThat( option.getOptionValue(), is( "3" ) );
	}

}
