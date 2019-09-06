package com.avereon.xenon;

import com.avereon.settings.Settings;
import com.avereon.util.Controllable;
import com.avereon.util.LogUtil;
import com.avereon.util.Parameters;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.invoke.MethodHandles;
import java.net.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

public class ProgramServer implements Controllable<ProgramServer> {

	private static final Logger log = LogUtil.get( MethodHandles.lookup().lookupClass() );

	private Program program;

	private ServerSocket server;

	private SocketHandler handler;

	private int requestedPort;

	public ProgramServer( Program program, int requestedPort ) {
		this.program = program;
		this.requestedPort = requestedPort;
	}

	public int getLocalPort() {
		return server == null ? 0 : server.getLocalPort();
	}

	@Override
	public boolean isRunning() {
		return ( server != null && server.isBound() );
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
			log.info( "Program server listening on port " + localPort );

			Thread serverThread = new Thread( handler = new SocketHandler(), "ProgramServerThread" );
			serverThread.setDaemon( true );
			serverThread.start();
		} catch( BindException exception ) {
			return this;
		} catch( IOException exception ) {
			log.error( "Error starting program server", exception );
		}

		return this;
	}

	@Override
	public ProgramServer awaitStart( long l, TimeUnit timeUnit ) throws InterruptedException {
		return this;
	}

	@Override
	public ProgramServer restart() {
		return this;
	}

	@Override
	public ProgramServer awaitRestart( long l, TimeUnit timeUnit ) throws InterruptedException {
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

	@Override
	public ProgramServer awaitStop( long l, TimeUnit timeUnit ) throws InterruptedException {
		return this;
	}

	private class SocketHandler implements Runnable {

		private Handler peerLogHandler;

		private boolean run;

		public void run() {
			run = true;
			while( run ) {
				try {
					// READ the command line parameters from a peer
					Socket client = server.accept();
					String[] commands = (String[])new ObjectInputStream( client.getInputStream() ).readObject();
					com.avereon.util.Parameters parameters = Parameters.parse( commands );
					log.warn( "Parameters from peer: " + parameters );

					// TODO Register a log event listener to send events to the peer

					program.processControlCommands( parameters );
					program.processResources( parameters );

					// TODO Unregister the log event listener
				} catch( ClassNotFoundException exception ) {
					log.error( "Error reading commands from client", exception );
				} catch( IOException exception ) {
					String message = exception.getMessage();
					message = message == null ? "null" : message.toLowerCase();
					if( !"socket closed".equals( message ) ) log.error( "Error waiting for connection", exception );
				}
			}
		}

		public void stop() {
			run = false;
			try {
				server.close();
			} catch( IOException exception ) {
				log.error( "Error closing server socket", exception );
			} finally {
				server = null;
			}
		}

	}

	public class LogHandler extends Handler {

		@Override
		public void publish( LogRecord record ) {
			System.err.println( "SEND TO PEER> " + record.getMessage() );
		}

		@Override
		public void flush() {

		}

		@Override
		public void close() throws SecurityException {

		}

	}

}
