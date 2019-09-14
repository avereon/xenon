package com.avereon.xenon;

import com.avereon.util.LogUtil;
import com.avereon.util.TextUtil;
import com.avereon.xenon.icon.BrokenIcon;
import com.avereon.xenon.task.Task;
import javafx.scene.Node;
import javafx.scene.image.Image;
import org.slf4j.Logger;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class IconLibrary {

	private static final Logger log = LogUtil.get( MethodHandles.lookup().lookupClass() );

	private static final int DEFAULT_SIZE = 16;

	private Program program;

	private Map<String, IconConfig> icons;

	public IconLibrary( Program program ) {
		this.program = program;
		icons = new ConcurrentHashMap<>();
	}

	public void register( String id, Class<? extends ProgramImage> icon, Object... parameters ) {
		icons.put( id, new IconConfig( icon, parameters ) );
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
		Node icon = getIconRenderer( id );
		if( icon == null ) icon = getIconFromUrl( id, size );
		if( icon == null ) icon = getIconRenderer( backupId );
		if( icon == null ) icon = new BrokenIcon();
		if( icon instanceof ProgramImage ) ((ProgramImage)icon).setSize( size );
		return icon;
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
		if( id == null || !icons.containsKey( id ) ) return null;

		try {
			return icons.get( id ).newInstance();
		} catch( Exception exception ) {
			log.error( "Unable to create icon: " + id, exception );
			return null;
		}
	}

	private Node getIconFromUrl( String url, double size ) {
		if( TextUtil.isEmpty( url ) ) return null;

		ProgramImageIcon icon = new ProgramImageIcon();

		program.getTaskManager().submit( Task.of( "Load icon: " + url, () -> {
			try {
				Image image = new Image( new URL( url ).toExternalForm(), size, size, true, true );
				if( !image.isError() ) icon.setRenderImage( image );
			} catch( MalformedURLException exception ) {
				if( url.contains( "://" ) ) log.info( "Unable to load icon", exception );
			} catch( Exception exception ) {
				log.warn( "Unable to load icon", exception );
			}
		} ) );

		return icon;
	}

	private static class IconConfig {

		private Class<? extends ProgramImage> icon;

		private Class<?>[] parameterTypes;

		private Object[] parameters;

		IconConfig( Class<? extends ProgramImage> icon, Object... parameters ) {
			this.icon = icon;
			this.parameters = parameters;
			this.parameterTypes = Arrays.stream( parameters ).map( Object::getClass ).toArray( Class[]::new );
		}

		ProgramImage newInstance() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
			return icon.getConstructor( parameterTypes ).newInstance( parameters );
		}

	}

}
