package com.avereon.xenon.notice;

import com.avereon.xenon.Program;
import com.avereon.xenon.UiFactory;
import com.avereon.xenon.task.Task;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import lombok.CustomLog;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

@CustomLog
public class NoticePane extends GridPane {

	private final Program program;

	private final Notice notice;

	private final Node closeIcon;

	public NoticePane( Program program, Notice notice, boolean flyout ) {
		this.program = program;
		this.notice = notice;
		this.getStyleClass().addAll( flyout ? "notice-flyout" : "notice" );

		Node noticeIcon = program.getIconLibrary().getIcon( notice.getType().getIcon() );
		Label title = new Label( String.valueOf( notice.getTitle() ) );
		closeIcon = program.getIconLibrary().getIcon( "close" );
		Node message;
		if( notice.getMessage() instanceof Node ) {
			message = (Node)notice.getMessage();
		} else {
			Label label = new Label( notice.getFormattedMessage() );
			label.setWrapText( true );
			message = label;
		}

		DateFormat formatter = new SimpleDateFormat( "h:mm a" );
		formatter.setTimeZone( TimeZone.getDefault() );
		String timestamp = formatter.format( new Date( notice.getTimestamp() ) );
		Label when = new Label( timestamp );
		when.setMinWidth( Region.USE_PREF_SIZE );

		//noticeIcon.getStyleClass().addAll( "padded" );
		//closeIcon.getStyleClass().addAll( "padded" );
		title.getStyleClass().addAll( "notice-title" );
		message.getStyleClass().addAll( "notice-message" );
		when.getStyleClass().addAll( "notice-when" );

		GridPane.setConstraints( noticeIcon, 1, 1 );
		GridPane.setConstraints( title, 2, 1 );
		GridPane.setHgrow( title, Priority.ALWAYS );
		GridPane.setConstraints( closeIcon, 3, 1 );
		GridPane.setHalignment( closeIcon, HPos.RIGHT );
		GridPane.setConstraints( message, 2, 2 );
		GridPane.setHgrow( message, Priority.ALWAYS );
		GridPane.setConstraints( when, 3, 2 );

		this.setHgap( UiFactory.PAD );
		this.setVgap( UiFactory.PAD );

		getChildren().addAll( noticeIcon, title, closeIcon, message, when );
	}

	public Notice getNotice() {
		return notice;
	}

	public void executeNoticeAction() {
		Runnable action = notice.getAction();
		if( action == null ) return;
		program.getTaskManager().submit( Task.of( "", action ) );
	}

	public Node getCloseButton() {
		return closeIcon;
	}

}
