package com.avereon.xenon.tool.settings;

import com.avereon.data.Node;
import com.avereon.settings.Settings;
import com.avereon.util.TextUtil;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class SettingDependency extends Node {

	public enum Operator {
		NOT,
		AND,
		OR,
		XOR
	}

	private static final String OPERATOR = "operator";

	private static final String PATH = "path";

	private static final String KEY = "key";

	private static final String VALUE = "value";

	private static final String DEPENDENCIES = "dependencies";

	public SettingDependency() {
		setValue( DEPENDENCIES, new CopyOnWriteArrayList<SettingDependency>() );
		setOperator( Operator.AND );
		definePrimaryKey( OPERATOR, PATH, KEY, VALUE );
		defineNaturalKey( OPERATOR, PATH, KEY, VALUE );
	}

	public Operator getOperator() {
		return getValue( OPERATOR );
	}

	public void setOperator( Operator operator ) {
		setValue( OPERATOR, operator );
	}

	public String getPath() {
		return getValue( PATH );
	}

	public void setPath( String path ) {
		setValue( PATH, path );
	}

	public String getKey() {
		return getValue( KEY );
	}

	public void setKey( String key ) {
		setValue( KEY, key );
	}

	public String getDependencyValue() {
		return getValue( VALUE );
	}

	public void setDependencyValue( String value ) {
		setValue( VALUE, value );
	}

	public List<SettingDependency> getDependencies() {
		return Collections.unmodifiableList( getValue( DEPENDENCIES ) );
	}

	public void addDependency( SettingDependency dependency ) {
		List<SettingDependency> dependencies = getValue( DEPENDENCIES );
		dependencies.add( dependency );
	}

	public boolean evaluate( Settings settings, boolean pass ) {
		String path = getPath();
		String key = getKey();
		String value = getDependencyValue();
		Operator operator = getOperator();
		if( operator == null ) operator = Operator.AND;

		if( path != null ) settings = settings.getNode( path );
		boolean match = TextUtil.areEqual( value, settings.get( key ) );

		switch( operator ) {
			case NOT -> pass &= !match;
			case AND -> pass &= match;
			case OR -> pass |= match;
			case XOR -> pass ^= match;
		}

		return pass;
	}

	public static boolean evaluate( List<SettingDependency> dependencies, Settings settings ) {
		boolean pass = true;

		for( SettingDependency dependency : dependencies ) {
			pass = dependency.evaluate( settings, pass );
		}

		return pass;
	}

	@Override
	public String toString() {
		String path = getPath();
		String key = getKey();
		String value = getDependencyValue();
		Operator operator = getOperator();
		return operator + " " + (path == null ? " " : path + "/") + key + " = " + value;
	}

}
