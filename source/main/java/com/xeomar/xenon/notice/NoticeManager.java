package com.xeomar.xenon.notice;

import com.xeomar.util.Controllable;
import com.xeomar.util.LogUtil;
import com.xeomar.xenon.Program;
import com.xeomar.xenon.resource.Resource;
import com.xeomar.xenon.resource.ResourceException;
import com.xeomar.xenon.resource.type.ProgramNoticeType;
import com.xeomar.xenon.tool.notice.NoticeTool;
import com.xeomar.xenon.workarea.Tool;
import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import org.slf4j.Logger;

import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class NoticeManager implements Controllable<NoticeManager> {

	private static final Logger log = LogUtil.get( MethodHandles.lookup().lookupClass() );

	private Program program;

	private Resource resource;

	private IntegerProperty unreadCount = new SimpleIntegerProperty();

	public NoticeManager( Program program ) {
		this.program = program;
		unreadCount.addListener( ( event ) -> updateActionIcon( unreadCount.get() ) );
	}

	public List<Notice> getNotices() {
		return ((NoticeList)resource.getModel()).getNotices();
	}

	public void addNotice( Notice notice ) {
		((NoticeList)resource.getModel()).addNotice( notice );
		setUnreadCount( getUnreadCount() + 1 );

		resource.refresh( program.getResourceManager() );

		Set<Tool> noticeTools = getProgram().getWorkspaceManager().getActiveWorkspace().getActiveWorkarea().getWorkpane().getTools( NoticeTool.class );
		if( noticeTools.size() > 0 ) {
			getProgram().getWorkspaceManager().getActiveWorkspace().getActiveWorkarea().getWorkpane().setActiveTool( noticeTools.iterator().next() );
		} else {
			Platform.runLater( () -> program.getWorkspaceManager().getActiveWorkspace().showNotice( notice ) );
		}
	}

	public void removeNotice( Notice notice ) {
		((NoticeList)resource.getModel()).removeNotice( notice );
		resource.refresh( program.getResourceManager() );
	}

	public void clearAll() {
		((NoticeList)resource.getModel()).clearAll();
		resource.refresh( program.getResourceManager() );
	}

	public Integer getUnreadCount() {
		return unreadCount.getValue();
	}

	public void setUnreadCount( Integer count ) {
		unreadCount.setValue( count );
	}

	public IntegerProperty unreadCountProperty() {
		return unreadCount;
	}

	public void readNotice() {
		setUnreadCount( Math.max( 0, getUnreadCount() - 1 ) );
	}

	public void readAll() {
		setUnreadCount( 0 );
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
		return restart();
	}

	@Override
	public NoticeManager awaitStart( long timeout, TimeUnit unit ) throws InterruptedException {
		return awaitRestart( timeout, unit );
	}

	@Override
	public NoticeManager restart() {
		try {
			resource = program.getResourceManager().createResource( ProgramNoticeType.URI );
			program.getResourceManager().loadResources( resource );
		} catch( ResourceException exception ) {
			exception.printStackTrace();
		}

		return this;
	}

	@Override
	public NoticeManager awaitRestart( long timeout, TimeUnit unit ) throws InterruptedException {
		return this;
	}

	@Override
	public NoticeManager stop() {
		program.getResourceManager().saveResources( resource );
		return this;
	}

	@Override
	public NoticeManager awaitStop( long timeout, TimeUnit unit ) throws InterruptedException {
		return this;
	}

	private NoticeList getNoticeList() {
		return (NoticeList)resource.getModel();
	}

	private void updateActionIcon( int count ) {
		log.warn( "Unread notice count: " + count );

		Platform.runLater( () -> getProgram().getActionLibrary().getAction( "notice" ).setIcon( count == 0 ? "notice" : "notice-unread" ) );
	}

}
