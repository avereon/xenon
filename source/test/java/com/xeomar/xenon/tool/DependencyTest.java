package com.xeomar.xenon.tool;

import com.xeomar.xenon.settings.MockSettings;
import com.xeomar.xenon.settings.Settings;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class DependencyTest {

	@Test
	public void testConstructorWithKeyAndValue() {
		Dependency dependency = new Dependency( "enabled", "true" );
		assertThat( dependency.getOperator(), is( Dependency.Operator.AND ) );
		assertThat( dependency.getKey(), is( "enabled" ) );
		assertThat( dependency.getDependencyValue(), is( "true" ) );
	}

	@Test
	public void testConstructorWithOperatorKeyAndValue() {
		Dependency dependency = new Dependency( Dependency.Operator.OR, "enabled", "true" );
		assertThat( dependency.getOperator(), is( Dependency.Operator.OR ) );
		assertThat( dependency.getKey(), is( "enabled" ) );
		assertThat( dependency.getDependencyValue(), is( "true" ) );
	}

	@Test
	public void testEvaluateWithNotOperator() {
		Settings settings = new MockSettings();
		Dependency dependency = new Dependency( Dependency.Operator.NOT, "enabled", "true" );

		settings.set( "enabled", false );
		assertThat( dependency.evaluate( settings, false ), is( false ) );
		assertThat( dependency.evaluate( settings, true ), is( true ) );

		settings.set("enabled", true );
		assertThat( dependency.evaluate( settings, false ), is( false ) );
		assertThat( dependency.evaluate( settings, true ), is( false ) );
	}

	@Test
	public void testEvaluateWithAndOperator() {
		Settings settings = new MockSettings();
		Dependency dependency = new Dependency( Dependency.Operator.AND, "enabled", "true" );

		settings.set( "enabled", false );
		assertThat( dependency.evaluate( settings, false ), is( false ) );
		assertThat( dependency.evaluate( settings, true ), is( false ) );

		settings.set("enabled", true );
		assertThat( dependency.evaluate( settings, false ), is( false ) );
		assertThat( dependency.evaluate( settings, true ), is( true ) );
	}

	@Test
	public void testEvaluateWithOrOperator() {
		Settings settings = new MockSettings();
		Dependency dependency = new Dependency( Dependency.Operator.OR, "enabled", "true" );

		settings.set( "enabled", false );
		assertThat( dependency.evaluate( settings, false ), is( false ) );
		assertThat( dependency.evaluate( settings, true ), is( true ) );

		settings.set("enabled", true );
		assertThat( dependency.evaluate( settings, false ), is( true ) );
		assertThat( dependency.evaluate( settings, true ), is( true ) );
	}


	@Test
	public void testEvaluateWithXorOperator() {
		Settings settings = new MockSettings();
		Dependency dependency = new Dependency( Dependency.Operator.XOR, "enabled", "true" );

		settings.set( "enabled", false );
		assertThat( dependency.evaluate( settings, false ), is( false ) );
		assertThat( dependency.evaluate( settings, true ), is( true ) );

		settings.set("enabled", true );
		assertThat( dependency.evaluate( settings, false ), is( true ) );
		assertThat( dependency.evaluate( settings, true ), is( false ) );
	}

}
