package com.avereon.xenon;

import com.avereon.xenon.resource.Codec;
import com.avereon.xenon.resource.OpenResourceRequest;
import com.avereon.xenon.resource.Resource;
import com.avereon.xenon.tool.ProgramTool;
import com.avereon.xenon.workarea.Workpane;
import com.avereon.xenon.workarea.WorkpaneView;

public class OpenToolRequest {

	private OpenResourceRequest openResourceRequest;

	private Resource resource;

	private Workpane pane;

	private Class<? extends ProgramTool> toolClass;

	public OpenToolRequest( OpenResourceRequest openResourceRequest ) {
		this.openResourceRequest = openResourceRequest;
	}

	//public URI getUri() {return openResourceRequest.getUri();}

	public String getQuery() {return openResourceRequest.getQuery();}

	public String getFragment() {return openResourceRequest.getFragment();}

	public Codec getCodec() {return openResourceRequest.getCodec();}

	public WorkpaneView getView() {return openResourceRequest.getView();}

	public boolean isOpenTool() {return openResourceRequest.isOpenTool();}

	public boolean isSetActive() {return openResourceRequest.isSetActive();}

	public Class<? extends ProgramTool> getToolClass() {
		return toolClass;
	}

	public OpenToolRequest setToolClass( Class<? extends ProgramTool> toolClass ) {
		this.toolClass = toolClass;
		return this;
	}

	public Resource getResource() {
		return resource;
	}

	public OpenToolRequest setResource( Resource resource ) {
		this.resource = resource;
		return this;
	}

	public Workpane getPane() {
		return pane;
	}

	public OpenToolRequest setPane( Workpane pane ) {
		this.pane = pane;
		return this;
	}

}
