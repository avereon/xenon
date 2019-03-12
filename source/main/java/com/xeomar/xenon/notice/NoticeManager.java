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
	}

	public List<Notice> getNotices() {
		return ((NoticeList)resource.getModel()).getNotices();
	}

	public void addNotice( Notice notice ) {
		((NoticeList)resource.getModel()).addNotice( notice );

		resource.refresh( program.getResourceManager() );

		Set<Tool> tools = getProgram().getWorkspaceManager().getActiveTools( NoticeTool.class );
		if( tools.size() > 0 ) {
			getProgram().getWorkspaceManager().getActiveWorkpane().setActiveTool( tools.iterator().next() );
		} else {
			Platform.runLater( () -> program.getWorkspaceManager().getActiveWorkspace().showNotice( notice ) );
			updateUnreadCount();
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

	public IntegerProperty unreadCountProperty() {
		return unreadCount;
	}

	public void readAll() {
		((NoticeList)resource.getModel()).getNotices().forEach( ( n ) -> n.setRead( true ) );
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

	private Integer getUnreadCount() {
		return unreadCount.getValue();
	}

	private void updateUnreadCount() {
		unreadCount.setValue( (int)((NoticeList)resource.getModel()).getNotices().stream().filter( ( n ) -> !n.isRead() ).count() );
	}

}
