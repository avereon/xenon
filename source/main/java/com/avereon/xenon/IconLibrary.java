package com.avereon.xenon;

import com.avereon.rossa.icon.flat.*;
import com.avereon.util.Log;
import com.avereon.util.TextUtil;
import com.avereon.venza.icon.BrokenIcon;
import com.avereon.venza.icon.ImageIcon;
import com.avereon.venza.icon.RenderedIcon;
import com.avereon.venza.image.Images;
import com.avereon.xenon.task.Task;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

import java.lang.System.Logger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class IconLibrary {

	private static final Logger log = Log.get();

	private static final int DEFAULT_SIZE = 16;

	private final Program program;

	private final Map<String, RenderedIcon> icons;

	@Deprecated
	//private final Map<String, IconConfig> oldIcons;

	public IconLibrary( Program program ) {
		this.program = program;
		icons = new ConcurrentHashMap<>();
		//oldIcons = new ConcurrentHashMap<>();

		register( "provider", new WingDiscLargeIcon() );
		register( "program", new XRingLargeIcon() );
		register( "close", new CloseIcon() );
		register( "exit", new PowerIcon() );

		register( "document", new DocumentIcon() );
		register( "asset", new DocumentIcon() );
		register( "asset-new", new DocumentIcon() );
		register( "asset-open", new FolderIcon() );
		register( "asset-save", new SaveIcon() );
		register( "asset-close", new CloseToolIcon() );
		register( "properties", new PropertiesIcon() );

		register( "undo", new UndoIcon() );
		register( "redo", new RedoIcon() );
		register( "cut", new CutIcon() );
		register( "copy", new CopyIcon() );
		register( "paste", new PasteIcon() );
		register( "delete", new DeleteIcon() );
		register( "indent", new IndentIcon() );
		register( "unindent", new UnindentIcon() );
		register( "play", new PlayIcon() );
		register( "pause", new PauseIcon() );

		register( "setting", new SettingIcon() );
		register( "settings", new SettingsIcon() );
		register( "themes", new ThemeIcon() );
		register( "options", new PreferencesIcon() );

		register( "guide", new GuideIcon() );
		register( "fault", new FaultIcon() );
		register( "terminal", new TerminalIcon() );

		register( "welcome", new WelcomeIcon() );
		register( "help-content", new QuestionIcon() );
		register( "notice", new NoticeIcon() );
		register( "notice-error", new NoticeIcon( Color.RED ) );
		register( "notice-warn", new NoticeIcon( Color.YELLOW ) );
		register( "notice-info", new NoticeIcon( Color.GREEN.brighter() ) );
		register( "notice-norm", new NoticeIcon( Color.web( "#40a0c0" ) ) );
		register( "notice-none", new NoticeIcon() );
		register( "task", new TaskQueueIcon() );
		register( "product", new ProductIcon() );
		register( "update", new DownloadIcon() );
		register( "about", new ExclamationIcon() );

		register( "workspace", new FrameIcon() );
		register( "workspace-new", new FrameIcon() );
		register( "workspace-close", new FrameIcon() );

		register( "workarea", new WorkareaIcon() );
		register( "workarea-new", new WorkareaIcon() );
		register( "workarea-rename", new WorkareaRenameIcon() );
		register( "workarea-close", new CloseToolIcon() );

		register( "wallpaper", new WorkareaIcon() );

		register( "file", new DocumentIcon() );
		register( "folder", new FolderIcon() );
		register( "asset-home", new HomeIcon() );
		register( "asset-root", new FileSystemIcon() );

		register( "add", new PlusIcon() );
		register( "refresh", new RefreshIcon() );
		register( "download", new DownloadIcon() );
		register( "market", new MarketIcon() );
		register( "module", new ModuleIcon() );
		register( "enable", new LightningIcon() );
		register( "disable", new DisableIcon() );
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
		icon.getProperties().put( "stylesheet", Program.STYLESHEET );
		icons.put( id, icon );
	}

	public void unregister( String id, RenderedIcon icon ) {
		if( icons.get( id ).getClass() == icon.getClass() ) icons.remove( id );
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
			icon = getIconFromUrl( id, size );
			if( icon != null ) break;
		}
		if( icon == null ) {
			return new BrokenIcon().resize( size );
		} else {
			return icon.copy().resize( size );
		}

//		ProgramIcon oldIcon = null;
//		for( String id : ids ) {
//			oldIcon = getIconRenderer( id, size );
//			if( oldIcon != null ) break;
//		}
//
//		if( oldIcon == null ) {
//			return new BrokenIcon().resize( size );
//		} else {
//			oldIcon.setSize( size );
//		}
//
//		return oldIcon;
	}

	public Node getIcon( List<String> ids, String backupId, double size ) {
		List<String> combined = ids == null ? new ArrayList<>() : new ArrayList<>( ids );
		combined.add( backupId );
		return getIcon( combined, size );
	}

	public Image[] getStageIcons( String id ) {
		return Images.getStageIcons( (RenderedIcon)getIcon( id ), 16, 24, 32, 48, 64, 128, 256 );
	}

//	private ProgramIcon getIconRenderer( String id, double size ) {
//		ProgramIcon icon = getIconRenderer( id );
//		//if( icon == null ) icon = getIconFromUrl( id, size );
//		return icon;
//	}

//	private ProgramIcon getIconRenderer( String id ) {
//		if( id == null || !oldIcons.containsKey( id ) ) return null;
//
//		try {
//			return oldIcons.get( id ).newInstance();
//		} catch( Exception exception ) {
//			log.log( Log.ERROR, "Unable to create icon: " + id, exception );
//			return null;
//		}
//	}

	private RenderedIcon getIconFromUrl( String url, double size ) {
		if( TextUtil.isEmpty( url ) || !url.contains( "://" ) ) return null;

		ImageIcon icon = new ImageIcon( url );
		icon.resize( size );
		program.getTaskManager().submit( Task.of( "Load icon: " + url, icon.getPreloadRunner() ) );

		return null;
	}

//	private static class IconConfig {
//
//		private Class<? extends ProgramImage> icon;
//
//		private Class<?>[] parameterTypes;
//
//		private Object[] parameters;
//
//		IconConfig( Class<? extends ProgramIcon> icon, Object... parameters ) {
//			this.icon = icon;
//			this.parameters = parameters;
//			this.parameterTypes = Arrays.stream( parameters ).map( Object::getClass ).toArray( Class[]::new );
//		}
//
//		ProgramIcon newInstance() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
//			return (ProgramIcon)icon.getConstructor( parameterTypes ).newInstance( parameters );
//		}
//
//	}

}
