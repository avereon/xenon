package com.avereon.xenon.workspace;

import com.avereon.xenon.UiFactory;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class ProgramWorkspaceDefaultSceneSizeUIT extends ProgramWorkspaceUIT {

	@Test
	void execute() {
		Stage stage = program.getWorkspaceManager().getActiveStage();
		assertThat( stage.isShowing(), is( true ) );
		assertThat( stage.getScene().getWidth(), is( UiFactory.DEFAULT_WIDTH ) );
		assertThat( stage.getScene().getHeight(), is( UiFactory.DEFAULT_HEIGHT ) );
	}

}
