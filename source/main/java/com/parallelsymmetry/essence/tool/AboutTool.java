package com.parallelsymmetry.essence.tool;

import com.parallelsymmetry.essence.Resource;
import com.parallelsymmetry.essence.work.WorkTool;

import java.net.URI;

public class AboutTool extends WorkTool {

	private static Resource resource = new Resource( URI.create( "program:about" ) );

	public AboutTool() {
		super( resource );
	}

}
