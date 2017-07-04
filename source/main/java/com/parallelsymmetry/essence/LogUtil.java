package com.parallelsymmetry.essence;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.LogManager;
import java.util.logging.XMLFormatter;

public class LogUtil {

	public static Logger get( Class<?> clazz ) {
		return LoggerFactory.getLogger( clazz );
	}

	static void configureLogging( String level ) {
		// Logging level conversion
		// SLF4J - Java
		// ---------------
		// ERROR - SEVERE
		// WARN  - WARNING
		// INFO  - INFO
		// DEBUG - FINE
		// TRACE - FINEST

		StringBuilder builder = new StringBuilder();
		// Add the log console handler
		builder.append( "handlers=java.util.logging.ConsoleHandler\n" );

		// TODO Add the log file handler
		//builder.append( "handlers=java.util.logging.ConsoleHandler,java.util.logging.FileHandler\n" );

		// Configure the simple formatter
		// https://docs.oracle.com/javase/7/docs/api/java/util/Formatter.html#syntax
		builder.append( ProgramFormatter.class.getName() + ".format=%1$tF %1$tT.%1$tL %4$s %2$s: %5$s %6$s%n\n" );

		// Configure the console handler
		builder.append( "java.util.logging.ConsoleHandler.level=FINEST\n" );
		builder.append( "java.util.logging.ConsoleHandler.formatter=" + ProgramFormatter.class.getName() + "\n" );

		// Configure the file handler
		builder.append( "java.util.logging.FileHandler.pattern=%h/java%u.log\n" );
		builder.append( "java.util.logging.FileHandler.limit=50000\n" );
		builder.append( "java.util.logging.FileHandler.count=1\n" );
		builder.append( "java.util.logging.FileHandler.formatter=" + XMLFormatter.class.getName() + "\n" );

		// Set the default log level
		builder.append( ".level=" );
		builder.append( getDefaultLogLevel( level ) );
		builder.append( "\n" );

		// Set the program log level
		builder.append( Program.class.getPackageName() );
		builder.append( ".level=" );
		builder.append( getProgramLogLevel( level ) );
		builder.append( "\n" );

		// Initialize the logging
		try {
			InputStream input = new ByteArrayInputStream( builder.toString().getBytes( "utf-8" ) );
			LogManager.getLogManager().readConfiguration( input );
		} catch( IOException exception ) {
			exception.printStackTrace( System.err );
		}
	}

	private static String getDefaultLogLevel( String level ) {
		String result = level.toUpperCase();

		switch( result ) {
			case "ERROR": {
				result = "ERROR";
				break;
			}
			case "WARN": {
				result = "WARNING";
				break;
			}
			default: {
				result = "INFO";
				break;
			}
		}

		return result;
	}

	private static String getProgramLogLevel( String level ) {
		String result = level.toUpperCase();

		switch( result ) {
			case "ERROR": {
				result = "ERROR";
				break;
			}
			case "WARN": {
				result = "WARNING";
				break;
			}
			case "INFO": {
				result = "INFO";
				break;
			}
			case "DEBUG": {
				result = "FINE";
				break;
			}
			case "TRACE": {
				result = "FINEST";
				break;
			}
			default: {
				result = "ERROR";
				break;
			}
		}

		return result;
	}

}
