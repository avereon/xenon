package com.xeomar.xenon.tool.task;

import com.xeomar.xenon.Program;
import com.xeomar.xenon.ProgramProduct;
import com.xeomar.xenon.resource.Resource;
import com.xeomar.xenon.tool.ProgramTool;

public class TaskTool extends ProgramTool {

	public TaskTool( ProgramProduct product, Resource resource ) {
		super( product, resource );
		setId( "tool-task" );
		setGraphic( ((Program)product).getIconLibrary().getIcon( "task" ) );
		setTitle( product.getResourceBundle().getString( "tool", "task-name" ) );


	}

}
