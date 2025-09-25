package com.avereon.xenon.workspace;

import com.avereon.xenon.BaseXenonUIT;
import com.avereon.xenon.notice.Notice;
import com.avereon.xenon.notice.NoticePane;
import javafx.scene.layout.Pane;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WorkspaceNoticeUIT extends BaseXenonUIT {

	@Test
	void testUserCanCloseNotice() throws Exception {
		// Force a notice
		Notice notice = new Notice("title", "message");
		getProgram().getNoticeManager().addNotice( notice );
		getProgramWatcher().waitForEvent( NoticeEvent.ADDED, 1000 );

		// Find the notices pane
		Pane noticesPane = getWorkspace().getNoticePane();
		assertThat( noticesPane.getChildren() ).isNotEmpty();

		// Click the notice pane close button
		NoticePane noticePane = (NoticePane)noticesPane.getChildren().get( 0 );
		robot.clickOn( noticePane.getCloseButton() );

		// Verify the notice is closed
		assertThat( noticesPane.getChildren() ).isEmpty();
	}

}
