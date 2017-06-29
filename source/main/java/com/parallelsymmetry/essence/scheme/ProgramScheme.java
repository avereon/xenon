package com.parallelsymmetry.essence.scheme;

import com.parallelsymmetry.essence.Program;

public class ProgramScheme extends BaseScheme {

	public ProgramScheme( Program program ) {
		super( program );
	}

	@Override
	public String getName() {
		return "program";
	}

}
