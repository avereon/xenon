package com.avereon.xenon;

import com.avereon.util.LogUtil;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.invoke.MethodHandles;
import java.net.InetAddress;
import java.net.Socket;
import java.util.List;

public class ProgramPeer {

	private static final Logger log = LogUtil.get( MethodHandles.lookup().lookupClass() );

	private Program program;

	private int port;

	ProgramPeer( Program program, int port ) {
		this.program = program;
		this.port = port;
	}

	public void run() {
		// Running as a peer
		if( log.isDebugEnabled() ) {
			log.debug( "Program already running on port {}", port );
		} else {
			log.info( "Program already running" );
		}

		// SEND the command line parameters to a host
		try {
			Socket socket = new Socket( InetAddress.getLoopbackAddress(), port );
			List<String> commandList = program.getProgramParameters().getOriginalCommands();
			String[] commands = commandList.toArray( new String[ commandList.size() ] );
			new ObjectOutputStream( socket.getOutputStream() ).writeObject( commands );

			// TODO If watch command specified, read log events from the host and submit them to the logging framework

			socket.close();
		} catch( IOException socketException ) {
			log.error( "Error sending commands to program", socketException );
		}
	}

}
