package com.parallelsymmetry.essence;

import com.parallelsymmetry.essence.testutil.FxApplicationTestCase;
import javafx.stage.Stage;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class ProgramWorkspaceTest extends FxApplicationTestCase {

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

		// FIXME This test will fail when run with other application tests
		// This test will pass when run alone, but when run after a previous
		// application test it fails on Linux.
		assertThat( stage.getScene().getHeight(), is( 540d ) );
	}

}
