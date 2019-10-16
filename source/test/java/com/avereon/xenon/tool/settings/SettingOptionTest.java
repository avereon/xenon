package com.avereon.xenon.tool.settings;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class SettingOptionTest {

	@Test
	public void testConstructorWithKeyValueAnd() {
		SettingOption option = new SettingOption();
		assertThat( option, not( is( nullValue() ) ) );
	}

	@Test
	public void testSetAndGetKey() {
		SettingOption option = new SettingOption();
		option.setKey( "info" );
		assertThat( option.getKey(), is( "info" ) );
	}

	@Test
	public void testSetAndGetName() {
		SettingOption option = new SettingOption();
		option.setName( "Information" );
		assertThat( option.getName(), is( "Information" ) );
	}

	@Test
	public void testSetAndGetValue() {
		SettingOption option = new SettingOption();
		option.setOptionValue( "3" );
		assertThat( option.getOptionValue(), is( "3" ) );
	}

}
