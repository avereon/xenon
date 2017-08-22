package com.xeomar.xenon;

import javafx.stage.Stage;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class ProgramWorkspaceTest extends FxProgramTestCase {

	@Test
	public void testWorkspaceWindowTitle() throws Exception {
		Stage stage = program.getWorkspaceManager().getActiveWorkspace().getStage();
		String workareaName = program.getWorkspaceManager().getActiveWorkspace().getActiveWorkarea().getName();
		assertThat( stage.isShowing(), is( true ) );
		assertThat( stage.getTitle(), is( workareaName + " - " + metadata.getName() ) );
	}

	@Test
	public void testWorkspaceDefaultSceneSize() throws Exception {
		Stage stage = program.getWorkspaceManager().getActiveWorkspace().getStage();
		assertThat( stage.isShowing(), is( true ) );
		assertThat( stage.getScene().getWidth(), is( 960d ) );
		assertThat( stage.getScene().getHeight(), is( 540d ) );
	}

	@Test
	public void testCloseApplication() throws Exception {
		//Platform.runLater( program::requestExit );
		//Platform.runLater( () -> program.requestExit( true ) );

		System.out.println( "Try to click on the YES button" );
		//robot.clickOn( ".dialog-pane > .button-bar > .container" );
	}

}
