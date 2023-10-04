package com.avereon.xenon.workspace;

import com.avereon.xenon.BaseXenonUIT;
import com.avereon.xenon.notice.Notice;
import javafx.scene.layout.Pane;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class NoticeInteractionUIT extends BaseXenonUIT {

	@Test
	void testUserCanCloseNotice() throws Exception {
		// Force a notice
		Notice notice = new Notice("title", "message");
		getProgram().getNoticeManager().addNotice( notice );
		getProgramEventWatcher().waitForEvent( NoticeEvent.ADDED, 1000 );

		// Find the notice pane
		Pane noticePane = getWorkspace().getNoticePane();
		List<Notice> notices = getProgram().getWorkspaceManager().getActiveWorkspace().getVisibleNotices();
		assertThat( notices).isNotEmpty();

		// TODO Click the notice pane close button

		// TODO Verify the notice is closed

	}

}
