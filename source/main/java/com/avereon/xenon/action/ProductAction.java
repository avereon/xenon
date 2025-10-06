package com.avereon.xenon.action;

import com.avereon.xenon.ProgramAction;
import com.avereon.xenon.Xenon;
import com.avereon.xenon.asset.type.ProgramModuleType;
import javafx.event.ActionEvent;

public class ProductAction extends ProgramAction {

	public ProductAction( Xenon program ) {
		super( program );
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public void handle( ActionEvent event ) {
		getProgram().getResourceManager().openAsset( ProgramModuleType.URI );
	}

}
