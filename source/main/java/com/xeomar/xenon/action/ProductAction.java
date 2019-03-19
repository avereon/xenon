package com.xeomar.xenon.action;

import com.xeomar.xenon.Action;
import com.xeomar.xenon.Program;
import com.xeomar.xenon.resource.type.ProgramProductType;
import javafx.event.ActionEvent;

import java.net.URI;

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
		URI uri = URI.create( ProgramProductType.URI + "#" + ProgramProductType.INSTALLED );
		getProgram().getResourceManager().open( uri );
	}

}
