package com.avereon.xenon.action;

import com.avereon.xenon.Program;
import javafx.event.ActionEvent;

import java.nio.file.Path;
import java.util.List;

public class WallpaperPriorAction extends WallpaperFileAction {

	public WallpaperPriorAction( Program program ) {
		super( program );
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public void handle( ActionEvent event ) {
		List<Path> files = listImageFiles();
		int index = getImageIndex();
		index--;
		if( index < 0 ) index = files.size() - 1;
		setImageIndex( index );
	}

}
