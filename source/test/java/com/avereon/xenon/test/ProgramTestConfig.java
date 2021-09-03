package com.avereon.xenon.test;

import com.avereon.xenon.Profile;
import com.avereon.xenon.ProgramFlag;

import java.util.ArrayList;
import java.util.List;

public interface ProgramTestConfig {

	static String[] getParameterValues() {
		List<String> values = new ArrayList<>();
		values.add( ProgramFlag.PROFILE );
		values.add( Profile.TEST );
		values.add( ProgramFlag.LOG_LEVEL );
		values.add( ProgramFlag.CONFIG);
		return values.toArray( new String[ 0 ] );
	}

}
