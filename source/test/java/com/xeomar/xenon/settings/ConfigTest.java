package com.xeomar.xenon.settings;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

public class ConfigTest {

	private Config config;

	@Before
	public void setup() {
		config = new Config();
	}

	@Test
	public void testStringSetting() {
		String key = "key";
		String value = "value";
		assertThat( config.get( key ), is( nullValue() ) );
		config.put( key, value );
		assertThat( config.get( key ), is( value ) );
	}

	@Test
	public void testStringDefaultSetting() {
		String key = "key";
		String value = "value";
		assertThat( config.get( key, value ), is( value ) );
	}

	@Test
	public void testPrimitiveBooleanSetting() {
		String key = "key";
		boolean value = true;
		assertThat( config.get( key ), is( nullValue() ) );
		assertThat( config.get( key, true ), is( true ) );
		config.put( key, value );
		assertThat( config.get( key ), is( value ) );
	}

	@Test
	public void testPrimitiveBooleanDefaultSetting() {
		String key = "key";
		boolean value = true;
		assertThat( config.get( key, value ), is( value ) );
	}

	@Test
	public void testBooleanClassSetting() {
		String key = "key";
		Boolean value = true;
		assertThat( config.get( key ), is( nullValue() ) );
		config.put( key, value );
		assertThat( config.get( key ), is( value ) );
	}

	@Test
	public void testBooleanClassDefaultSetting() {
		String key = "key";
		Boolean value = true;
		assertThat( config.get( key, value ), is( value ) );
	}

	@Test
	public void testPrimitiveIntegerSetting() {
		String key = "key";
		int value = 5;
		assertThat( config.get( key ), is( nullValue() ) );
		config.put( key, value );
		assertThat( config.get( key ), is( value ) );
	}

	@Test
	public void testPrimitiveIntegerDefaultSetting() {
		String key = "key";
		int value = 5;
		assertThat( config.get( key, value ), is( value ) );
	}

	@Test
	public void testIntegerClassSetting() {
		String key = "key";
		Integer value = 5;
		assertThat( config.get( key ), is( nullValue() ) );
		config.put( key, value );
		assertThat( config.get( key ), is( value ) );
	}

	@Test
	public void testIntegerClassDefaultSetting() {
		String key = "key";
		Integer value = 5;
		assertThat( config.get( key, value ), is( value ) );
	}

	@Test
	public void testPrimitiveLongSetting() {
		String key = "key";
		long value = 5L;
		assertThat( config.get( key ), is( nullValue() ) );
		config.put( key, value );
		assertThat( config.get( key ), is( value ) );
	}

	@Test
	public void testPrimitiveLongDefaultSetting() {
		String key = "key";
		long value = 5L;
		assertThat( config.get( key, value ), is( value ) );
	}

	@Test
	public void testLongClassSetting() {
		String key = "key";
		Long value = 5L;
		assertThat( config.get( key ), is( nullValue() ) );
		config.put( key, value );
		assertThat( config.get( key ), is( value ) );
	}

	@Test
	public void testLongClassDefaultSetting() {
		String key = "key";
		Long value = 5L;
		assertThat( config.get( key, value ), is( value ) );
	}

	@Test
	public void testPrimitiveFloatSetting() {
		String key = "key";
		float value = 5F;
		assertThat( config.get( key ), is( nullValue() ) );
		config.put( key, value );
		assertThat( config.get( key ), is( value ) );
	}

	@Test
	public void testPrimitiveFloatDefaultSetting() {
		String key = "key";
		float value = 5F;
		assertThat( config.get( key, value ), is( value ) );
	}

	@Test
	public void testFloatClassSetting() {
		String key = "key";
		Float value = 5F;
		assertThat( config.get( key ), is( nullValue() ) );
		config.put( key, value );
		assertThat( config.get( key ), is( value ) );
	}

	@Test
	public void testFloatClassDefaultSetting() {
		String key = "key";
		Float value = 5F;
		assertThat( config.get( key, value ), is( value ) );
	}

	@Test
	public void testPrimitiveDoubleSetting() {
		String key = "key";
		double value = 5.0;
		assertThat( config.get( key ), is( nullValue() ) );
		config.put( key, value );
		assertThat( config.get( key ), is( value ) );
	}

	@Test
	public void testPrimitiveDoubleDefaultSetting() {
		String key = "key";
		double value = 5.0;
		assertThat( config.get( key, value ), is( value ) );
	}

	@Test
	public void testDoubleClassSetting() {
		String key = "key";
		Double value = 5.0;
		assertThat( config.get( key ), is( nullValue() ) );
		config.put( key, value );
		assertThat( config.get( key ), is( value ) );
	}

	@Test
	public void testDoubleClassDefaultSetting() {
		String key = "key";
		Double value = 5.0;
		assertThat( config.get( key, value ), is( value ) );
	}

}
