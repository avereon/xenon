package com.xeomar.xenon.scheme;

import com.xeomar.xenon.Program;
import com.xeomar.xenon.resource.Resource;

public class ProgramScheme extends BaseScheme {

	public ProgramScheme( Program program ) {
		super( program );
	}

	@Override
	public String getName() {
		return "program";
	}

	@Override
	public boolean exists( Resource resource ) {
		// Program resources always exist
		return true;
	}

}
