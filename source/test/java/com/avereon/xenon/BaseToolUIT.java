package com.avereon.xenon;

import com.avereon.xenon.workpane.Workpane;

import static org.assertj.core.api.Assertions.assertThat;

public abstract class BaseToolUIT extends BaseXenonUIT {

	protected static final String MAIN_MENU = "#menu-button-menu";

	protected void assertToolCount( Workpane pane, int count ) {
		assertThat( pane.getTools() ).hasSize( count );
	}

	protected void openMenuItem( String menuId, String menuItemId ) {
		robot.clickOn( MAIN_MENU );
		robot.moveTo( menuId );
		robot.clickOn( menuItemId );
	}

}
