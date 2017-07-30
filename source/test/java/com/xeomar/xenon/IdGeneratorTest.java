package com.xeomar.xenon;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class IdGeneratorTest {

	@Test
	public void testGetId() {
		String id = IdGenerator.getId();
		assertThat( id.length(), is( 16 ) );
	}

	@Test
	public void testConvertLong() {
		assertThat( IdGenerator.toString( 0x0000000000000000L ), is( "mmmmmmmmmmmmmmmm" ) );
		assertThat( IdGenerator.toString( 0x5555555555555555L ), is( "cccccccccccccccc" ) );
		assertThat( IdGenerator.toString( 0xaaaaaaaaaaaaaaaaL ), is( "xxxxxxxxxxxxxxxx" ) );
		assertThat( IdGenerator.toString( 0xffffffffffffffffL ), is( "ssssssssssssssss" ) );
	}

}
