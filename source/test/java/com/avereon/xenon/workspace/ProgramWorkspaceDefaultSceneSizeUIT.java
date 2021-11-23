package com.avereon.xenon.workspace;

import com.avereon.xenon.UiFactory;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ProgramWorkspaceDefaultSceneSizeUIT extends ProgramWorkspaceUIT {

	@Test
	void execute() {
		Stage stage = getProgram().getWorkspaceManager().getActiveStage();
		assertThat( stage.isShowing() ).isTrue();
		assertThat( stage.getScene().getWidth() ).isEqualTo( UiFactory.DEFAULT_WIDTH );
		assertThat( stage.getScene().getHeight() ).isEqualTo( UiFactory.DEFAULT_HEIGHT );
	}

}
