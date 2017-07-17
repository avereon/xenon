package com.parallelsymmetry.essence.scheme;

import com.parallelsymmetry.essence.Program;
import com.parallelsymmetry.essence.resource.Resource;
import com.parallelsymmetry.essence.resource.ResourceException;

public class ProgramScheme extends BaseScheme {

	public ProgramScheme( Program program ) {
		super( program );
	}

	@Override
	public String getName() {
		return "program";
	}

	@Override
	public boolean exists( Resource resource ) throws ResourceException {
		// Program resources always exist
		return true;
	}

}
