package com.avereon.xenon;

import com.avereon.weave.ElevatedFlag;
import com.avereon.weave.UpdateFlag;
import com.avereon.weave.Weave;

/**
 * The Launcher class is the entry point for the application. The Launcher is
 * also responsible for determining if the application should be updated or
 * if the application should be launched normally, based on the command line.
 */
public class Launcher {

	public static void main( String[] commands ) {
		XenonLauncherConfig.setCustomLauncherSystemProperty();

		com.avereon.util.Parameters parameters = com.avereon.util.Parameters.parse( commands );
		boolean callback = parameters.isSet( ElevatedFlag.CALLBACK_SECRET );
		boolean update = parameters.isSet( UpdateFlag.UPDATE );
		boolean updating = update || callback;

		if( updating ) {
			Weave.launch( commands );
		} else {
			Xenon.launch( commands );
		}
	}

}
