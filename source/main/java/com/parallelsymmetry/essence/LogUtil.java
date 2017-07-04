package com.parallelsymmetry.essence;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogUtil {

	public static Logger get( Class<?> clazz ) {
		return LoggerFactory.getLogger( clazz );
	}

	static void configureLogging( Program program ) {

	}

	static String getLogLevel( Program program ) {
		String level = program.getParameter( ProgramParameter.LOG_LEVEL );
		if( level != null ) {
			try {
				level = level.toUpperCase();
				switch( level ) {
					case "ERROR": {
						level = "SEVERE";
						break;
					}
					case "WARN": {
						level = "WARNING";
						break;
					}
					case "DEBUG": {
						level = "FINE";
						break;
					}
					case "TRACE": {
						level = "FINEST";
						break;
					}
				}
			} catch( Exception exception ) {
				// Intentionally ignore exception
			}
		}
		return level;
	}

}
