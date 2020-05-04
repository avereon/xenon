package com.avereon.xenon.action;

import com.avereon.settings.Settings;
import com.avereon.util.Log;
import com.avereon.xenon.Action;
import com.avereon.xenon.Program;

import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

public abstract class WallpaperFileAction extends Action {

	protected WallpaperFileAction( Program program ) {
		super( program );
	}

	protected int getImageIndex() {
		Settings settings = getProgram().getProgramSettings();
		String imageIndexString = settings.get( "workspace-scenery-image-index", "0" );

		int index;
		try {
			index = Integer.parseInt( imageIndexString );
		} catch( NumberFormatException exception ) {
			index = -1;
		}

		return index;
	}

	protected void setImageIndex( int index ) {
		getProgram().getProgramSettings().set( "workspace-scenery-image-index", String.valueOf( index ) );
	}

	protected List<Path> listImageFiles() {
		try {
			String imagePath = getProgram().getProgramSettings().get( "workspace-scenery-image-path", "" );
			return listImageFiles( Paths.get( imagePath ) );
		} catch( IOException exception ) {
			return List.of();
		}
	}

	public static List<Path> listImageFiles( Path folder ) throws IOException {
		List<Path> result = new ArrayList<>();
		try( DirectoryStream<Path> stream = Files.newDirectoryStream( folder, "*.{gif,GIF,jpg,JPG,jpeg,JPEG,png,PNG}" ) ) {
			for( Path entry : stream ) {
				result.add( entry );
			}
		} catch( DirectoryIteratorException exception ) {
			log.log( Log.ERROR, "Error listing image files", exception );
		}
		return result;
	}

}
