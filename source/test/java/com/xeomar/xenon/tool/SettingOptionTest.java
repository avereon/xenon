package com.xeomar.xenon.tool;

import com.xeomar.xenon.tool.settings.SettingOption;
import org.junit.Test;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

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
