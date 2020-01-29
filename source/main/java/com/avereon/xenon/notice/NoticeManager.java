package com.avereon.xenon.notice;

import com.avereon.settings.Settings;
import com.avereon.util.Controllable;
import com.avereon.util.LogUtil;
import com.avereon.xenon.ManagerSettings;
import com.avereon.xenon.Program;
import com.avereon.xenon.asset.Asset;
import com.avereon.xenon.asset.AssetException;
import com.avereon.xenon.asset.type.ProgramNoticeType;
import com.avereon.xenon.task.TaskException;
import com.avereon.xenon.tool.notice.NoticeTool;
import com.avereon.xenon.workpane.Tool;
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

	private Asset asset;

	private IntegerProperty unreadCount = new SimpleIntegerProperty();

	public NoticeManager( Program program ) {
		this.program = program;
	}

	public List<Notice> getNotices() {
		return getNoticeList().getNotices();
	}

	public void error( Throwable throwable ) {
		error( getThrowableTitle( throwable ), getThrowableMessage( throwable ), throwable );
	}

	public void error( Object message, Throwable throwable, Object... parameters ) {
		error( getThrowableTitle( throwable ), message, throwable, parameters );
	}

	public void error( Object title, Object message, Object... parameters ) {
		error( title, message, null, parameters );
	}

	public void error( Object title, Object message, Throwable throwable, Object... parameters ) {
		addNotice( new Notice( title, message, throwable, parameters ).setType( Notice.Type.ERROR ).setBalloonStickiness( Notice.Balloon.ALWAYS ) );
	}

	public void warning( Object title, Object message, Object... parameters ) {
		warning( title, message, null, parameters );
	}

	public void warning( Throwable throwable ) {
		warning( getThrowableTitle( throwable ), getThrowableMessage( throwable ), throwable );
	}

	public void warning( Object message, Throwable throwable, Object... parameters ) {
		warning( getThrowableTitle( throwable ), message, throwable, parameters );
	}

	public void warning( Object title, Object message, Throwable throwable, Object... parameters ) {
		addNotice( new Notice( title, message, throwable, parameters ).setType( Notice.Type.WARN ) );
	}

	public void addNotice( Notice notice ) {
		getNoticeList().addNotice( notice );

		switch( notice.getType() ) {
			case ERROR: {
				error( notice );
				break;
			}
			case WARN: {
				warning( notice );
				break;
			}
		}

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
	}

	public void removeAll() {
		getNoticeList().clearAll();
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

	public Settings getSettings() {
		return getProgram().getSettingsManager().getSettings( ManagerSettings.NOTICE );
	}

	@Override
	public boolean isRunning() {
		return true;
	}

	@Override
	public NoticeManager start() {
		try {
			asset = program.getAssetManager().createAsset( ProgramNoticeType.URI );
			program.getAssetManager().loadAssets( asset );
			// TODO Register an event listener to show unread messages after the program is finished starting
			// At startup there may be notices that need to be shown but the workspace has not been restored yet
		} catch( AssetException exception ) {
			exception.printStackTrace();
		}

		return this;
	}

	@Override
	public NoticeManager stop() {
		// TODO Unregister an event listener to show unread messages when there is an active workspace
		program.getAssetManager().saveAssets( asset );
		return this;
	}

	private String getThrowableTitle( Throwable throwable ) {
		if( throwable instanceof TaskException ) throwable = throwable.getCause();
		return throwable.getClass().getSimpleName();
	}

	private String getThrowableMessage( Throwable throwable ) {
		if( throwable instanceof TaskException ) throwable = throwable.getCause();
		return throwable.getLocalizedMessage();
	}

	private void error( Notice notice ) {
		log.error( notice.getFormattedMessage(), notice.getThrowable() );
	}

	private void warning( Notice notice ) {
		log.warn( notice.getFormattedMessage(), notice.getThrowable() );
	}

	private NoticeList getNoticeList() {
		return asset.getModel();
	}

	private void updateUnreadCount() {
		unreadCount.setValue( getUnreadNotices().size() );
	}

	private List<Notice> getUnreadNotices() {
		return getNoticeList().getNotices().stream().filter( ( n ) -> !n.isRead() ).collect( Collectors.toList() );
	}

}
