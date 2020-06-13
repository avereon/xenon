package com.avereon.xenon;

import com.avereon.xenon.workpane.Tool;
import com.avereon.xenon.workpane.Workpane;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public abstract class BaseToolUIT extends FxProgramUIT {

	protected void assertToolCount( Workpane pane, int count ) {
		int actual = pane.getTools().size();
		if( actual != count ) {
			for( Tool tool : pane.getTools() ) {
				System.out.println( "Tool: " + tool );
			}
		}
		assertThat( actual , is( count ) );
	}

}
