package com.avereon.xenon.notice;

import com.avereon.xenon.UiFactory;
import com.avereon.xenon.Xenon;
import com.avereon.xenon.task.Task;
import javafx.geometry.HPos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import lombok.CustomLog;
import lombok.Getter;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

@CustomLog
public class NoticePane extends GridPane {

	private final Xenon program;

	@Getter
	private final Notice notice;

	private final Node closeIcon;

	public NoticePane( Xenon program, Notice notice, boolean flyout ) {
		this.program = program;
		this.notice = notice;
		this.getStyleClass().addAll( flyout ? "notice-flyout" : "notice" );

		Node icon = program.getIconLibrary().getIcon( notice.getType().getIcon() );
		Label title = new Label( String.valueOf( notice.getTitle() ) );
		closeIcon = program.getIconLibrary().getIcon( "close" );

		// When
		DateFormat formatter = new SimpleDateFormat( "h:mm a" );
		formatter.setTimeZone( TimeZone.getDefault() );
		String timestamp = formatter.format( new Date( notice.getTimestamp() ) );
		Label when = new Label( timestamp );

		// Message
		Node message;
		if( notice.getMessage() instanceof Node ) {
			message = (Node)notice.getMessage();
		} else {
			Label label = new Label( notice.getFormattedMessage() );
			label.setWrapText( true );
			message = label;
		}

		title.getStyleClass().addAll( "notice-title" );
		//title.setMinWidth( Region.USE_PREF_SIZE );
		message.getStyleClass().addAll( "notice-message" );
		when.getStyleClass().addAll( "notice-when" );
		//when.setMinWidth( Region.USE_PREF_SIZE );

		GridPane.setConstraints( icon, 1, 1 );
		GridPane.setConstraints( title, 2, 1 );
		GridPane.setHgrow( title, Priority.ALWAYS );
		GridPane.setConstraints( when, 3, 1 );
		GridPane.setHalignment( when, HPos.RIGHT );
		GridPane.setHgrow( when, Priority.SOMETIMES );
		GridPane.setConstraints( closeIcon, 4, 1 );
		GridPane.setHalignment( closeIcon, HPos.RIGHT );
		GridPane.setConstraints( message, 1, 3 );
		GridPane.setColumnSpan( message, GridPane.REMAINING );
		GridPane.setHgrow( message, Priority.ALWAYS );

		this.setHgap( UiFactory.PAD );
		this.setVgap( UiFactory.PAD );

		getChildren().addAll( icon, title, when, closeIcon, message );
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
