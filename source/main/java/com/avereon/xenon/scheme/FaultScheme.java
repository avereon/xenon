package com.avereon.xenon.scheme;

import com.avereon.xenon.Xenon;
import com.avereon.xenon.resource.Resource;
import com.avereon.xenon.resource.exception.ResourceException;

public class FaultScheme extends ProgramScheme {

	public static final String ID = "fault";

	public FaultScheme( Xenon program ) {
		super( program, ID );
	}

	@Override
	public boolean exists( Resource resource ) {
		return true;
	}

	@Override
	public boolean canLoad( Resource resource ) throws ResourceException {
		return true;
	}

}
