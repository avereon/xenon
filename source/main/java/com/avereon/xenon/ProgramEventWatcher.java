package com.avereon.xenon;

import com.avereon.event.Event;
import com.avereon.event.EventHandler;
import com.avereon.event.EventType;
import com.avereon.xenon.task.TaskEvent;
import lombok.CustomLog;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

@CustomLog
public class ProgramEventWatcher implements EventHandler<Event> {

	private static final Map<EventType<?>, Level> LEVELS;

	private static final Level DEFAULT_LEVEL = Level.FINER;

	static {
		LEVELS = new HashMap<>();
		LEVELS.put( ProgramEvent.STARTING, Level.FINE );
		LEVELS.put( ProgramEvent.STARTED, Level.INFO );
		LEVELS.put( ProgramEvent.STOPPING, Level.FINE );
		LEVELS.put( ProgramEvent.STOPPED, Level.INFO );
		LEVELS.put( TaskEvent.PROGRESS, Level.FINER );

		//LEVELS.put( AssetEvent.ANY, Log.WARN );
	}

	public void handle( Event event ) {
		log.at( getLevel( event ) ).log( String.valueOf( event ) );
	}

	private Level getLevel( Event event ) {
		EventType<?> type = event.getEventType();

		while( type != null ) {
			Level level = LEVELS.get( type );
			if( level != null ) return level;
			type = type.getParentEventType();
		}

		return DEFAULT_LEVEL;
	}

}
