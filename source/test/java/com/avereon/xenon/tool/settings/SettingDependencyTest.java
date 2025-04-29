package com.avereon.xenon.tool.settings;

import com.avereon.settings.MapSettings;
import com.avereon.settings.Settings;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SettingDependencyTest {

	@Test
	void testConstructorWithKeyAndValue() {
		SettingDependency dependency = new SettingDependency();
		dependency.setKey( "enabled" );
		dependency.setDependencyValue( "true" );

		assertThat( dependency.getOperator() ).isEqualTo( SettingDependency.Operator.AND );
		assertThat( dependency.getKey() ).isEqualTo( "enabled" );
		assertThat( dependency.getDependencyValue() ).isEqualTo( "true" );
	}

	@Test
	void testConstructorWithOperatorKeyAndValue() {
		SettingDependency dependency = new SettingDependency();
		dependency.setOperator( SettingDependency.Operator.OR );
		dependency.setKey( "enabled" );
		dependency.setDependencyValue( "true" );

		assertThat( dependency.getOperator() ).isEqualTo( SettingDependency.Operator.OR );
		assertThat( dependency.getKey() ).isEqualTo( "enabled" );
		assertThat( dependency.getDependencyValue() ).isEqualTo( "true" );
	}

	@Test
	void testEvaluateWithNotOperator() {
		Settings settings = new MapSettings();
		SettingDependency dependency = new SettingDependency();
		dependency.setOperator( SettingDependency.Operator.NOT );
		dependency.setKey( "enabled" );
		dependency.setDependencyValue( "true" );

		settings.set( "enabled", false );
		assertThat( dependency.evaluate( settings, false ) ).isEqualTo( false );
		assertThat( dependency.evaluate( settings, true ) ).isEqualTo( true );

		settings.set( "enabled", true );
		assertThat( dependency.evaluate( settings, false ) ).isEqualTo( false );
		assertThat( dependency.evaluate( settings, true ) ).isEqualTo( false );
	}

	@Test
	void testEvaluateWithAndOperator() {
		Settings settings = new MapSettings();
		SettingDependency dependency = new SettingDependency();
		dependency.setOperator( SettingDependency.Operator.AND );
		dependency.setKey( "enabled" );
		dependency.setDependencyValue( "true" );

		settings.set( "enabled", false );
		assertThat( dependency.evaluate( settings, false ) ).isEqualTo( false );
		assertThat( dependency.evaluate( settings, true ) ).isEqualTo( false );

		settings.set( "enabled", true );
		assertThat( dependency.evaluate( settings, false ) ).isEqualTo( false );
		assertThat( dependency.evaluate( settings, true ) ).isEqualTo( true );
	}

	@Test
	void testEvaluateWithOrOperator() {
		Settings settings = new MapSettings();
		SettingDependency dependency = new SettingDependency();
		dependency.setOperator( SettingDependency.Operator.OR );
		dependency.setKey( "enabled" );
		dependency.setDependencyValue( "true" );

		settings.set( "enabled", false );
		assertThat( dependency.evaluate( settings, false ) ).isEqualTo( false );
		assertThat( dependency.evaluate( settings, true ) ).isEqualTo( true );

		settings.set( "enabled", true );
		assertThat( dependency.evaluate( settings, false ) ).isEqualTo( true );
		assertThat( dependency.evaluate( settings, true ) ).isEqualTo( true );
	}

	@Test
	void testEvaluateWithXorOperator() {
		Settings settings = new MapSettings();
		SettingDependency dependency = new SettingDependency();
		dependency.setOperator( SettingDependency.Operator.XOR );
		dependency.setKey( "enabled" );
		dependency.setDependencyValue( "true" );

		settings.set( "enabled", false );
		assertThat( dependency.evaluate( settings, false ) ).isEqualTo( false );
		assertThat( dependency.evaluate( settings, true ) ).isEqualTo( true );

		settings.set( "enabled", true );
		assertThat( dependency.evaluate( settings, false ) ).isEqualTo( true );
		assertThat( dependency.evaluate( settings, true ) ).isEqualTo( false );
	}

	@Test
	void testEvaluateWithPath() {
		Settings settings = new MapSettings();
		SettingDependency dependency = new SettingDependency();
		dependency.setPath( "path" );
		dependency.setKey( "enabled" );
		dependency.setDependencyValue( "true" );

		settings.getNode( "path" ).set( "enabled", false );
		assertThat( dependency.evaluate( settings, false ) ).isEqualTo( false );
		assertThat( dependency.evaluate( settings, true ) ).isEqualTo( false );

		settings.getNode( "path" ).set( "enabled", true );
		assertThat( dependency.evaluate( settings, false ) ).isEqualTo( false );
		assertThat( dependency.evaluate( settings, true ) ).isEqualTo( true );
	}
}
