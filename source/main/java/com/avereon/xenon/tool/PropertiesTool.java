package com.avereon.xenon.tool;

import com.avereon.util.Log;
import com.avereon.xenon.ProgramProduct;
import com.avereon.xenon.ProgramTool;
import com.avereon.xenon.asset.Asset;
import com.avereon.xenon.asset.OpenAssetRequest;
import com.avereon.xenon.workpane.ToolException;

/**
 * This tool listens for "show properties" and "hide properties" events that
 * allow the user to edit the properties of an object using the settings API.
 */
public class PropertiesTool extends ProgramTool {

	private static final System.Logger log = Log.get();

	public PropertiesTool( ProgramProduct product, Asset asset ) {
		super( product, asset );
	}

	@Override
	protected void ready( OpenAssetRequest request ) throws ToolException {
		// TODO Register a listener for "properties events"
	}

}
