package com.avereon.xenon.test;

import com.avereon.product.Profile;
import com.avereon.product.ProgramFlag;

import java.util.ArrayList;
import java.util.List;

public interface ProgramTestConfig {

	/**
	 * The wait timeout for many operations. Common values are:
	 * <pre>
	 * 5000 - GitHub Actions, Mintbox Mini
	 *  | Slower computers
	 *  |
	 *  | Faster computers
	 * 1000 - AMD Threadripper, Intel i9</pre>
	 */
	int TIMEOUT = 2000;

	/**
	 * The wait timeout for many operations. Common values are:
	 * <pre>
	 * 25000 - GitHub Actions, Mintbox Mini
	 *  | Slower computers
	 *  |
	 *  | Faster computers
	 * 5000 - AMD Threadripper, Intel i9</pre>
	 */
	int LONG_TIMEOUT = 5 * TIMEOUT;

	static String[] getParameterValues() {
		List<String> values = new ArrayList<>();
		values.add( ProgramFlag.PROFILE );
		values.add( Profile.TEST );
		values.add( ProgramFlag.LOG_LEVEL );
		values.add( ProgramFlag.ERROR );
		values.add( ProgramFlag.NOSPLASH );
		return values.toArray( new String[ 0 ] );
	}

}
