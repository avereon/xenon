package com.avereon.xenon.tool;

import com.avereon.util.Log;
import com.avereon.xenon.ProgramProduct;
import com.avereon.xenon.asset.Asset;
import com.avereon.xenon.tool.guide.Guide;
import com.avereon.xenon.tool.guide.GuideNode;
import com.avereon.xenon.tool.guide.GuidedTool;

import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

//import org.apache.commons.vfs2.FileSystemException;
//import org.apache.commons.vfs2.FileSystemManager;
//import org.apache.commons.vfs2.VFS;

public class AssetTool extends GuidedTool {

	private static final System.Logger log = Log.get();

	private final Guide guide;

	// NOTE Temporary
//	private FileSystemManager fsManager;

	public AssetTool( ProgramProduct product, Asset asset ) {
		super( product, asset );
		guide = initializeGuide();

//		try {
//			fsManager = VFS.getManager();
//		} catch( FileSystemException e ) {
//			log.log( Log.ERROR, e );
//		}
	}

	@Override
	protected Guide getGuide() {
		return guide;
	}

	private Guide initializeGuide() {
		Guide guide = new Guide();

		// NOTE Existing assets should all have a unique URI, that can be the id
		testing( guide );

		return guide;
	}

	private Guide testing( Guide guide ) {
		for( Path fsRoot : FileSystems.getDefault().getRootDirectories() ) {
			Path filenamePath = fsRoot.getFileName();
			String filename = filenamePath == null ? "" : filenamePath.toString();

			String name = filename;
			String icon = "file";
			if( Files.isDirectory( fsRoot ) ) {
				name = "/" + filename;
				icon = "folder";
			}

			guide.addNode( new GuideNode( getProgram() , fsRoot.toUri().toString(), name, icon ) );
		}

		return guide;
	}

}
