package com.avereon.xenon.tool;

import com.avereon.util.Log;
import com.avereon.util.TextUtil;
import com.avereon.xenon.ProgramProduct;
import com.avereon.xenon.asset.Asset;
import com.avereon.xenon.asset.OpenAssetRequest;
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
	protected void open( OpenAssetRequest request ) {
		// Set the title depending on the mode requested
		String action = request.getFragment();
		if( TextUtil.isEmpty( action )) action = "open";
		setTitle( getProduct().rb().text( "action", action + ".name" ));
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
			boolean folder = Files.isDirectory( fsRoot );
			Path filenamePath = fsRoot.getFileName();
			String filename = filenamePath == null ? "" : filenamePath.toString();

			String name = folder ? "/" + filename : filename;
			String icon = folder ? "folder": "file";

			GuideNode node = new GuideNode( getProgram() , fsRoot.toUri().toString(), name, icon );
			guide.addNode( node );

			if( folder ) {
				// Need to add a "dummy" node when not expanded
				// The dummy node will be replaced when the parent is expanded
				guide.addNode( node, new GuideNode( getProgram(), "dummy", "dummy", "dummy" ) );
			}
		}

		return guide;
	}

}
