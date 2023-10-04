package com.avereon.xenon.workspace;

import com.avereon.event.EventType;
import com.avereon.xenon.notice.Notice;
import lombok.Getter;

@Getter
public class NoticeEvent extends WorkspaceEvent {

	public static final EventType<NoticeEvent> NOTICE = new EventType<>( WorkspaceEvent.ANY, "NOTICE" );

	public static final EventType<NoticeEvent> ANY = NOTICE;

	public static final EventType<NoticeEvent> ADDED = new EventType<>( NOTICE, "ADDED" );

	public static final EventType<NoticeEvent> REMOVED = new EventType<>( NOTICE, "REMOVED" );

	private final Notice notice;

	public NoticeEvent( Object source, EventType<? extends NoticeEvent> type, Workspace workspace, Notice notice ) {
		super( source, type, workspace );
		this.notice = notice;
	}

}
