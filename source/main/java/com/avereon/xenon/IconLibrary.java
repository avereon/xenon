package com.avereon.xenon;

import com.avereon.product.Rb;
import com.avereon.util.TextUtil;
import com.avereon.xenon.task.Task;
import com.avereon.zarra.image.BrokenIcon;
import com.avereon.zarra.image.ImageIcon;
import com.avereon.zarra.image.Images;
import com.avereon.zarra.image.VectorImage;
import com.avereon.zenna.icon.*;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import lombok.CustomLog;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@CustomLog
public class IconLibrary {

	private static final int DEFAULT_SIZE = 16;

	private final Xenon program;

	private final Map<String, VectorImage> icons;

	public IconLibrary( Xenon program ) {
		this.program = program;
		icons = new ConcurrentHashMap<>();

		register( "provider", new WingDiscLargeIcon() );
		register( "program", new XRingLargeIcon() );
		register( "menu", new Hamburger4Icon() );
		register( "context", new ContextIcon() );
		register( "settings", new GearIcon() );
		register( "maintenance", new WrenchIcon() );
		register( "restart", new RefreshIcon() );
		register( "close", new CloseIcon() );
		register( "exit", new PowerIcon() );

		register( "document", new DocumentIcon() );
		register( "asset", new DocumentIcon() );
		register( "asset-new", new DocumentIcon() );
		register( "asset-open", new FolderIcon() );
		register( "asset-save", new SaveIcon() );
		register( "asset-save-all", new SaveIcon() );
		register( "asset-rename", new SaveIcon() );
		register( "asset-close", new CloseToolIcon() );
		register( "properties", new PropertiesIcon() );
		register( "print", new PrinterIcon() );

		register( "edit", new PencilIcon() );
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

		register( "tool", new WrenchIcon() );

		register( "view", new EyeIcon() );
		register( "setting", new SettingIcon() );
		register( "themes", new ThemeIcon() );
		register( "options", new PreferencesIcon() );

		register( "guide", new GuideIcon() );
		register( "fault", new FaultIcon() );
		register( "terminal", new TerminalIcon() );

		register( "about", new ExclamationIcon() );
		register( "help", new QuestionIcon() );
		register( "help-content", new QuestionIcon() );
		register( "search", new MagnifierIcon() );
		register( "notice", new BellIcon() );
		register( "notice-error", new BellIcon( Color.RED ) );
		register( "notice-warn", new BellIcon( Color.YELLOW ) );
		register( "notice-info", new BellIcon( Color.GREEN.brighter() ) );
		register( "notice-norm", new BellIcon( Color.web( "#40a0c0" ) ) );
		register( "notice-none", new BellIcon() );
		register( "notice-unread", new BellIcon( true ) );
		register( "notice-unread-error", new BellIcon( Color.RED, true ) );
		register( "notice-unread-warn", new BellIcon( Color.YELLOW, true ) );
		register( "notice-unread-info", new BellIcon( Color.GREEN.brighter(), true ) );
		register( "notice-unread-norm", new BellIcon( Color.web( "#40a0c0" ), true ) );
		register( "notice-unread-none", new BellIcon( true ) );
		register( "product", new ProductIcon() );
		register( "task", new TaskQueueIcon() );
		register( "update", new DownloadIcon() );
		register( "welcome", new WelcomeIcon() );

		register( "workspace", new FrameIcon() );
		register( "workspace-new", new FrameIcon() );
		register( "minimize", new MinimizeIcon() );
		register( "maximize", new MaximizeIcon() );
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
		register( "reload", new RefreshIcon() );
		register( "download", new DownloadIcon() );
		register( "market", new MarketIcon() );
		register( "module", new ModuleIcon() );
		register( "enable", new LightningIcon() );
		register( "disable", new DisableIcon() );
		register( "remove", new CloseIcon() );
		register( "tag", new TagIcon() );
		register( "title", new TitleIcon() );

		register( "up", new ArrowUpIcon() );
		register( "down", new ArrowDownIcon() );
		register( "left", new ArrowLeftIcon() );
		register( "right", new ArrowRightIcon() );
		register( "prior", new ArrowLeftIcon() );
		register( "next", new ArrowRightIcon() );

		register( "toggle-enabled", new ToggleIcon( true ) );
		register( "toggle-disabled", new ToggleIcon( false ) );
	}

	public final Xenon getProgram() {
		return program;
	}

	public void register( String id, VectorImage icon ) {
		icon.getProperties().put( "stylesheet", Xenon.STYLESHEET );
		icons.put( id, icon );
	}

	public void unregister( String id, VectorImage icon ) {
		if( icon == null ) return;
		var registered = icons.get( id );
		if( registered == null ) return;
		if( registered.getClass() == icon.getClass() ) icons.remove( id );
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
		VectorImage icon = null;
		for( String id : ids ) {
			icon = icons.get( id );
			if( icon == null ) icon = getIconFromUrl( id, size );
			if( icon != null ) break;
		}
		if( icon != null ) icon = icon.copy();
		if( icon == null ) icon = new BrokenIcon();

		return icon.resize( size );
	}

	public Node getIcon( List<String> ids, String backupId, double size ) {
		List<String> combined = ids == null ? new ArrayList<>() : new ArrayList<>( ids );
		combined.add( backupId );
		return getIcon( combined, size );
	}

	public Image[] getStageIcons( String id ) {
		return Images.getStageIcons( (VectorImage)getIcon( id ), 16, 24, 32, 48, 64, 128, 256 );
	}

	private VectorImage getIconFromUrl( String url, double size ) {
		if( TextUtil.isEmpty( url ) || !url.contains( "://" ) ) return null;
		ImageIcon icon = new ImageIcon( url ).resize( size );
		String taskName = Rb.text( RbKey.PROMPT, "load-icon", url );
		program.getTaskManager().submit( Task.of( taskName, icon.getPreloadRunner() ) );
		return icon;
	}

}
