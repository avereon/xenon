package com.avereon.xenon;

import com.avereon.skill.Controllable;
import com.avereon.util.Log;
import com.avereon.util.TextUtil;
import com.google.common.flogger.LazyArgs;
import lombok.extern.flogger.Flogger;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.List;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;

import static com.google.common.flogger.LazyArgs.lazy;

@Flogger
public class ProgramPeer implements Controllable<ProgramPeer> {

	private final Program program;

	private final int port;

	private Socket socket;

	ProgramPeer( Program program, int port ) {
		this.program = program;
		this.port = port;
	}

	@Override
	public ProgramPeer start() {
		// Running as a peer
		if( log.atFine().isEnabled() ) {
			log.atFine().log( "Program already running on port %s", port );
		} else {
			log.atInfo().log( "Program already running" );
		}

		try {
			socket = new Socket( InetAddress.getLoopbackAddress(), port );
			sendCommands( socket );
			readMessages( socket );
		} catch( IOException exception ) {
			log.atSevere().withCause( exception ).log( "Error reading commands to program" );
		}
		return this;
	}

	@Override
	public boolean isRunning() {
		return socket != null && !socket.isClosed();
	}

	@Override
	public ProgramPeer stop() {
		try {
			socket.close();
		} catch( IOException exception ) {
			log.atWarning().withCause( exception ).log( "Error closing peer socket" );
		}

		return this;
	}

	private void sendCommands( Socket socket ) {
		try {
			List<String> commandList = program.getProgramParameters().getOriginalCommands();
			String[] commands = commandList.toArray( new String[ 0 ] );
			log.atInfo().log( "%s", lazy( () -> TextUtil.toString( commands, " " ) ) );
			ObjectOutputStream commandStream = new ObjectOutputStream( socket.getOutputStream() );
			commandStream.writeObject( commands );
			commandStream.flush();
		} catch( IOException exception ) {
			log.atSevere().withCause( exception ).log( "Error sending commands to host" );
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
		} catch( IOException exception ) {
			log.atFine().log( "Host is done sending messages" );
		} catch( Exception exception ) {
			log.atSevere().withCause( exception ).log( "Error reading commands from host" );
		}
	}

	private void watchOne( InputStream input ) throws IOException, ClassNotFoundException {
		ObjectInputStream objectInput = new ObjectInputStream( input );
		Object object = objectInput.readObject();
		if( object == null ) return;
		if( object instanceof LogRecord ) {
			// NOTE This goes around the logging API to get to the logging implementation
			LogManager.getLogManager().getLogger( "" ).log( (LogRecord)object );
		} else {
			log.atInfo().log( "%s", object );
		}
	}

	private void watchAll( InputStream input ) throws IOException, ClassNotFoundException {
		ObjectInputStream objectInput = new ObjectInputStream( input );
		Object object;
		while( (object = objectInput.readObject()) != null ) {
			if( object instanceof LogRecord ) {
				LogManager.getLogManager().getLogger( "" ).log( (LogRecord)object );
			} else {
				log.atInfo().log( "%s", object );
			}
		}
	}

}
