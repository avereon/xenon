package com.avereon.xenon.resource;

import com.avereon.xenon.asset.AssetType;

/**
 * ResourceType is the new name for the former AssetType.
 * For now this class extends the existing AssetType implementation while the
 * rest of the codebase transitions to the Resource terminology.
 */
public class ResourceType extends AssetType {

	public ResourceType(com.avereon.xenon.XenonProgramProduct product, String rbKey) {
		super(product, rbKey);
	}
}
