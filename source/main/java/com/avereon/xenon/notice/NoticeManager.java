package com.avereon.xenon.notice;

import com.avereon.settings.Settings;
import com.avereon.util.Controllable;
import com.avereon.util.Log;
import com.avereon.xenon.ManagerSettings;
import com.avereon.xenon.Program;
import com.avereon.xenon.ProgramEvent;
import com.avereon.xenon.asset.Asset;
import com.avereon.xenon.asset.AssetException;
import com.avereon.xenon.asset.AssetManager;
import com.avereon.xenon.asset.type.ProgramNoticeType;
import com.avereon.xenon.scheme.FaultScheme;
import com.avereon.xenon.task.TaskException;
import com.avereon.xenon.tool.NoticeTool;
import com.avereon.xenon.workpane.Tool;
import com.avereon.zerra.javafx.Fx;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

import java.lang.System.Logger;
import java.net.URI;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

public class NoticeManager implements Controllable<NoticeManager> {

	private static final Logger log = Log.get();

	private Program program;

	private Asset asset;

	private List<Notice> startupNotices;

	private IntegerProperty unreadCount = new SimpleIntegerProperty();

	public NoticeManager( Program program ) {
		this.program = program;
		this.startupNotices = new CopyOnWriteArrayList<>();
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
			return;
		}

		Fx.run( () -> {
			getNoticeList().addNotice( notice );
			Set<Tool> tools = getProgram().getWorkspaceManager().getActiveWorkpaneTools( NoticeTool.class );
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
		getNoticeList().removeAll();
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
		log.log( Log.TRACE, "Notice manager starting..." );
		try {
			getProgram().register( ProgramEvent.STARTED, e -> startupNotices.forEach( this::addNotice ) );
			asset = getProgram().getAssetManager().createAsset( ProgramNoticeType.URI );
			getProgram().getAssetManager().loadAssets( asset );
		} catch( AssetException exception ) {
			exception.printStackTrace();
		}
		log.log( Log.DEBUG, "Notice manager started." );

		return this;
	}

	@Override
	public NoticeManager stop() {
		log.log( Log.TRACE, "Notice manager stopping..." );
		getProgram().getAssetManager().saveAssets( asset );
		log.log( Log.DEBUG, "Notice manager stopped." );
		return this;
	}

	String getThrowableTitle( Throwable throwable ) {
		if( throwable instanceof TaskException ) throwable = throwable.getCause();
		return throwable.getClass().getSimpleName();
	}

	String getThrowableMessage( Throwable throwable ) {
		if( throwable instanceof TaskException ) throwable = throwable.getCause();
		return throwable.getLocalizedMessage();
	}

	private NoticeModel getNoticeList() {
		return asset.getModel();
	}

	private void updateUnreadCount() {
		unreadCount.setValue( getUnreadNotices().size() );
	}

	private List<Notice> getUnreadNotices() {
		return getNoticeList().getNotices().stream().filter( ( n ) -> !n.isRead() ).collect( Collectors.toList() );
	}

}
