package com.avereon.xenon.workpane;

import org.assertj.core.api.AbstractAssert;

public class ToolEventAssert extends AbstractAssert<ToolEventAssert, MockTool.MethodCall> {

	private ToolEventAssert( MockTool.MethodCall actual ) {
		super( actual, ToolEventAssert.class );
	}

	public static ToolEventAssert assertThat( MockTool.MethodCall actual ) {
		return new ToolEventAssert( actual );
	}

	public ToolEventAssert hasMethod( String method ) {
		if( actual == null || !actual.method.equals( method ) ) failWithMessage( "Expected event method %s but was %s", method, actual.method );
		return this;
	}
}
