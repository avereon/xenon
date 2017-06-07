package com.parallelsymmetry.essence;

import com.parallelsymmetry.essence.event.ProgramStartedEvent;
import com.parallelsymmetry.essence.testutil.FxApplicationTestCase;
import javafx.stage.Stage;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class ProgramWorkspaceTest extends FxApplicationTestCase {

	@Test
	public void testWorkspaceDefaultSceneSize() throws Exception {
		waitForEvent( ProgramStartedEvent.class );

		Stage stage = program.getWorkspaceManager().getActiveWorkspace().getStage();
		assertThat( stage.isShowing(), is( true ) );
		assertThat( stage.getScene().getWidth(), is( 960d ) );

		// FIXME This is a little big on Linux, it ended up 512.5
		assertThat( stage.getScene().getHeight(), is( 540d ) );
	}

	@Test
	public void testWorkspaceWindowTitle() throws Exception {
		waitForEvent( ProgramStartedEvent.class );

		Stage stage = program.getWorkspaceManager().getActiveWorkspace().getStage();
		String workareaName = program.getWorkspaceManager().getActiveWorkspace().getActiveWorkarea().getName();
		assertThat( stage.isShowing(), is( true ) );
		assertThat( stage.getTitle(), is( workareaName + " - " + metadata.getName() ) );
	}

}
