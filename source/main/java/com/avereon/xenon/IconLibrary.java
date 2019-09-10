package com.avereon.xenon;

import com.avereon.util.LogUtil;
import com.avereon.util.TextUtil;
import com.avereon.xenon.icon.BrokenIcon;
import com.avereon.xenon.task.Task;
import javafx.scene.Node;
import javafx.scene.image.Image;
import org.slf4j.Logger;

import java.lang.invoke.MethodHandles;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class IconLibrary {

	private static final Logger log = LogUtil.get( MethodHandles.lookup().lookupClass() );

	private static final int DEFAULT_SIZE = 16;

	private Program program;

	private Map<String, Class<? extends ProgramImage>> icons;

	public IconLibrary( Program program ) {
		this.program = program;
		icons = new ConcurrentHashMap<>();
	}

	public void register( String id, Class<? extends ProgramImage> icon ) {
		icons.put( id, icon );
	}

	public Node getIcon( String id ) {
		return getIcon( id, DEFAULT_SIZE );
	}

	public Node getIcon( String id, double size ) {
		return getIcon( id, null, size );
	}

	public Node getIcon( String id, String backupId ) {
		return getIcon( id, backupId, DEFAULT_SIZE );
	}

	public Node getIcon( String id, String backupId, double size ) {
		Node node = null;

		if( id == null ) id = "";
		if( node == null ) node = getIconRenderer( id );
		if( node == null ) node = getIconFromUrl( id, size );
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
		if( TextUtil.isEmpty( url ) ) return null;

		ProgramImageIcon icon = new ProgramImageIcon();

		program.getTaskManager().submit( Task.of( "Load icon: " + url, () -> {
			try {
				Image image = new Image( new URL( url ).toExternalForm(), size, size, true, true );
				if( !image.isError() ) icon.setImage( image );
			} catch( MalformedURLException exception ) {
				if( url.contains( "://" ) ) log.info( "Unable to load icon", exception );
			} catch( Exception exception ) {
				log.warn( "Unable to load icon", exception );
			}
		} ) );

		return icon;
	}

}
