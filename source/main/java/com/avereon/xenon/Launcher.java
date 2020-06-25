package com.avereon.xenon;

import com.avereon.zenna.ElevatedFlag;

public class Launcher {

	public static void main( String[] commands ) {
		com.avereon.util.Parameters parameters = com.avereon.util.Parameters.parse( commands );
		if( parameters.isSet( ElevatedFlag.CALLBACK_SECRET ) ) {
			new com.avereon.zenna.Program().start( commands );
			System.exit( 0 );
		} else if( parameters.isSet( ProgramFlag.UPDATE ) ) {
			new com.avereon.zenna.Program().start( new Program().updateProgram( parameters ) );
			System.exit( 0 );
		} else {
			Program.launch( commands );
		}
	}

}
