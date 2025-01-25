package com.avereon.xenon.product;

import com.avereon.xenon.ProgramTestCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

@ExtendWith( MockitoExtension.class )
class ProductManagerTest extends ProgramTestCase {

	// The tolerance in seconds for time comparisons
	private static final int TOLERANCE = 10;

	private ProductManager productManager;

	@BeforeEach
	public void setup() throws Exception {
		super.setup();
		productManager = new ProductManager( getProgram() ).start();
		productManager.setCheckOption( null );
		productManager.setFoundOption( null );
		productManager.getSettings().set( ProductManager.LAST_CHECK_TIME, null );
		productManager.getSettings().set( ProductManager.NEXT_CHECK_TIME, null );
		productManager.getSettings().flush();
	}

	@Test
	void testGetCheckOption() {
		assertThat( productManager.getCheckOption() ).isEqualTo( ProductManager.CheckOption.MANUAL );
	}

	@Test
	void testGetFoundOption() {
		assertThat( productManager.getFoundOption() ).isEqualTo( ProductManager.FoundOption.NOTIFY );
		productManager.setFoundOption( ProductManager.FoundOption.APPLY );
		assertThat( productManager.getFoundOption() ).isEqualTo( ProductManager.FoundOption.APPLY );
	}

	@Test
	void testGetLastUpdateCheck() {
		productManager.getSettings().set( ProductManager.LAST_CHECK_TIME, null );
		assertThat( productManager.getLastUpdateCheck() ).isEqualTo( null );

		long now = System.currentTimeMillis();
		productManager.getSettings().set( ProductManager.LAST_CHECK_TIME, now );
		assertThat( productManager.getLastUpdateCheck() ).isEqualTo( now );
	}

	@Test
	void testGetNextUpdateCheck() {
		productManager.getSettings().set( ProductManager.NEXT_CHECK_TIME, null );
		assertThat( productManager.getNextUpdateCheck() ).isEqualTo( null );

		long now = System.currentTimeMillis();
		productManager.getSettings().set( ProductManager.NEXT_CHECK_TIME, now );
		assertThat( productManager.getNextUpdateCheck() ).isEqualTo( now );
	}

	@Test
	void scheduleUpdateCheckWithManual() {
		// given
		assertThat( getProgram().isProgramUpdated() ).isFalse();
		assertThat( getProgram().isUpdateInProgress() ).isFalse();
		assertThat( productManager.getNextUpdateCheck() ).isNull();

		// These cause events to be fired that cause scheduleUpdateCheck to be called
		productManager.getSettings().set( ProductManager.LAST_CHECK_TIME, null );
		productManager.getSettings().set( ProductManager.NEXT_CHECK_TIME, null );
		productManager.setCheckOption( ProductManager.CheckOption.MANUAL );

		// when
		productManager.scheduleUpdateCheck( false );

		// then
		assertThat( productManager.getNextUpdateCheck() ).isNull();
	}

	@Test
	void scheduleUpdateCheckWithStartup() {
		// given
		assertThat( getProgram().isProgramUpdated() ).isFalse();
		assertThat( getProgram().isUpdateInProgress() ).isFalse();

		// These cause events to be fired that cause scheduleUpdateCheck to be called
		productManager.getSettings().set( ProductManager.LAST_CHECK_TIME, null );
		productManager.getSettings().set( ProductManager.NEXT_CHECK_TIME, null );
		productManager.setCheckOption( ProductManager.CheckOption.STARTUP );
		productManager.getSettings().flush();

		// when
		productManager.scheduleUpdateCheck( false );

		// then
		assertThat( productManager.getNextUpdateCheck() ).isEqualTo( null );
	}

	@ParameterizedTest
	@MethodSource( "provideCheckIntervalOptions" )
	void scheduleUpdateCheckWithInterval( ProductManager.CheckInterval checkInterval, Long priorLastCheckTime, Long priorNextCheckTime, Long expectedNextCheckTime ) {
		// given
		assertThat( getProgram().isProgramUpdated() ).isFalse();
		assertThat( getProgram().isUpdateInProgress() ).isFalse();

		// Momentarily set the check option to manual to avoid bad interaction
		productManager.setCheckOption( ProductManager.CheckOption.MANUAL );
		productManager.getSettings().set( ProductManager.LAST_CHECK_TIME, priorLastCheckTime );
		productManager.getSettings().set( ProductManager.NEXT_CHECK_TIME, priorNextCheckTime );

		productManager.setCheckOption( ProductManager.CheckOption.INTERVAL );
		productManager.getSettings().set( ProductManager.INTERVAL_UNIT, checkInterval.name().toLowerCase() );

		// when
		productManager.scheduleUpdateCheck( false );

		// then
		assertThat( productManager.getNextUpdateCheck() ).isCloseTo( expectedNextCheckTime, within( TimeUnit.SECONDS.toMillis( TOLERANCE ) ) );
	}

