package com.avereon.xenon;

import com.avereon.rossa.icon.*;
import com.avereon.rossa.icon.flat.CloseIcon;
import com.avereon.rossa.icon.flat.CloseToolIcon;
import com.avereon.rossa.icon.flat.DocumentIcon;
import com.avereon.rossa.icon.flat.NoticeIcon;
import com.avereon.rossa.icon.flat.PowerIcon;
import com.avereon.rossa.icon.flat.ToggleIcon;
import com.avereon.rossa.icon.flat.WelcomeIcon;
import com.avereon.rossa.icon.flat.WingDiscLargeIcon;
import com.avereon.rossa.icon.flat.XRingLargeIcon;
import com.avereon.rossa.icon.flat.*;
import com.avereon.util.Log;
import com.avereon.util.TextUtil;
import com.avereon.venza.icon.BrokenIcon;
import com.avereon.venza.icon.RenderedIcon;
import com.avereon.venza.image.Images;
import com.avereon.venza.image.ProgramIcon;
import com.avereon.venza.image.ProgramImage;
import com.avereon.venza.image.ProgramImageIcon;
import com.avereon.xenon.task.Task;
import javafx.scene.Node;
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

	private final Program program;

	private final Map<String, RenderedIcon> icons;

	@Deprecated
	private final Map<String, IconConfig> oldIcons;

	public IconLibrary( Program program ) {
		this.program = program;
		icons = new ConcurrentHashMap<>();
		oldIcons = new ConcurrentHashMap<>();

		register( "provider", new WingDiscLargeIcon() );
		register( "program", new XRingLargeIcon() );
		register( "close", new CloseIcon() );
		register( "exit", new PowerIcon() );

		register( "document", new DocumentIcon() );
		register( "asset", new DocumentIcon() );
		register( "asset-new", new DocumentIcon() );
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
		register( "themes", ThemeIcon.class );
		register( "options", SettingsIcon.class );

		register( "guide", GuideIcon.class );
		register( "fault", FaultIcon.class );
		register( "terminal", TerminalIcon.class );

		register( "welcome", new WelcomeIcon() );
		register( "help-content", QuestionIcon.class );
		register( "notice", new NoticeIcon() );
		register( "notice-error", new NoticeIcon( Color.RED ) );
		register( "notice-warn", new NoticeIcon( Color.YELLOW ) );
		register( "notice-info", new NoticeIcon( Color.GREEN.brighter() ) );
		register( "notice-norm", new NoticeIcon( Color.web( "#40a0c0" ) ) );
		register( "notice-none", new NoticeIcon() );
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
		register( "workarea-close", new CloseToolIcon() );

		register( "wallpaper", WorkareaIcon.class );

		register( "file", new DocumentIcon() );
		register( "folder", FolderIcon.class );
		register( "asset-home", HomeIcon.class );
		register( "asset-root", FileSystemIcon.class );

		register( "add", PlusIcon.class );
		register( "refresh", RefreshIcon.class );
		register( "download", DownloadIcon.class );
		register( "market", MarketIcon.class );
		register( "module", ModuleIcon.class );
		register( "enable", LightningIcon.class );
		register( "disable", DisableIcon.class );
		register( "remove", new CloseIcon() );

		register( "up", new ArrowUpIcon() );
		register( "down", new ArrowDownIcon() );
		register( "left", new ArrowLeftIcon() );
		register( "right", new ArrowRightIcon() );
		register( "prior", new ArrowLeftIcon() );
		register( "next", new ArrowRightIcon() );

		register( "toggle-enabled", new ToggleIcon( true ) );
		register( "toggle-disabled", new ToggleIcon( false ) );
	}

	public void register( String id, RenderedIcon icon ) {
		icons.put( id, icon );
	}

	public void register( String id, Class<? extends ProgramIcon> icon, Object... parameters ) {
		oldIcons.put( id, new IconConfig( icon, parameters ) );
	}

	public void unregister( String id, Class<? extends ProgramIcon> icon ) {
		if( icon.isInstance( oldIcons.get( id ) ) ) oldIcons.remove( id );
	}

	public Node getIcon( String id ) {
		return getIcon( List.of( id == null ? "" : id ), DEFAULT_SIZE );
	}

	public Node getIcon( String id, double size ) {
		return getIcon( List.of( id == null ? "" : id ), size );
	}

	public Node getIcon( String id, String backupId ) {
		return getIcon( List.of( id == null ? "" : id, backupId == null ? "" : backupId ), DEFAULT_SIZE );
	}

	public Node getIcon( String id, String backupId, double size ) {
		return getIcon( List.of( id == null ? "" : id, backupId == null ? "" : backupId ), size );
	}

	public Node getIcon( List<String> ids ) {
		return getIcon( ids, DEFAULT_SIZE );
	}

	public Node getIcon( List<String> ids, String backupId ) {
		return getIcon( ids, backupId, DEFAULT_SIZE );
	}

	public Node getIcon( List<String> ids, double size ) {
		RenderedIcon icon = null;
		for( String id : ids ) {
			icon = icons.get( id );
			if( icon != null ) break;
		}
		if( icon != null ) {
			icon = icon.copy().resize( size );
			return icon;
		}

		ProgramIcon oldIcon = null;
		for( String id : ids ) {
			oldIcon = getIconRenderer( id, size );
			if( oldIcon != null ) break;
		}

		if( oldIcon == null ) {
			return new BrokenIcon().resize( size );
		} else {
			oldIcon.setSize( size );
		}

		return oldIcon;
	}

	public Node getIcon( List<String> ids, String backupId, double size ) {
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
		if( id == null || !oldIcons.containsKey( id ) ) return null;

		try {
			return oldIcons.get( id ).newInstance();
		} catch( Exception exception ) {
			log.log( Log.ERROR, "Unable to create icon: " + id, exception );
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
