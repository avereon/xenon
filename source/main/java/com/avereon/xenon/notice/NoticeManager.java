package com.avereon.xenon.notice;

import com.avereon.settings.Settings;
import com.avereon.skill.Controllable;
import com.avereon.xenon.ManagerSettings;
import com.avereon.xenon.ProgramEvent;
import com.avereon.xenon.Xenon;
import com.avereon.xenon.asset.Asset;
import com.avereon.xenon.asset.AssetManager;
import com.avereon.xenon.asset.exception.AssetException;
import com.avereon.xenon.asset.type.ProgramNoticeType;
import com.avereon.xenon.scheme.FaultScheme;
import com.avereon.xenon.task.Task;
import com.avereon.xenon.tool.NoticeTool;
import com.avereon.xenon.workpane.Tool;
import com.avereon.zarra.javafx.Fx;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import lombok.CustomLog;
import lombok.Getter;

import java.net.URI;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@CustomLog
public class NoticeManager implements Controllable<NoticeManager> {

	@Getter
	private final Xenon program;

	private final List<Notice> startupNotices;

	private final IntegerProperty unreadCount;

	private Asset asset;

	public NoticeManager( Xenon program ) {
		this.program = program;
		this.startupNotices = new CopyOnWriteArrayList<>();
		this.unreadCount = new SimpleIntegerProperty();
	}

	@Override
	public boolean isRunning() {
		return true;
	}

	@Override
	public NoticeManager start() {
		log.atTrace().log( "Notice manager starting..." );
		try {
			getProgram().register( ProgramEvent.STARTED, e -> startupNotices.forEach( this::addNotice ) );
			asset = getProgram().getAssetManager().createAsset( ProgramNoticeType.URI );
			getProgram().getAssetManager().loadAssets( asset );
			unreadCountProperty().addListener( ( p, o, n ) -> updateNoticeIcon( n.intValue() ) );
		} catch( AssetException exception ) {
			log.atWarn( exception ).log( "Error starting notice manager." );
		}
		log.atDebug().log( "Notice manager started." );

		return this;
	}

	@Override
	public NoticeManager stop() {
		log.atTrace().log( "Notice manager stopping..." );
		getProgram().getAssetManager().saveAssets( asset );
		log.atDebug().log( "Notice manager stopped." );
		return this;
	}

	public List<Notice> getNotices() {
		return getNoticeList().getNotices();
	}

	public void error( Object title, Object message, Object... parameters ) {
		fault( title, message, null, Notice.Type.ERROR, parameters );
	}

	public void warning( Object title, Object message, Object... parameters ) {
		fault( title, message, null, Notice.Type.WARN, parameters );
	}

	void fault( Object title, Object message, Throwable throwable, Notice.Type type, Object... parameters ) {
		Notice notice = new Notice( title, message, throwable, parameters ).setType( type );
		if( type == Notice.Type.ERROR ) notice.setBalloonStickiness( Notice.Balloon.ALWAYS );
		notice.setAction( () -> {
			AssetManager manager = getProgram().getAssetManager();
			URI uri = URI.create( String.format( "%s:%d", FaultScheme.ID, System.identityHashCode( throwable ) ) );
			manager.openAsset( uri, throwable );
		} );
		addNotice( notice );
	}

	public void addNotice( Notice notice ) {
		if( notice.getId() == null ) throw new NullPointerException( "Notice id cannot be null: id=" + notice.getId() );

		if( !getProgram().getWorkspaceManager().isUiReady() ) {
			startupNotices.add( notice );
			log.atDebug().log( "Notice added to startup list: %s", notice );
			return;
		}

		Fx.run( () -> {
			getNoticeList().addNotice( notice );
			Set<Tool> tools = getProgram().getWorkspaceManager().getActiveWorkpaneTools( NoticeTool.class );
			NoticeTool activeNoticeTool = (NoticeTool)tools.stream().filter( Tool::isActive ).findAny().orElse( null );

			if( tools.isEmpty() ) {
				getProgram().getWorkspaceManager().getActiveWorkspace().showNotice( notice );
				updateUnreadCount();
			} else if( activeNoticeTool != null ) {
				markAllAsRead();
			} else {
				getProgram().getWorkspaceManager().getActiveWorkpane().setActiveTool( tools.stream().findAny().orElse( null ) );
			}
		} );
	}

	public void removeNotice( Notice notice ) {
		getNoticeList().removeNotice( notice );
	}

	public void removeAll() {
		getNoticeList().removeAll();
	}

	public IntegerProperty unreadCountProperty() {
		return unreadCount;
	}

	public Notice.Type getHighestUnreadNoticeType() {
		return Notice.Type.values()[ getUnreadNotices().stream().mapToInt( ( n ) -> n.getType().ordinal() ).max().orElse( Notice.Type.NONE.ordinal() ) ];
	}

	public void markAllAsRead() {
		getNoticeList().getNotices().forEach( ( n ) -> n.setRead( true ) );
		updateUnreadCount();
	}

	public void readNotice( Notice notice ) {
		notice.setRead( true );
		updateUnreadCount();
	}

	public Settings getSettings() {
		return getProgram().getSettingsManager().getSettings( ManagerSettings.NOTICE );
	}

	String getThrowableTitle( Throwable throwable ) {
		if( throwable instanceof Task.InternalException ) throwable = throwable.getCause();
		return throwable.getClass().getSimpleName();
	}

	String getThrowableMessage( Throwable throwable ) {
		if( throwable instanceof Task.InternalException ) throwable = throwable.getCause();
		return throwable.getLocalizedMessage();
	}

	private NoticeModel getNoticeList() {
		return asset.getModel();
	}

	private void updateUnreadCount() {
		int unreadNoticeCount = getUnreadNotices().size();

		unreadCount.setValue( unreadNoticeCount );
	}

	private void updateNoticeIcon( int unreadNoticeCount ) {
		// Update the action icon
		String actionIcon = "notice";
		if( unreadNoticeCount == 0 ) {
			actionIcon += "-none";
		} else {
			actionIcon += "-unread";
		}
		getProgram().getActionLibrary().getAction( "notice" ).setIcon( actionIcon );
	}

	private List<Notice> getUnreadNotices() {
		return getNoticeList().getNotices().stream().filter( ( n ) -> !n.isRead() ).collect( Collectors.toList() );
	}

}
