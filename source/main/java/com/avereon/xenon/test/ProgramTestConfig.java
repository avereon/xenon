package com.avereon.xenon.test;

import com.avereon.xenon.XenonFlag;
import com.avereon.xenon.XenonMode;
import com.avereon.xenon.XenonTestFlag;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

public final class ProgramTestConfig {

	/**
	 * The wait timeout for many operations. Common values are:
	 * <pre>
	 * 5000 - GitHub Actions, Mintbox Mini
	 *  | Slower computers
	 *  |
	 *  | Faster computers
	 * 1000 - AMD Threadripper, Intel i9</pre>
	 */
	public static final int TIMEOUT = 5000;

	/**
	 * The long wait timeout for many operations. Common values are:
	 * <pre>
	 * 25000 - GitHub Actions, Mintbox Mini
	 *  | Slower computers
	 *  |
	 *  | Faster computers
	 * 5000 - AMD Threadripper, Intel i9</pre>
	 */
	public static final int LONG_TIMEOUT = 5 * TIMEOUT;

	@Getter
	@Setter
	private static List<String> parameters;

	static {
		List<String> values = new ArrayList<>();
		values.add( XenonFlag.RESET );
		values.add( XenonFlag.NO_SPLASH );
		values.add( XenonFlag.NO_UPDATES );
		values.add( XenonFlag.MODE );
		values.add( XenonMode.TEST );
		values.add( XenonFlag.LOG_LEVEL );
		values.add( XenonFlag.WARN );
		values.add( XenonTestFlag.EMPTY_WORKSPACE );
		parameters = values;
	}

	public static void addFlag(String flag) {
		parameters.add( flag );
	}

	public static void removeFlag( String flag ) {
		parameters.remove( flag );
	}

}
