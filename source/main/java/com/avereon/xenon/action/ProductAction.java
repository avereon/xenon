package com.avereon.xenon.action;

import com.avereon.xenon.Action;
import com.avereon.xenon.Program;
import com.avereon.xenon.asset.type.ProgramProductType;
import javafx.event.ActionEvent;

public class ProductAction extends Action {

	public ProductAction( Program program ) {
		super( program );
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public void handle( ActionEvent event ) {
		getProgram().getAssetManager().openAsset( ProgramProductType.URI );
	}

}
