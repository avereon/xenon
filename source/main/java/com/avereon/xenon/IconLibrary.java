package com.avereon.xenon;

import com.avereon.rossa.icon.*;
import com.avereon.util.Log;
import com.avereon.util.TextUtil;
import com.avereon.venza.icon.BrokenIcon;
import com.avereon.venza.image.Images;
import com.avereon.venza.image.ProgramIcon;
import com.avereon.venza.image.ProgramImage;
import com.avereon.venza.image.ProgramImageIcon;
import com.avereon.xenon.task.Task;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

import java.lang.System.Logger;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class IconLibrary {

	private static final Logger log = Log.get();

	private static final int DEFAULT_SIZE = 16;

	private Program program;

	private Map<String, IconConfig> icons;

	public IconLibrary( Program program ) {
		this.program = program;
		icons = new ConcurrentHashMap<>();

		register( "provider", WingDiscLargeIcon.class );
		register( "program", XRingLargeIcon.class );
		register( "close", CloseIcon.class );
		register( "exit", PowerIcon.class );

		register( "asset", DocumentIcon.class );
		register( "asset-new", DocumentIcon.class );
		register( "asset-open", FolderIcon.class );
		//register( "asset-save", SaveIcon.class );
		register( "asset-save", LightningIcon.class );
		register( "asset-close", DocumentCloseIcon.class );
		register( "properties", SettingsIcon.class );

		register( "undo", UndoIcon.class );
		register( "redo", RedoIcon.class );
		register( "cut", CutIcon.class );
		register( "copy", CopyIcon.class );
		register( "paste", PasteIcon.class );
		register( "delete", DeleteIcon.class );
		register( "indent", IndentIcon.class );
		register( "unindent", UnindentIcon.class );
		register( "play", PlayIcon.class );
		register( "pause", PauseIcon.class );

		register( "setting", SettingIcon.class );
		register( "settings", SettingsIcon.class );

		register( "guide", GuideIcon.class );
		register( "fault", FaultIcon.class );
		register( "terminal", TerminalIcon.class );

		register( "welcome", WelcomeIcon.class );
		register( "help-content", QuestionIcon.class );
		register( "notice", NoticeIcon.class );
		register( "notice-error", NoticeIcon.class, Color.RED );
		register( "notice-warn", NoticeIcon.class, Color.YELLOW );
		register( "notice-info", NoticeIcon.class, Color.GREEN.brighter() );
		register( "notice-norm", NoticeIcon.class, Color.web( "#40a0c0" ) );
		register( "notice-none", NoticeIcon.class );
		register( "task", TaskQueueIcon.class );
		register( "product", ProductIcon.class );
		register( "update", DownloadIcon.class );
		register( "about", ExclamationIcon.class );

		register( "workspace", FrameIcon.class );
		register( "workspace-new", FrameIcon.class );
		register( "workspace-close", FrameIcon.class );

		register( "workarea", WorkareaIcon.class );
		register( "workarea-new", WorkareaIcon.class );
		register( "workarea-rename", WorkareaRenameIcon.class );
		register( "workarea-close", CloseToolIcon.class );

		register( "add", PlusIcon.class );
		register( "refresh", RefreshIcon.class );
		register( "download", DownloadIcon.class );
		register( "market", MarketIcon.class );
		register( "module", ModuleIcon.class );
		register( "enable", LightningIcon.class );
		register( "disable", DisableIcon.class );
		register( "remove", CloseIcon.class );

		register( "toggle-enabled", ToggleIcon.class, true );
		register( "toggle-disabled", ToggleIcon.class, false );
	}

	public void register( String id, Class<? extends ProgramIcon> icon, Object... parameters ) {
		icons.put( id, new IconConfig( icon, parameters ) );
	}

	public void unregister( String id, Class<? extends ProgramIcon> icon ) {
		if( icon.isInstance( icons.get( id ) ) ) icons.remove( id );
	}

	public ProgramIcon getIcon( String id ) {
		return getIcon( List.of( id == null ? "" : id ), DEFAULT_SIZE );
	}

	public ProgramIcon getIcon( String id, double size ) {
		return getIcon( List.of( id == null ? "" : id ), size );
	}

	public ProgramIcon getIcon( String id, String backupId ) {
		return getIcon( List.of( id == null ? "" : id, backupId == null ? "" : backupId ), DEFAULT_SIZE );
	}

	public ProgramIcon getIcon( String id, String backupId, double size ) {
		return getIcon( List.of( id == null ? "" : id, backupId == null ? "" : backupId ), size );
	}

	public ProgramIcon getIcon( List<String> ids ) {
		return getIcon( ids, DEFAULT_SIZE );
	}

	public ProgramIcon getIcon( List<String> ids, String backupId ) {
		return getIcon( ids, backupId, DEFAULT_SIZE );
	}

	public ProgramIcon getIcon( List<String> ids, double size ) {
		ProgramIcon icon = null;
		for( String id : ids ) {
			icon = getIconRenderer( id, size );
			if( icon != null ) break;
		}
		if( icon == null ) icon = new BrokenIcon();
		icon.setSize( size );
		return icon;
	}

	public ProgramIcon getIcon( List<String> ids, String backupId, double size ) {
		List<String> combined = ids == null ? new ArrayList<>() : new ArrayList<>( ids );
		combined.add( backupId );
		return getIcon( combined, size );
	}

	public Image[] getStageIcons( String id ) {
		return getStageIcons( id, 16, 24, 32, 48, 64, 128, 256 );
	}

	private Image[] getStageIcons( String id, int... sizes ) {
		return Images.getStageIcons( getIconRenderer( id ), sizes );
	}

	private ProgramIcon getIconRenderer( String id, double size ) {
		ProgramIcon icon = getIconRenderer( id );
		if( icon == null ) icon = getIconFromUrl( id, size );
		return icon;
	}

	private ProgramIcon getIconRenderer( String id ) {
		if( id == null || !icons.containsKey( id ) ) return null;

		try {
			return icons.get( id ).newInstance();
		} catch( Exception exception ) {
			log.log( Log.ERROR,  "Unable to create icon: " + id, exception );
			return null;
		}
	}

	private ProgramIcon getIconFromUrl( String url, double size ) {
		if( TextUtil.isEmpty( url ) || !url.contains( "://" ) ) return null;

		ProgramImageIcon icon = new ProgramImageIcon( url );
		icon.setSize( size );
		program.getTaskManager().submit( Task.of( "Load icon: " + url, icon.getPreloadRunner() ) );

		return icon;
	}

	private static class IconConfig {

		private Class<? extends ProgramImage> icon;

		private Class<?>[] parameterTypes;

		private Object[] parameters;

		IconConfig( Class<? extends ProgramIcon> icon, Object... parameters ) {
			this.icon = icon;
			this.parameters = parameters;
			this.parameterTypes = Arrays.stream( parameters ).map( Object::getClass ).toArray( Class[]::new );
		}

		ProgramIcon newInstance() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
			return (ProgramIcon)icon.getConstructor( parameterTypes ).newInstance( parameters );
		}

	}

}