	private static Stream<Arguments> provideCheckIntervalOptions() {
		long now = System.currentTimeMillis();
		return Stream.of(
			Arguments.of( ProductManager.CheckInterval.HOUR, null, null, now ),
			Arguments.of( ProductManager.CheckInterval.DAY, null, null, now ),
			Arguments.of( ProductManager.CheckInterval.WEEK, null, null, now ),
			Arguments.of( ProductManager.CheckInterval.MONTH, null, null, now ),
			Arguments.of( ProductManager.CheckInterval.HOUR, now, null, now + TimeUnit.HOURS.toMillis( 1 ) ),
			Arguments.of( ProductManager.CheckInterval.DAY, now, null, now + TimeUnit.DAYS.toMillis( 1 ) ),
			Arguments.of( ProductManager.CheckInterval.WEEK, now, null, now + TimeUnit.DAYS.toMillis( 7 ) ),
			Arguments.of( ProductManager.CheckInterval.MONTH, now, null, now + TimeUnit.DAYS.toMillis( 30 ) )
		);
	}

	@ParameterizedTest
	@MethodSource( "provideCheckScheduleOptions" )
	void scheduleUpdateCheckWithSchedule( ProductManager.CheckWhen checkWhen, Integer checkHour, Long priorLastCheckTime, Long priorNextCheckTime, Long expectedNextCheckTime ) {
		// given
		assertThat( getProgram().isProgramUpdated() ).isFalse();
		assertThat( getProgram().isUpdateInProgress() ).isFalse();

		// Momentarily set the check option to manual to avoid bad interaction
		productManager.setCheckOption( ProductManager.CheckOption.MANUAL );
		productManager.getUpdateCheckSettings().set( ProductManager.LAST_CHECK_TIME, priorLastCheckTime );
		productManager.getUpdateCheckSettings().set( ProductManager.NEXT_CHECK_TIME, priorNextCheckTime );

		productManager.setCheckOption( ProductManager.CheckOption.SCHEDULE );
		productManager.getUpdateCheckSettings().set( ProductManager.SCHEDULE_WHEN, checkWhen );
		productManager.getUpdateCheckSettings().set( ProductManager.SCHEDULE_HOUR, checkHour );

		// when
		productManager.scheduleUpdateCheck( false );

		// then
		assertThat( productManager.getNextUpdateCheck() ).isCloseTo( expectedNextCheckTime, within( TimeUnit.SECONDS.toMillis( TOLERANCE ) ) );
	}

	private static Stream<Arguments> provideCheckScheduleOptions() {
		int scheduleHour = 5;
		long currentTimeUtc = System.currentTimeMillis();
		int offset = TimeZone.getDefault().getRawOffset();

		// Days of the week
		List<Arguments> arguments = new ArrayList<>();
		for( ProductManager.CheckWhen scheduleWhen : ProductManager.CheckWhen.values() ) {
			// Start with an instant in UTC
			Instant instant = Instant.ofEpochMilli( currentTimeUtc );

			// Get the local date-time according to the user
			ZoneId timeZoneId = ZoneId.systemDefault();
			LocalDateTime localDateTime = instant.atZone( timeZoneId ).toLocalDateTime();

			// Get the day of week from 1 (Monday) to 7 (Sunday)
			int nowDayOfWeek = localDateTime.getDayOfWeek().getValue();

			// Calculate the day offset to the next check day
			int dayOffset;
			if( scheduleWhen == ProductManager.CheckWhen.DAILY ) {
				dayOffset = 1;
			} else {
				dayOffset = scheduleWhen.ordinal() - nowDayOfWeek;
			}
			if( dayOffset < 1 ) dayOffset += 7;

			// Calculate the next update check.
			LocalDateTime nextCheck = localDateTime.plusDays( dayOffset ).withHour( scheduleHour ).withMinute( 0 ).withSecond( 0 ).withNano( 0 );

			// Convert the local date-time to an instant
			Instant nextCheckInstant = nextCheck.atZone( timeZoneId ).toInstant();

			arguments.add( Arguments.of( scheduleWhen, scheduleHour, null, null, nextCheckInstant.toEpochMilli() ) );
		}

		return arguments.stream();
	}

