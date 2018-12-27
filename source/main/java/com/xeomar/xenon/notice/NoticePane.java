package com.xeomar.xenon.notice;

import com.xeomar.util.LogUtil;
import com.xeomar.xenon.Program;
import com.xeomar.xenon.resource.ResourceException;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import org.slf4j.Logger;

import java.lang.invoke.MethodHandles;

public class NoticePane extends GridPane {

	private static final Logger log = LogUtil.get( MethodHandles.lookup().lookupClass() );

	private Program program;

	private Node closeIcon;

	public NoticePane( Program program, Notice notice ) {
		this.program = program;
		this.getStyleClass().addAll( "notice", "padded" );

		Node noticeIcon = program.getIconLibrary().getIcon( "notice" );
		Label title = new Label( notice.getTitle() );
		closeIcon = program.getIconLibrary().getIcon( "remove" );
		Label message = new Label( notice.getMessage() );
		message.setWrapText( true );

		noticeIcon.getStyleClass().addAll( "padded" );
		closeIcon.getStyleClass().addAll( "padded" );
		title.getStyleClass().addAll( "notice-title" );
		message.getStyleClass().addAll( "notice-message" );

		GridPane.setConstraints( noticeIcon, 1, 1 );
		GridPane.setConstraints( title, 2, 1 );
		GridPane.setHgrow( title, Priority.SOMETIMES );
		GridPane.setConstraints( closeIcon, 3, 1 );
		GridPane.setConstraints( message, 2, 2 );
		GridPane.setColumnSpan( message, 2 );

		getChildren().addAll( noticeIcon, title, closeIcon, message );

		onMouseClickedProperty().set( ( event ) -> {
			try {
				program.getResourceManager().open( notice.getAction() );
			} catch( ResourceException exception ) {
				log.warn( "Unable to execute notice action", exception );
			} finally {
				event.consume();
			}
		} );
	}

	public Node getCloseButton() {
		return closeIcon;
	}

}
