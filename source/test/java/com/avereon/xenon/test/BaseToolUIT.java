package com.avereon.xenon.test;

import com.avereon.xenon.workpane.Tool;
import com.avereon.xenon.workpane.Workpane;

import java.util.Collection;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public abstract class BaseToolUIT extends FxProgramUIT {

	protected void assertToolCount( Workpane pane, int count ) {
		Collection<Tool> tools = pane.getTools();

		try {
			assertThat( tools.size(), is( count ) );
		} catch( AssertionError error ) {
			tools.forEach( t -> System.out.println( "Tool: " + t ) );
			throw error;
		}
	}

}
