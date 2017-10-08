package com.xeomar.xenon.tool.settings;

import com.xeomar.xenon.settings.MapSettings;
import com.xeomar.xenon.settings.Settings;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class SettingDependencyTest {

	@Test
	public void testConstructorWithKeyAndValue() {
		SettingDependency dependency = new SettingDependency();
		dependency.setKey( "enabled" );
		dependency.setDependencyValue( "true" );

		assertThat( dependency.getOperator(), is( SettingDependency.Operator.AND ) );
		assertThat( dependency.getKey(), is( "enabled" ) );
		assertThat( dependency.getDependencyValue(), is( "true" ) );
	}

	@Test
	public void testConstructorWithOperatorKeyAndValue() {
		SettingDependency dependency = new SettingDependency();
		dependency.setOperator( SettingDependency.Operator.OR );
		dependency.setKey( "enabled" );
		dependency.setDependencyValue( "true" );

		assertThat( dependency.getOperator(), is( SettingDependency.Operator.OR ) );
		assertThat( dependency.getKey(), is( "enabled" ) );
		assertThat( dependency.getDependencyValue(), is( "true" ) );
	}

	@Test
	public void testEvaluateWithNotOperator() {
		Settings settings = new MapSettings(  );
		SettingDependency dependency = new SettingDependency( );
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
	public void testEvaluateWithAndOperator() {
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
	public void testEvaluateWithOrOperator() {
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
	public void testEvaluateWithXorOperator() {
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
