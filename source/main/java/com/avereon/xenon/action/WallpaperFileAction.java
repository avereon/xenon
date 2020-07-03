package com.avereon.xenon.action;

import com.avereon.settings.Settings;
import com.avereon.xenon.Action;
import com.avereon.xenon.Program;
import com.avereon.xenon.workspace.WorkspaceBackground;

import java.io.IOException;
import java.nio.file.*;
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
			index = 0;
		}

		return index;
	}

	protected void setImageIndex( int index ) {
		getProgram().getProgramSettings().set( "workspace-scenery-image-index", String.valueOf( index ) );
	}

	protected List<Path> listImageFiles() {
		try {
			String imagePath = getProgram().getProgramSettings().get( "workspace-scenery-image-path", "" );
			return WorkspaceBackground.listImageFiles( Paths.get( imagePath ) );
		} catch( IOException exception ) {
			return List.of();
		}
	}

}
