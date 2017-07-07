package com.parallelsymmetry.essence;

import com.parallelsymmetry.essence.resource.Resource;

public class ProgramTool extends ProductTool {

	public ProgramTool( Program program, Resource resource ) {
		super( program, resource );
	}

	public Program getProgram() {
		return (Program)getProduct();
	}

}
