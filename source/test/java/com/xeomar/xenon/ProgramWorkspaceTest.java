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
	public void testCloseProgram() throws Exception {
		// FIXME This test does not work on Monocle
		if( "Monocle".equals( System.getProperty( "glass.platform" ) ) ) return;

		closeCurrentWindow();
		clickOn( "No" );
		closeCurrentWindow();
		clickOn( "Yes" );

		// Unfortunately there is little to test at this point because the program
		// still has to be running for the tests not to hang. So the program does
		// not actually go through the shutdown sequence at this point.
	}

}
