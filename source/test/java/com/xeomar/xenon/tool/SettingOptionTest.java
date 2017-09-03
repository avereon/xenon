package com.xeomar.xenon.tool;

import com.xeomar.xenon.tool.settings.SettingOption;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class SettingOptionTest {

	@Test
	public void testConstructorWithKeyValueAnd() {
		SettingOption option = new SettingOption( "info", "Information", "3" );
		assertThat( option.getKey(), is( "info" ) );
		assertThat( option.getName(), is( "Information" ) );
		assertThat( option.getOptionValue(), is( "3" ) );
	}

}
