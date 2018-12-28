package com.xeomar.xenon.notice;

import com.xeomar.util.Controllable;
import com.xeomar.xenon.Program;
import com.xeomar.xenon.resource.Resource;
import com.xeomar.xenon.resource.ResourceException;
import com.xeomar.xenon.resource.type.ProgramNoticeType;
import javafx.application.Platform;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class NoticeManager implements Controllable<NoticeManager> {

	private Program program;

	private Resource resource;

	public NoticeManager( Program program ) {
		this.program = program;
	}

	public List<Notice> getNotices() {
		return ((NoticeList)resource.getModel()).getNotices();
	}

	public void addNotice( Notice notice ) {
		((NoticeList)resource.getModel()).addNotice( notice );
		program.getResourceManager().saveResources( resource );

		// TODO Show the new notice in the workspace notice area
		Platform.runLater( () -> program.getWorkspaceManager().getActiveWorkspace().showNotice( notice ) );
	}

	public void removeNotice( Notice notice ) {
		((NoticeList)resource.getModel()).removeNotice( notice );
		program.getResourceManager().saveResources( resource );
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

}
