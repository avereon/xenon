package com.avereon.xenon.notice;

import com.avereon.util.TextUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

public class NoticeLogHandler extends Handler {

	private static final Map<Level, Notice.Type> types;

	private NoticeManager manager;

	static {
		types = new HashMap<>();
		types.put( Level.SEVERE, Notice.Type.ERROR );
		types.put( Level.WARNING, Notice.Type.WARN );
	}

	public NoticeLogHandler( NoticeManager manager ) {
		this.manager = manager;
	}

	@Override
	public void publish( LogRecord record ) {
		Notice.Type type = types.get( record.getLevel() );
		Throwable thrown = record.getThrown();
		if( type == null || thrown == null ) return;
		fault( type, record, thrown );
	}

	private void fault( Notice.Type type, LogRecord record, Throwable thrown ) {
		String title = manager.getThrowableTitle( thrown );

		String message = record.getMessage();
		if( TextUtil.isEmpty( message ) ) message = manager.getThrowableMessage( thrown );

		manager.fault( title, message, thrown, type, record.getParameters() );
	}

	@Override
	public void flush() { }

	@Override
	public void close() throws SecurityException { }

}
