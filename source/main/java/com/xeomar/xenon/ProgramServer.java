package com.xeomar.xenon;

import com.xeomar.settings.Settings;
import com.xeomar.util.Parameters;
import com.xeomar.util.TestUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.*;
import java.util.List;

public class ProgramServer {

	private static final Logger log = LoggerFactory.getLogger( ProgramServer.class );

	private Program program;

	private ServerSocket server;

	private SocketHandler handler;

	public ProgramServer( Program program ) {
		this.program = program;
	}

	public boolean start() {
		Settings programSettings = program.getSettingsManager().getSettings( ProgramSettings.PROGRAM );
		int port = programSettings.get( "program-port", Integer.class, 0 );

		// Start the program server
		try {
			server = new ServerSocket();
			server.setReuseAddress( true );
			server.bind( new InetSocketAddress( InetAddress.getLoopbackAddress(), port ) );
			int localPort = server.getLocalPort();
			programSettings.set( "program-port", localPort );
			programSettings.flush();
			log.info( "Program server listening on port " + localPort );

			Thread serverThread = new Thread( handler = new SocketHandler(), "ProgramServerThread" );
			serverThread.setDaemon( true );
			serverThread.start();
		} catch( BindException exception ) {
			log.info( "Program already running" );

			// Send the command line parameters
			try {
				// WRITE the command line parameters to a peer
				Socket socket = new Socket( InetAddress.getLoopbackAddress(), port );
				List<String> commandList = program.getProgramParameters().getOriginalCommands();
				String[] commands = commandList.toArray( new String[ commandList.size() ] );
				new ObjectOutputStream( socket.getOutputStream() ).writeObject( commands );
				socket.close();
			} catch( IOException socketException ) {
				log.error( "Error sending commands to program", socketException );
			}

			return false;
		} catch( IOException exception ) {
			log.error( "Error starting program server", exception );
		}

		return true;
	}

	public void stop() {
		if( server == null ) return;
		if( handler != null ) handler.stop();

		Settings programSettings = program.getSettingsManager().getSettings( ProgramSettings.PROGRAM );
		programSettings.set( "program-port", 0 );
		programSettings.flush();
	}

	private class SocketHandler implements Runnable {

		private boolean run;

		public void run() {
			run = true;
			while( run ) {
				try {
					// READ the command line parameters from a peer
					Socket client = server.accept();
					String[] commands = (String[])new ObjectInputStream( client.getInputStream() ).readObject();
					com.xeomar.util.Parameters parameters = Parameters.parse( commands );
					log.warn( "Parameters from peer: " + parameters );
					program.processCommands( parameters );
					program.processResources( parameters );
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

}
