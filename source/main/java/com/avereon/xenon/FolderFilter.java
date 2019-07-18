package com.avereon.xenon;

import java.io.File;
import java.io.FileFilter;

/**
 * @author Mark Soderquist
 */
public class FolderFilter implements FileFilter {

	@Override
	public boolean accept( File file ) {
		return file.isDirectory();
	}

}
