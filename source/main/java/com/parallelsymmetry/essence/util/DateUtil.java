package com.parallelsymmetry.essence.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class DateUtil {

	public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

	public static final TimeZone DEFAULT_TIME_ZONE = TimeZone.getTimeZone( "UTC" );

	/**
	 * Convenience method to get the current year for the UTC time zone.
	 *
	 * @return
	 */
	public static final int getCurrentYear() {
		return getCurrentYear( DEFAULT_TIME_ZONE );
	}

	/**
	 * Convenience method to get the current year based on time zone.
	 *
	 * @param timeZone
	 * @return
	 */
	public static final int getCurrentYear( String timeZone ) {
		return getCurrentYear( TimeZone.getTimeZone( timeZone ) );
	}

	/**
	 * Convenience method to get the current year based on time zone.
	 *
	 * @param timeZone
	 * @return
	 */
	public static final int getCurrentYear( TimeZone timeZone ) {
		return Calendar.getInstance( timeZone ).get( Calendar.YEAR );
	}

	/**
	 * Parse a date string with the given format using the standard time zone.
	 *
	 * @param format
	 * @param data
	 * @return
	 */
	public static final Date parse( String data, String format ) {
		return parse( data, format, DEFAULT_TIME_ZONE );
	}

	/**
	 * Parse a date string with the given format and time zone.
	 *
	 * @param data
	 * @param format
	 * @param timeZone
	 * @return
	 */
	public static final Date parse( String data, String format, String timeZone ) {
		return parse( data, format, timeZone == null ? null : TimeZone.getTimeZone( timeZone ) );
	}

	/**
	 * Parse a date string with the given format and time zone.
	 *
	 * @param data
	 * @param format
	 * @param timeZone
	 * @return
	 */
	public static final Date parse( String data, String format, TimeZone timeZone ) {
		if( data == null ) return null;

		SimpleDateFormat formatter = new SimpleDateFormat( format );
		formatter.setTimeZone( timeZone );

		try {
			return formatter.parse( data );
		} catch( ParseException exception ) {
			return null;
		}
	}

	/**
	 * Format a date with the given format using the standard time zone.
	 *
	 * @param date
	 * @param format
	 * @return
	 */
	public static final String format( Date date, String format ) {
		return format( date, format, DEFAULT_TIME_ZONE );
	}

	/**
	 * Format a date with the given format and time zone.
	 *
	 * @param date
	 * @param format
	 * @param timeZone
	 * @return
	 */
	public static final String format( Date date, String format, String timeZone ) {
		return format( date, format, timeZone );
	}

	/**
	 * Format a date with the given format and time zone.
	 *
	 * @param date
	 * @param format
	 * @param timeZone
	 * @return
	 */
	public static final String format( Date date, String format, TimeZone timeZone ) {
		SimpleDateFormat formatter = new SimpleDateFormat( format );
		formatter.setTimeZone( timeZone );
		return formatter.format( date );
	}

	public static final String formatDuration( long time ) {
		long millis = time % 1000;
		time /= 1000;
		long seconds = time % 60;
		time /= 60;
		long minutes = time % 60;
		time /= 60;
		long hours = time % 24;
		time /= 24;

		StringBuilder builder = new StringBuilder();
		if( time != 0 ) {
			builder.append( " " );
			builder.append( time );
			builder.append( "d" );
		}

		if( hours != 0 ) {
			builder.append( " " );
			builder.append( hours );
			builder.append( "h" );
		}

		if( minutes != 0 ) {
			builder.append( " " );
			builder.append( minutes );
			builder.append( "m" );
		}

		if( seconds != 0 ) {
			builder.append( " " );
			builder.append( seconds );
			builder.append( "s" );
		}

		if( millis != 0 ) {
			builder.append( " " );
			builder.append( millis );
			builder.append( "ms" );
		}

		return builder.toString().trim();
	}

}
