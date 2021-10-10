package com.avereon.xenon.test.tool.settings;

import com.avereon.settings.MapSettings;
import com.avereon.settings.Settings;
import com.avereon.xenon.tool.settings.SettingDependency;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class SettingDependencyTest {

	@Test
	void testConstructorWithKeyAndValue() {
		SettingDependency dependency = new SettingDependency();
		dependency.setKey( "enabled" );
		dependency.setDependencyValue( "true" );

		assertThat( dependency.getOperator(), is( SettingDependency.Operator.AND ) );
		assertThat( dependency.getKey(), is( "enabled" ) );
		assertThat( dependency.getDependencyValue(), is( "true" ) );
	}

	@Test
	void testConstructorWithOperatorKeyAndValue() {
		SettingDependency dependency = new SettingDependency();
		dependency.setOperator( SettingDependency.Operator.OR );
		dependency.setKey( "enabled" );
		dependency.setDependencyValue( "true" );

		assertThat( dependency.getOperator(), is( SettingDependency.Operator.OR ) );
		assertThat( dependency.getKey(), is( "enabled" ) );
		assertThat( dependency.getDependencyValue(), is( "true" ) );
	}

	@Test
	void testEvaluateWithNotOperator() {
		Settings settings = new MapSettings();
		SettingDependency dependency = new SettingDependency();
		dependency.setOperator( SettingDependency.Operator.NOT );
		dependency.setKey( "enabled" );
		dependency.setDependencyValue( "true" );

		settings.set( "enabled", false );
		assertThat( dependency.evaluate( settings, false ), is( false ) );
		assertThat( dependency.evaluate( settings, true ), is( true ) );

		settings.set( "enabled", true );
		assertThat( dependency.evaluate( settings, false ), is( false ) );
		assertThat( dependency.evaluate( settings, true ), is( false ) );
	}

	@Test
	void testEvaluateWithAndOperator() {
		Settings settings = new MapSettings();
		SettingDependency dependency = new SettingDependency();
		dependency.setOperator( SettingDependency.Operator.AND );
		dependency.setKey( "enabled" );
		dependency.setDependencyValue( "true" );

		settings.set( "enabled", false );
		assertThat( dependency.evaluate( settings, false ), is( false ) );
		assertThat( dependency.evaluate( settings, true ), is( false ) );

		settings.set( "enabled", true );
		assertThat( dependency.evaluate( settings, false ), is( false ) );
		assertThat( dependency.evaluate( settings, true ), is( true ) );
	}

	@Test
	void testEvaluateWithOrOperator() {
		Settings settings = new MapSettings();
		SettingDependency dependency = new SettingDependency();
		dependency.setOperator( SettingDependency.Operator.OR );
		dependency.setKey( "enabled" );
		dependency.setDependencyValue( "true" );

		settings.set( "enabled", false );
		assertThat( dependency.evaluate( settings, false ), is( false ) );
		assertThat( dependency.evaluate( settings, true ), is( true ) );

		settings.set( "enabled", true );
		assertThat( dependency.evaluate( settings, false ), is( true ) );
		assertThat( dependency.evaluate( settings, true ), is( true ) );
	}

	@Test
	void testEvaluateWithXorOperator() {
		Settings settings = new MapSettings();
		SettingDependency dependency = new SettingDependency();
		dependency.setOperator( SettingDependency.Operator.XOR );
		dependency.setKey( "enabled" );
		dependency.setDependencyValue( "true" );

		settings.set( "enabled", false );
		assertThat( dependency.evaluate( settings, false ), is( false ) );
		assertThat( dependency.evaluate( settings, true ), is( true ) );

		settings.set( "enabled", true );
		assertThat( dependency.evaluate( settings, false ), is( true ) );
		assertThat( dependency.evaluate( settings, true ), is( false ) );
	}

}
