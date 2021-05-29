package com.avereon.xenon;

import com.avereon.settings.Settings;

import java.util.Comparator;

class ToolOrderComparator implements Comparator<ProgramTool> {

	@Override
	public int compare( ProgramTool tool1, ProgramTool tool2 ) {
		Settings settings1 = tool1.getSettings();
		Settings settings2 = tool2.getSettings();

		if( settings1 == null && settings2 == null ) return 0;
		if( settings1 == null ) return -1;
		if( settings2 == null ) return 1;

		Integer order1 = settings1.get( "order", Integer.class );
		Integer order2 = settings2.get( "order", Integer.class );

		if( order1 == null && order2 == null ) return 0;
		if( order1 == null ) return -1;
		if( order2 == null ) return 1;

		return order2 - order1;
	}
}
