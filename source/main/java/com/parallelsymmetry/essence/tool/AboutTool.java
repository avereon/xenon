package com.parallelsymmetry.essence.tool;

import com.parallelsymmetry.essence.Resource;
import com.parallelsymmetry.essence.work.Tool;

import java.net.URI;

public class AboutTool extends Tool {

	private static Resource resource = new Resource( URI.create( "program:about" ) );

	public AboutTool() {
		super( resource );
	}

}
