package com.xeomar.xenon;

import com.xeomar.xenon.icon.*;
import javafx.scene.image.Image;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class IconLibrary {

	private static final int DEFAULT_SIZE = 16;

	private Map<String, Class<? extends ProgramImage>> icons;

	public IconLibrary() {
		icons = new ConcurrentHashMap<>();
		register( "program", XRingLargeIcon.class );
		register( "new", DocumentIcon.class );
		register( "open", FolderIcon.class );
		//register( "save", SaveIcon.class );
		register( "save", LightningIcon.class );
		register( "close", CloseIcon.class );
		register( "exit", ExitIcon.class );

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
		register( "notice", ExclamationIcon.class );
		register( "notice-unread", UnreadNoticeIcon.class );
		register( "task", TaskQueueIcon.class );
		register( "product", ProductIcon.class );
		register( "update", DownloadIcon.class );
		register( "about", ExclamationIcon.class );

		register( "workarea-new", WorkareaIcon.class );
		register( "workarea-rename", WorkareaRenameIcon.class );
		register( "workarea-close", WorkareaCloseIcon.class );

		register( "refresh", RefreshIcon.class );
		register( "download", DownloadIcon.class );
		register( "market", MarketIcon.class );
		register( "module", ModuleIcon.class );
		register( "enable", LightningIcon.class );
		register( "disable", DisableIcon.class );
		register( "remove", ExitIcon.class );
	}

	public ProgramImage getIcon( String id ) {
		return getIcon( id, DEFAULT_SIZE );
	}

	public ProgramImage getIcon( String id, double size ) {
		return getIconRenderer( id ).setSize( size );
	}

	public Image[] getStageIcons( String id ) {
		return getStageIcons( id, 16, 24, 32, 48, 64, 128, 256 );
	}

	private Image[] getStageIcons( String id, int... sizes ) {
		Image[] images = new Image[ sizes.length ];
		for( int index = 0; index < sizes.length; index++ ) {
			images[ index ] = getIcon( id ).setSize( sizes[ index ] ).getImage();
		}
		return images;
	}

	public void register( String id, Class<? extends ProgramImage> icon ) {
		icons.put( id, icon );
	}

	private ProgramImage getIconRenderer( String id ) {
		Class<? extends ProgramImage> renderer = icons.get( id );

		ProgramImage icon;
		try {
			icon = renderer.getConstructor().newInstance();
		} catch( Exception exception ) {
			icon = new BrokenIcon();
		}

		return icon.setSize( DEFAULT_SIZE );
	}

}
