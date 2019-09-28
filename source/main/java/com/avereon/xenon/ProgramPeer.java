package com.avereon.xenon;

import com.avereon.util.LogUtil;
import org.slf4j.Logger;

import java.io.*;
import java.lang.invoke.MethodHandles;
import java.net.InetAddress;
import java.net.Socket;
import java.util.List;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;

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

		try {
			Socket socket = new Socket( InetAddress.getLoopbackAddress(), port );
			sendCommands( socket );
			readMessages( socket );
		} catch( IOException exception ) {
			log.error( "Error reading commands to program", exception );
		}
	}

	private void sendCommands( Socket socket ) {
		try {
			List<String> commandList = program.getProgramParameters().getOriginalCommands();
			String[] commands = commandList.toArray( new String[ 0 ] );
			ObjectOutputStream commandStream = new ObjectOutputStream( socket.getOutputStream() );
			commandStream.writeObject( commands );
			commandStream.flush();
		} catch( IOException exception ) {
			log.error( "Error sending commands to host", exception );
		}
	}

	private void readMessages( Socket socket ) {
		try {
			if( program.getProgramParameters().isSet( ProgramFlag.WATCH ) ) {
				watchAll( socket.getInputStream() );
			} else if( program.getProgramParameters().anySet( ProgramFlag.ACTIONS ) ) {
				watchOne( socket.getInputStream() );
			}
			socket.close();
		} catch( EOFException exception ) {
			log.debug( "Host is done sending messages" );
		} catch( ClassNotFoundException | IOException exception ) {
			log.error( "Error reading commands to program", exception );
		}
	}

	private void watchOne( InputStream input ) throws IOException, ClassNotFoundException {
		ObjectInputStream objectInput = new ObjectInputStream( input );
		Object object = objectInput.readObject();
		if( object == null ) return;
		if( object instanceof LogRecord ) {
			LogManager.getLogManager().getLogger( "" ).log( (LogRecord)object );
		} else {
			log.info( String.valueOf( object ) );
		}
	}

	private void watchAll( InputStream input ) throws IOException, ClassNotFoundException {
		ObjectInputStream objectInput = new ObjectInputStream( input );
		Object object;
		while( (object = objectInput.readObject()) != null ) {
			if( object instanceof LogRecord ) {
				LogManager.getLogManager().getLogger( "" ).log( (LogRecord)object );
			} else {
				log.info( String.valueOf( object ) );
			}
		}
	}

}
