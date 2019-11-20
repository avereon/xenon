package com.avereon.xenon;

import java.util.ArrayList;
import java.util.List;

public interface ProgramTest {

	static String[] getParameterValues() {
		List<String> values = new ArrayList<>();
		values.add( ProgramFlag.PROFILE );
		values.add( Profile.TEST );
		values.add( ProgramFlag.LOG_LEVEL );
		values.add( "none" );
		return values.toArray( new String[ 0 ] );
	}

}
