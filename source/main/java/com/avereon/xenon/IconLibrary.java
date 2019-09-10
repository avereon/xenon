package com.avereon.xenon;

import com.avereon.xenon.icon.*;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class IconLibrary {

	private static final int DEFAULT_SIZE = 16;

	private static final String URL_CHECK = ":";

	private Map<String, Class<? extends ProgramImage>> icons;

	public IconLibrary() {
		icons = new ConcurrentHashMap<>();
		register( "program", XRingLargeIcon.class );
		register( "resource-new", DocumentIcon.class );
		register( "resource-open", FolderIcon.class );
		//register( "resource-save", SaveIcon.class );
		register( "resource-save", LightningIcon.class );
		register( "resource-close", DocumentCloseIcon.class );
		register( "exit", PowerIcon.class );

		register( "close", CloseIcon.class );

		register( "undo", UndoIcon.class );
		register( "redo", RedoIcon.class );
		register( "cut", CutIcon.class );
		register( "copy", CopyIcon.class );
		register( "paste", PasteIcon.class );
		register( "delete", DeleteIcon.class );
		register( "indent", IndentIcon.class );
		register( "unindent", UnindentIcon.class );

		register( "setting", SettingIcon.class );
		register( "settings", SettingsIcon.class );

		register( "guide", GuideIcon.class );

		register( "welcome", WelcomeIcon.class );
		register( "help-content", QuestionIcon.class );
		register( "notice", NoticeIcon.class );
		register( "notice-unread", UnreadNoticeIcon.class );
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

		register( "toggle-enabled", ToggleEnabledIcon.class );
		register( "toggle-disabled", ToggleDisabledIcon.class );
	}

	public void register( String id, Class<? extends ProgramImage> icon ) {
		if( id.contains( URL_CHECK ) ) throw new RuntimeException( "Icon id should not contain URL_CHECK string" );
		icons.put( id, icon );
	}

	public Node getIcon( String id ) {
		return getIcon( id, DEFAULT_SIZE );
	}

	public Node getIcon( String id, double size ) {
		//return id.contains( URL_CHECK ) ? getIconFromUrl( id, size ) : getIconRenderer( id ).setSize( size );
		return getIcon( id, null, size );
	}

	public Node getIcon( String id, String backupId ) {
		return getIcon( id, backupId, DEFAULT_SIZE );
	}

	public Node getIcon( String id, String backupId, double size ) {
		Node node = null;

		if( id == null ) id = "";
		if( node == null ) node = getIconRenderer( id );
		if( node == null && id.contains( URL_CHECK ) ) node = getIconFromUrl( id, size );
		if( node == null ) node = getIconRenderer( backupId );
		if( node == null ) node = new BrokenIcon().setSize( size );
		if( node instanceof ProgramImage ) ((ProgramImage)node).setSize( size );

		return node;
	}

	public Image[] getStageIcons( String id ) {
		return getStageIcons( id, 16, 24, 32, 48, 64, 128, 256 );
	}

	private Image[] getStageIcons( String id, int... sizes ) {
		Image[] images = new Image[ sizes.length ];
		for( int index = 0; index < sizes.length; index++ ) {
			ProgramImage image = getIconRenderer( id );
			if( image == null ) image = new BrokenIcon();
			images[ index ] = image.setSize( sizes[ index ] ).getImage();
		}
		return images;
	}

	private ProgramImage getIconRenderer( String id ) {
		try {
			return icons.get( id ).getConstructor().newInstance();
		} catch( Exception exception ) {
			return null;
		}
	}

	private Node getIconFromUrl( String url, double size ) {
		Image image = null;

		try {
			image = new Image( new URL( url ).toExternalForm(), size, size, true, true );
		} catch( MalformedURLException exception ) {
			exception.printStackTrace();
		}
		if( image == null || image.isError() ) return null;

		return new ImageView( image );
	}

}
