package com.parallelsymmetry.essence;

import com.parallelsymmetry.essence.util.JavaUtil;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.function.Function;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;

public class ProgramFormatter extends Formatter {

	private static final String FORMAT_PROPERTY_KEY = ProgramFormatter.class.getName() + ".format";

	private static final String DEFAULT_FORMAT = "%1$tF %1$tT.%1$tL %4$s %2$s %5$s %6$s%n";

	private final String format = getSimpleFormat( ProgramFormatter::getLoggingProperty );

	@Override
	public String format( LogRecord record ) {
		// Timestamp
		ZonedDateTime timestamp = ZonedDateTime.ofInstant( record.getInstant(), ZoneId.systemDefault() );

		// Source
		String source = record.getSourceClassName();
		if( source == null ) {
			source = record.getLoggerName();
		} else {
			source = JavaUtil.getShortClassName( source );
			if( record.getSourceMethodName() != null ) source += "." + record.getSourceMethodName();
		}

		// Logger
		String logger = record.getLoggerName();

		// Level
		String level = getLevel( record.getLevel() );
		//String level = record.getLevel().getLocalizedName();

		// Message
		String message = formatMessage( record );

		// Throwable
		String throwable = "";
		if( record.getThrown() != null ) {
			StringWriter writer = new StringWriter();
			PrintWriter printer = new PrintWriter( writer );
			printer.println();
			record.getThrown().printStackTrace( printer );
			printer.close();
			throwable = writer.toString();
		}

		return String.format( format, timestamp, source, logger, level, message, throwable );
	}

	private String getLevel( Level level ) {
		String result = "[I]";

		switch( level.getName() ) {
			case ("SEVERE"): {
				result = "[E]";
				break;
			}
			case ("WARNING"): {
				result = "[W]";
				break;
			}
			case ("INFO"): {
				result = "[I]";
				break;
			}
			case ("FINE"): {
				result = "[D]";
				break;
			}
			case ("FINEST"): {
				result = "[T]";
				break;
			}
		}

		return result;
	}

	private static String getSimpleFormat( Function<String, String> defaultPropertyGetter ) {
		String format = getLoggingProperty( FORMAT_PROPERTY_KEY );
		if( format == null ) format = DEFAULT_FORMAT;
		return format;
	}

	private static String getLoggingProperty( String name ) {
		return LogManager.getLogManager().getProperty( name );
	}

}
