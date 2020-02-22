package com.avereon.xenon;

import com.avereon.event.Event;
import com.avereon.event.EventHandler;
import com.avereon.event.EventType;
import com.avereon.util.Log;
import com.avereon.xenon.task.TaskEvent;

import java.lang.System.Logger;
import java.util.HashMap;
import java.util.Map;

public class ProgramEventWatcher implements EventHandler<Event> {

	private static final Logger log = Log.get();

	private static final Map<EventType<?>, Logger.Level> LEVELS;

	private static final Logger.Level DEFAULT_LEVEL = Log.TRACE;

	static {
		LEVELS = new HashMap<>();
		LEVELS.put( ProgramEvent.STARTING, Log.DEBUG );
		LEVELS.put( ProgramEvent.STARTED, Log.INFO );
		LEVELS.put( ProgramEvent.STOPPING, Log.DEBUG );
		LEVELS.put( ProgramEvent.STOPPED, Log.INFO );
		LEVELS.put( TaskEvent.PROGRESS, Log.TRACE );

		//LEVELS.put( AssetEvent.ANY, Log.WARN );
	}

	public void handle( Event event ) {
		log.log( getLevel( event ), String.valueOf( event ) );
	}

	private Logger.Level getLevel( Event event ) {
		EventType<?> type = event.getEventType();

		while( type != null ) {
			Logger.Level level = LEVELS.get( type );
			if( level != null ) return level;
			type = type.getParentEventType();
		}

		return DEFAULT_LEVEL;
	}

}
