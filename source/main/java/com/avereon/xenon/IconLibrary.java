package com.avereon.xenon;

import com.avereon.util.LogUtil;
import com.avereon.util.TextUtil;
import com.avereon.venza.image.ProgramIcon;
import com.avereon.venza.image.ProgramImage;
import com.avereon.venza.image.ProgramImageIcon;
import com.avereon.rossa.icon.BrokenIcon;
import com.avereon.xenon.task.Task;
import javafx.scene.image.Image;
import org.slf4j.Logger;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
		Image[] images = new Image[ sizes.length ];
		for( int index = 0; index < sizes.length; index++ ) {
			ProgramImage image = getIconRenderer( id );
			if( image == null ) image = new BrokenIcon();
			images[ index ] = image.setSize( sizes[ index ] ).getImage();
		}
		return images;
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
			log.error( "Unable to create icon: " + id, exception );
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
