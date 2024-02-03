package com.avereon.xenon;

import com.avereon.settings.Settings;
import com.avereon.skill.Controllable;
import com.avereon.util.Parameters;
import lombok.CustomLog;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.*;
import java.util.logging.Handler;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;

@CustomLog
public class ProgramServer implements Controllable<ProgramServer> {

	private final Xenon program;

	private final int requestedPort;

	private ServerSocket server;

	private SocketHandler handler;

	ProgramServer( Xenon program, int requestedPort ) {
		this.program = program;
		this.requestedPort = requestedPort;
	}

	public int getLocalPort() {
		return server == null ? 0 : server.getLocalPort();
	}

	@Override
	public boolean isRunning() {
		return (server != null && server.isBound());
	}

	public ProgramServer start() {
		Settings programSettings = program.getSettingsManager().getSettings( ProgramSettings.PROGRAM );

		// Start the program server
		try {
			server = new ServerSocket();
			server.setReuseAddress( true );
			server.bind( new InetSocketAddress( InetAddress.getLoopbackAddress(), requestedPort ) );
			int localPort = server.getLocalPort();
			programSettings.set( "program-port", localPort );
			programSettings.flush();
			log.atFine().log( "Program server started on port %s", localPort );

			Thread serverThread = new Thread( handler = new SocketHandler(), "ProgramServerThread" );
			serverThread.setDaemon( true );
			serverThread.start();
		} catch( BindException exception ) {
			return this;
		} catch( IOException exception ) {
			log.atSevere().withCause( exception ).log( "Error starting program server" );
		}

		return this;
	}

	public ProgramServer stop() {
		if( server == null ) return this;
		if( handler != null ) handler.stop();

		Settings programSettings = program.getSettingsManager().getSettings( ProgramSettings.PROGRAM );
		programSettings.set( "program-port", 0 );
		programSettings.flush();

		return this;
	}

	private class SocketHandler implements Runnable {

		private boolean run;

		public void run() {
			run = true;
			while( run ) {
				try {
					// Read the command line parameters from the peer
					Socket client = server.accept();
					String[] commands = (String[])new ObjectInputStream( client.getInputStream() ).readObject();
					com.avereon.util.Parameters parameters = Parameters.parse( commands );
					log.atWarning().log( "Parameters from peer: %s", parameters );

					// Process the parameters and send messages to the peer
					Handler peerLogHandler = new LogHandler( client );
					LogManager.getLogManager().getLogger( "" ).addHandler( peerLogHandler );
					program.processCliActions( parameters, false );
					program.processAssets( parameters );
					//LogManager.getLogManager().getLogger( "" ).removeHandler( peerLogHandler );
				} catch( ClassNotFoundException exception ) {
					log.atSevere().withCause( exception ).log( "Error reading commands from client" );
				} catch( IOException exception ) {
					String message = exception.getMessage();
					message = message == null ? "null" : message.toLowerCase();
					if( !"socket closed".equals( message ) ) log.atSevere().withCause( exception ).log( "Error waiting for connection" );
				}
			}
		}

		public void stop() {
			run = false;
			try {
				server.close();
			} catch( IOException exception ) {
				log.atSevere().withCause( exception ).log( "Error closing server socket" );
			} finally {
				server = null;
			}
			log.atFine().log( "Program server stopped listening" );
		}

	}

	private static class LogHandler extends Handler {

		private final Socket client;

		private final ObjectOutputStream objectOutput;

		LogHandler( Socket client ) throws IOException {
			this.client = client;
			this.objectOutput = new ObjectOutputStream( client.getOutputStream() );
		}

		@Override
		public void publish( LogRecord record ) {
			try {
				if( client.isConnected() ) objectOutput.writeObject( record );
			} catch( Exception exception ) {
				close();
			}
		}

		@Override
		public void flush() {
			try {
				if( client.isConnected() ) objectOutput.flush();
			} catch( Exception exception ) {
				close();
			}
		}

		@Override
		public void close() throws SecurityException {
			try {
				client.close();
			} catch( IOException exception ) {
				exception.printStackTrace( System.err );
			} finally {
				LogManager.getLogManager().getLogger( "" ).removeHandler( this );
			}
		}

	}

}
