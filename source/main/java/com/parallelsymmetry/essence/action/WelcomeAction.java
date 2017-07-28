package com.parallelsymmetry.essence.action;

import com.parallelsymmetry.essence.Action;
import com.parallelsymmetry.essence.Program;
import com.parallelsymmetry.essence.resource.Resource;
import javafx.event.Event;

import java.net.URI;

public class WelcomeAction extends Action {

	private Resource resource;

	public WelcomeAction( Program program ) {
		super( program );
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public void handle( Event event ) {
		if( resource == null ) {
			try {
				resource = program.getResourceManager().createResource( URI.create( "program:welcome" ) );
			} catch( Exception exception ) {
				log.warn( "Error opening settings resource", exception );
			}
		}

		program.getResourceManager().open( resource );
	}

}
