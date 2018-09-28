package com.xeomar.xenon.action;

import com.xeomar.xenon.Action;
import com.xeomar.xenon.Program;
import com.xeomar.xenon.resource.ResourceException;
import com.xeomar.xenon.resource.type.ProgramProductType;
import javafx.event.Event;

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
	public void handle( Event event ) {
		try {
			URI uri = URI.create( ProgramProductType.URI + "#" + ProgramProductType.INSTALLED );
			getProgram().getResourceManager().open( uri );
		} catch( ResourceException exception ) {
			log.error( "Error opening product tool", exception );
		}
	}

}
