package com.avereon.xenon.action;

import com.avereon.xenon.Action;
import com.avereon.xenon.Program;
import com.avereon.xenon.resource.type.ProgramProductType;
import com.avereon.xenon.tool.product.ProductTool;
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
		URI uri = URI.create( ProgramProductType.URI + "#" + ProductTool.INSTALLED );
		getProgram().getResourceManager().open( uri );
	}

}
