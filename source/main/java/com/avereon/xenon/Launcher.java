package com.avereon.xenon;

import com.avereon.weave.ElevatedFlag;
import com.avereon.weave.UpdateFlag;
import com.avereon.weave.Weave;

public class Launcher {

	public static void main( String[] commands ) {
		ProgramConfig.configureCustomLauncherName();

		com.avereon.util.Parameters parameters = com.avereon.util.Parameters.parse( commands );

		boolean update = parameters.isSet( UpdateFlag.UPDATE );
		boolean callback = parameters.isSet( ElevatedFlag.CALLBACK_SECRET );
		boolean updating = update || callback;

		if( updating ) {
			new Weave().start( commands );
		} else {
			Xenon.doLaunch( commands );
		}
	}

}
