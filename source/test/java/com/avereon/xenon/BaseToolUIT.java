package com.avereon.xenon;

import com.avereon.xenon.workpane.Tool;
import com.avereon.xenon.workpane.Workpane;

import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

public abstract class BaseToolUIT extends BaseXenonUIT {

	protected static final String MAIN_MENU = "#menu-button-menu";

	protected void assertToolCount( Workpane pane, int count ) {
		Collection<Tool> tools = pane.getTools();
		assertThat( tools ).isNotNull();

		try {
			assertThat( tools ).hasSize( count );
		} catch( AssertionError error ) {
			tools.forEach( t -> System.out.println( "Tool: " + t ) );
			throw error;
		}
	}

}
