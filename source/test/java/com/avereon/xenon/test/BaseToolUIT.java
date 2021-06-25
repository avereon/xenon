package com.avereon.xenon.test;

import com.avereon.xenon.workpane.Tool;
import com.avereon.xenon.workpane.Workpane;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public abstract class BaseToolUIT extends FxProgramUIT {

	protected void assertToolCount( Workpane pane, int count ) {
		try {
			assertThat( pane.getTools().size(), is( count ) );
		} catch( AssertionError error ) {
			for( Tool tool : pane.getTools() ) {
				System.out.println( "Tool: " + tool );
			}
			throw error;
		}
	}

}
