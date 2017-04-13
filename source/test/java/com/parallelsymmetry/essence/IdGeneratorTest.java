package com.parallelsymmetry.essence;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class IdGeneratorTest {

	@Test
	public void testGetId() {
		String id = IdGenerator.getId();
		assertThat( id.length(), is( 8 ) );
	}

	@Test
	public void testConvertLong() {
		assertThat( IdGenerator.toString( 0x00000000 ), is( "mmmmmmmm" ) );
		assertThat( IdGenerator.toString( 0x55555555 ), is( "cccccccc" ) );
		assertThat( IdGenerator.toString( 0xAAAAAAAA ), is( "xxxxxxxx" ) );
		assertThat( IdGenerator.toString( 0xFFFFFFFF ), is( "ssssssss" ) );
	}

}
