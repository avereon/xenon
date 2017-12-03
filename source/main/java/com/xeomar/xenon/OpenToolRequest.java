package com.xeomar.xenon;

import com.xeomar.xenon.resource.Codec;
import com.xeomar.xenon.resource.OpenResourceRequest;
import com.xeomar.xenon.resource.Resource;
import com.xeomar.xenon.tool.AbstractTool;
import com.xeomar.xenon.workarea.Workpane;
import com.xeomar.xenon.workarea.WorkpaneView;

public class OpenToolRequest {

	private OpenResourceRequest openResourceRequest;

	private Workpane pane;

	private Class<? extends AbstractTool> toolClass;

	public OpenToolRequest( OpenResourceRequest openResourceRequest ) {
		this.openResourceRequest = openResourceRequest;
	}

	public Resource getResource() {return openResourceRequest.getResource();}

	public String getQuery() {return openResourceRequest.getQuery();}

	public String getFragment() {return openResourceRequest.getFragment();}

	public Codec getCodec() {return openResourceRequest.getCodec();}

	public WorkpaneView getView() {return openResourceRequest.getView();}

	public boolean isOpenTool() {return openResourceRequest.isOpenTool();}

	public boolean isSetActive() {return openResourceRequest.isSetActive();}

	public Class<? extends AbstractTool> getToolClass() {
		return toolClass;
	}

	public OpenToolRequest setToolClass( Class<? extends AbstractTool> toolClass ) {
		this.toolClass = toolClass;
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
