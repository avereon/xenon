package com.xeomar.xenon.tool;

import com.xeomar.xenon.node.Node;
import com.xeomar.xenon.settings.Settings;
import com.xeomar.xenon.util.TextUtil;

public class SettingDependency extends Node {

	public enum Operator {
		NOT,
		AND,
		OR,
		XOR
	}

	public static final String OPERATOR = "operator";

	public static final String KEY = "key";

	public static final String VALUE = "value";

	public SettingDependency( String key, String value ) {
		this( Operator.AND, key, value );
	}

	public SettingDependency( Operator operator, String key, String value ) {
		setValue( OPERATOR, operator != null ? operator : Operator.AND );
		setValue( KEY, key );
		setValue( VALUE, value );
		definePrimaryKey( OPERATOR, KEY, VALUE );
		defineBusinessKey( OPERATOR, KEY, VALUE );
		defineReadOnly( KEY, VALUE, OPERATOR );
	}

	public String getKey() {
		return getValue( KEY );
	}

	public String getDependencyValue() {
		return getValue( VALUE );
	}

	public Operator getOperator() {
		return getValue( OPERATOR );
	}

	public boolean evaluate( Settings settings ) {
		return evaluate( settings, true );
	}

	public boolean evaluate( Settings settings, boolean pass ) {
		String key = getKey();
		String value = getDependencyValue();
		Operator operator = getOperator();
		if( operator == null ) operator = Operator.AND;

		boolean equal = TextUtil.areEqual( value, settings.getString( key, null ) );

		switch( operator ) {
			case NOT: {
				pass = pass & !equal;
				break;
			}
			case AND: {
				pass = pass & equal;
				break;
			}
			case OR: {
				pass = pass | equal;
				break;
			}
			case XOR: {
				pass = pass ^ equal;
				break;
			}
		}

		return pass;
	}

	@Override
	public String toString() {
		String key = getKey();
		String value = getDependencyValue();
		Operator operator = getOperator();
		return operator + " " + key + " = " + value;
	}

}
