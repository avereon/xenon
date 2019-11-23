package com.avereon.xenon.notice;

import com.avereon.util.Controllable;
import com.avereon.util.LogUtil;
import com.avereon.xenon.Program;
import com.avereon.xenon.resource.Resource;
import com.avereon.xenon.resource.ResourceException;
import com.avereon.xenon.resource.type.ProgramNoticeType;
import com.avereon.xenon.tool.notice.NoticeTool;
import com.avereon.xenon.workarea.Tool;
import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import org.slf4j.Logger;

import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class NoticeManager implements Controllable<NoticeManager> {

	private static final Logger log = LogUtil.get( MethodHandles.lookup().lookupClass() );

	private Program program;

	private Resource resource;

	private IntegerProperty unreadCount = new SimpleIntegerProperty();

	public NoticeManager( Program program ) {
		this.program = program;
	}

	public List<Notice> getNotices() {
		return getNoticeList().getNotices();
	}

	public void error( Throwable throwable ) {
		error( throwable.getClass().getSimpleName(), throwable.getMessage(), throwable );
	}

	public void error( Object message, Throwable throwable, String... parameters ) {
		error( throwable.getClass().getSimpleName(), message, throwable, parameters );
	}

	public void error( Object title, Object message, String... parameters ) {
		error( title, message, null, parameters );
	}

	public void error( Object title, Object message, Throwable throwable, String... parameters ) {
		addNotice( new Notice( title, message, throwable, parameters ).setType( Notice.Type.ERROR ).setBalloonStickiness( Notice.Balloon.ALWAYS ) );
	}

	public void warning( Object title, Object message, String... parameters ) {
		warning( title, message, null, parameters );
	}

	public void warning( Object title, Object message, Throwable throwable, String... parameters ) {
		addNotice( new Notice( title, message, throwable, parameters ).setType( Notice.Type.WARN ) );
	}

	public void addNotice( Notice notice ) {
		getNoticeList().addNotice( notice );
		resource.refresh( program.getResourceManager() );

		Platform.runLater( () -> {
			Set<Tool> tools = getProgram().getWorkspaceManager().getActiveTools( NoticeTool.class );
			if( tools.size() > 0 ) {
				getProgram().getWorkspaceManager().getActiveWorkpane().setActiveTool( tools.iterator().next() );
			} else {
				getProgram().getWorkspaceManager().getActiveWorkspace().showNotice( notice );
				updateUnreadCount();
			}
		} );
	}

	public void removeNotice( Notice notice ) {
		getNoticeList().removeNotice( notice );
		resource.refresh( program.getResourceManager() );
	}

	public void removeAll() {
		getNoticeList().clearAll();
		resource.refresh( program.getResourceManager() );
	}

	public IntegerProperty unreadCountProperty() {
		return unreadCount;
	}

	public Notice.Type getUnreadNoticeType() {
		return Notice.Type.values()[ getUnreadNotices().stream().mapToInt( ( n ) -> n.getType().ordinal() ).max().orElse( Notice.Type.NONE.ordinal() ) ];
	}

	public void readAll() {
		getNoticeList().getNotices().forEach( ( n ) -> n.setRead( true ) );
		updateUnreadCount();
	}

	public void readNotice( Notice notice ) {
		notice.setRead( true );
		updateUnreadCount();
	}

	public Program getProgram() {
		return this.program;
	}

	@Override
	public boolean isRunning() {
		return true;
	}

	@Override
	public NoticeManager start() {
		try {
			resource = program.getResourceManager().createResource( ProgramNoticeType.URI );
			program.getResourceManager().loadResources( resource );
			// TODO Register an event listener to show unread messages after the program is finished starting
			// At startup there may be notices that need to be shown but the workspace has not been restored yet
		} catch( ResourceException exception ) {
			exception.printStackTrace();
		}

		return this;
	}

	@Override
	public NoticeManager stop() {
		// TODO Unregister an event listener to show unread messages when there is an active workspace
		program.getResourceManager().saveResources( resource );
		return this;
	}

	private NoticeList getNoticeList() {
		return (NoticeList)resource.getModel();
	}

	private void updateUnreadCount() {
		unreadCount.setValue( getUnreadNotices().size() );
	}

	private List<Notice> getUnreadNotices() {
		return getNoticeList().getNotices().stream().filter( ( n ) -> !n.isRead() ).collect( Collectors.toList() );
	}

}
