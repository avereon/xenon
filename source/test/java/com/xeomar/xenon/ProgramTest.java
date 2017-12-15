package com.xeomar.xenon;

import java.util.ArrayList;
import java.util.List;

public interface ProgramTest {

	static String[] getParameterValues() {
		List<String> values = new ArrayList<>();
		values.add( "--" + ProgramFlag.EXECMODE );
		values.add( ProgramFlag.EXECMODE_TEST );
		values.add( "--" + ProgramFlag.LOG_LEVEL );
		values.add( "none" );
		return values.toArray( new String[values.size()] );
	}

}