	@ParameterizedTest
	@MethodSource( "provideGetNextIntervalDelay" )
	void getNextIntervalDelay( Long lastCheckTime, long currentTimestamp, ProductManager.CheckInterval interval, long expectedDelay ) {
		assertThat( ProductManager.getNextIntervalDelay( lastCheckTime, currentTimestamp, interval ) ).isEqualTo( expectedDelay );
	}

	private static Stream<Arguments> provideGetNextIntervalDelay() {
		long lastCheckTime = 1732424400000L;
		long immediately = 0L;

		return Stream.of(
			Arguments.of( null, lastCheckTime, ProductManager.CheckInterval.HOUR, immediately ),

			Arguments.of( lastCheckTime, lastCheckTime + TimeUnit.MINUTES.toMillis( 55 ), ProductManager.CheckInterval.HOUR, TimeUnit.MINUTES.toMillis( 5 ) ),
			Arguments.of( lastCheckTime, lastCheckTime + TimeUnit.MINUTES.toMillis( 65 ), ProductManager.CheckInterval.HOUR, TimeUnit.MINUTES.toMillis( -5 ) ),

			Arguments.of( lastCheckTime, lastCheckTime + TimeUnit.HOURS.toMillis( 23 ), ProductManager.CheckInterval.DAY, TimeUnit.HOURS.toMillis( 1 ) ),
			Arguments.of( lastCheckTime, lastCheckTime + TimeUnit.HOURS.toMillis( 25 ), ProductManager.CheckInterval.DAY, TimeUnit.HOURS.toMillis( -1 ) ),

			Arguments.of( lastCheckTime, lastCheckTime + TimeUnit.DAYS.toMillis( 6 ), ProductManager.CheckInterval.WEEK, TimeUnit.DAYS.toMillis( 1 ) ),
			Arguments.of( lastCheckTime, lastCheckTime + TimeUnit.DAYS.toMillis( 8 ), ProductManager.CheckInterval.WEEK, TimeUnit.DAYS.toMillis( -1 ) ),

			Arguments.of( lastCheckTime, lastCheckTime + TimeUnit.DAYS.toMillis( 29 ), ProductManager.CheckInterval.MONTH, TimeUnit.DAYS.toMillis( 1 ) ),
			Arguments.of( lastCheckTime, lastCheckTime + TimeUnit.DAYS.toMillis( 31 ), ProductManager.CheckInterval.MONTH, TimeUnit.DAYS.toMillis( -1 ) )
		);
	}

	@ParameterizedTest
	@MethodSource( "provideGetNextScheduleDelay" )
	void getNextScheduleDelay( long currentTimestamp, ProductManager.CheckWhen when, int hour, long expectedDelay ) {
		assertThat( ProductManager.getNextScheduleDelay( currentTimestamp, when, hour ) ).isEqualTo( expectedDelay );
	}

	private static Stream<Arguments> provideGetNextScheduleDelay() {
		int offset = TimeZone.getDefault().getRawOffset();

		// This represents Sunday, November 24, 2024, 5:00:00 AM UTC
		long now = 1732424400000L;

		return Stream.of(
			Arguments.of( now, ProductManager.CheckWhen.DAILY, 5, TimeUnit.DAYS.toMillis( 0 ) - offset ),
			Arguments.of( now, ProductManager.CheckWhen.SUNDAY, 5, TimeUnit.DAYS.toMillis( 0 ) - offset ),
			Arguments.of( now, ProductManager.CheckWhen.MONDAY, 5, TimeUnit.DAYS.toMillis( 1 ) - offset ),
			Arguments.of( now, ProductManager.CheckWhen.TUESDAY, 5, TimeUnit.DAYS.toMillis( 2 ) - offset ),
			Arguments.of( now, ProductManager.CheckWhen.WEDNESDAY, 5, TimeUnit.DAYS.toMillis( 3 ) - offset ),
			Arguments.of( now, ProductManager.CheckWhen.THURSDAY, 5, TimeUnit.DAYS.toMillis( 4 ) - offset ),
			Arguments.of( now, ProductManager.CheckWhen.FRIDAY, 5, TimeUnit.DAYS.toMillis( 5 ) - offset ),
			Arguments.of( now, ProductManager.CheckWhen.SATURDAY, 5, TimeUnit.DAYS.toMillis( 6 ) - offset )
		);
	}

}
