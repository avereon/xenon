package com.xeomar.xenon;

import java.io.IOException;
import java.net.ServerSocket;

public class ProgramServer {

	private int port;

	private ServerSocket socket;

	public ProgramServer( int port ) {
		this.port = port;
	}

	public Object start() throws IOException {
		// NEXT Implement program server
		return null;
	}

	public boolean isRunning() {
		return socket != null;
	}

	public void stop() throws IOException {
		if( socket == null ) return;
		socket.close();
	}

	int getPort() {
		return socket == null ? 0 : socket.getLocalPort();
	}

}
