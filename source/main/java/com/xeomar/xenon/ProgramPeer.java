package com.xeomar.xenon;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.List;

public class ProgramPeer {

	private static final Logger log = LoggerFactory.getLogger( ProgramPeer.class );

	private Program program;

	private int port;

	ProgramPeer( Program program, int port ) {
		this.program = program;
		this.port = port;
	}

	public void run() {
		// Running as a peer
		if( log.isDebugEnabled()) {
			log.debug( "Program already running on port " + port );
		} else {
			log.info( "Program already running" );
		}

		// SEND the command line parameters to a host
		try {
			Socket socket = new Socket( InetAddress.getLoopbackAddress(), port );
			List<String> commandList = program.getProgramParameters().getOriginalCommands();
			String[] commands = commandList.toArray( new String[ commandList.size() ] );
			new ObjectOutputStream( socket.getOutputStream() ).writeObject( commands );

			// TODO Read in output from the host and print it to the command line
			// NOTE How to determine the log level?
			// NOTE Can I just get back log event objects?

			socket.close();
		} catch( IOException socketException ) {
			log.error( "Error sending commands to program", socketException );
		}
	}

}
