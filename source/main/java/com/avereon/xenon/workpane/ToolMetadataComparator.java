package com.avereon.xenon.workpane;

import com.avereon.xenon.tool.ToolRegistration;

import java.util.Comparator;

public class ToolMetadataComparator implements Comparator<ToolRegistration> {

	@Override
	public int compare( ToolRegistration metadata1, ToolRegistration metadata2 ) {
		return metadata1.getName().compareToIgnoreCase( metadata2.getName() );
	}

}
