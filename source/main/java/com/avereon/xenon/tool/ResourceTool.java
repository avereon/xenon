package com.avereon.xenon.tool;

import com.avereon.xenon.XenonProgramProduct;
import com.avereon.xenon.resource.Resource;

/**
 * ResourceTool is the new name for AssetTool. For now, it subclasses
 * AssetTool to provide a Resource-oriented API surface while retaining the
 * existing behavior.
 */
public class ResourceTool extends AssetTool {

	public ResourceTool( XenonProgramProduct product, Resource resource ) {
		// Resource extends Asset, so this is safe during transition
		super( product, resource );
	}
}
