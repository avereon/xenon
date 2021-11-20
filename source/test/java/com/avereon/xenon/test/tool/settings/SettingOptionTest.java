package com.avereon.xenon.test.tool.settings;

import com.avereon.xenon.tool.settings.SettingOption;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SettingOptionTest {

	@Test
	void testConstructorWithKeyValueAnd() {
		SettingOption option = new SettingOption();
		assertThat( option ).isNotNull();
	}

	@Test
	void testSetAndGetKey() {
		SettingOption option = new SettingOption();
		option.setKey( "info" );
		assertThat( option.getKey() ).isEqualTo( "info" );
	}

	@Test
	void testSetAndGetName() {
		SettingOption option = new SettingOption();
		option.setName( "Information" );
		assertThat( option.getName() ).isEqualTo( "Information" );
	}

	@Test
	void testSetAndGetValue() {
		SettingOption option = new SettingOption();
		option.setOptionValue( "3" );
		assertThat( option.getOptionValue() ).isEqualTo( "3" );
	}

}
