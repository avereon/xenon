package com.avereon.xenon.product;

import com.avereon.xenon.ProgramTestCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
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
		productManager = new ProductManager( getProgram() );

		// Ensures updates are not checked during tests
		productManager.setCheckOption( ProductManager.CheckOption.MANUAL );
		productManager.setFoundOption( ProductManager.FoundOption.STORE );

		productManager.start();
	}

	@Test
	void scheduleUpdateCheckWithManual() {
		// given
		assertThat( productManager ).isNotNull();
		assertThat( getProgram().isProgramUpdated() ).isFalse();
		assertThat( getProgram().isUpdateInProgress() ).isFalse();

		// These cause events to be fired that cause scheduleUpdateCheck to be called
		productManager.getSettings().set( ProductManager.LAST_CHECK_TIME, null );
		productManager.getSettings().set( ProductManager.NEXT_CHECK_TIME, null );
		productManager.setCheckOption( ProductManager.CheckOption.MANUAL );

		// when
		productManager.scheduleUpdateCheck( false );

		// then
		Long lastCheckTime = productManager.getLastUpdateCheck();
		Long nextCheckTime = productManager.getNextUpdateCheck();

		// There was not a last check time so it is null
		assertThat( lastCheckTime ).isEqualTo( null );

		// The next check time should be null
		assertThat( nextCheckTime ).isEqualTo( null );
	}

	@Test
	void scheduleUpdateCheckWithStartup() {
		long now = System.currentTimeMillis();
		// given
		assertThat( productManager ).isNotNull();
		assertThat( getProgram().isProgramUpdated() ).isFalse();
		assertThat( getProgram().isUpdateInProgress() ).isFalse();

		// These cause events to be fired that cause scheduleUpdateCheck to be called
		productManager.getSettings().set( ProductManager.LAST_CHECK_TIME, null );
		productManager.getSettings().set( ProductManager.NEXT_CHECK_TIME, null );
		productManager.setCheckOption( ProductManager.CheckOption.STARTUP );

		// when
		productManager.scheduleUpdateCheck( false );

		// then
		Long lastCheckTime = productManager.getLastUpdateCheck();
		Long nextCheckTime = productManager.getNextUpdateCheck();

		// There was not a last check time so it is null
//		assertThat( lastCheckTime ).isCloseTo( now, within( TimeUnit.SECONDS.toMillis( TOLERANCE ) ) );
		assertThat( lastCheckTime ).isEqualTo( null );

		// The next check time should be null
		assertThat( nextCheckTime ).isEqualTo( null );
	}

	@ParameterizedTest
	@MethodSource( "provideCheckIntervalOptions" )
	void scheduleUpdateCheckWithInterval( ProductManager.CheckInterval checkInterval, Long priorLastCheckTime, Long priorNextCheckTime, Long expectedLastCheckTime, Long expectedNextCheckTime ) {
		// given
		assertThat( productManager ).isNotNull();
		assertThat( getProgram().isProgramUpdated() ).isFalse();
		assertThat( getProgram().isUpdateInProgress() ).isFalse();

		// These cause events to be fired that cause scheduleUpdateCheck to be called
		productManager.getSettings().set( ProductManager.LAST_CHECK_TIME, priorLastCheckTime );
		productManager.getSettings().set( ProductManager.NEXT_CHECK_TIME, priorNextCheckTime );
		productManager.setCheckOption( ProductManager.CheckOption.INTERVAL );
		getProgram().getSettings().set( ProductManager.INTERVAL_UNIT, checkInterval.name().toLowerCase() );

		// when
		productManager.scheduleUpdateCheck( false );

		// then
		Long lastCheckTime = productManager.getLastUpdateCheck();
		Long nextCheckTime = productManager.getNextUpdateCheck();

		// There was not a last check time so it is null
		if( lastCheckTime != null ) {
			assertThat( lastCheckTime ).isCloseTo( expectedLastCheckTime, within( TimeUnit.SECONDS.toMillis( TOLERANCE ) ) );
		}

		// The next check time should be some time in the future
		assertThat( nextCheckTime ).isCloseTo( expectedNextCheckTime, within( TimeUnit.SECONDS.toMillis( TOLERANCE ) ) );
	}

	private static Stream<Arguments> provideCheckIntervalOptions() {
		long now = System.currentTimeMillis();
		return Stream.of(
			Arguments.of( ProductManager.CheckInterval.HOUR, null, null, 0L, now ),
			Arguments.of( ProductManager.CheckInterval.DAY, null, null, 0L, now ),
			Arguments.of( ProductManager.CheckInterval.WEEK, null, null, 0L, now ),
			Arguments.of( ProductManager.CheckInterval.MONTH, null, null, 0L, now ),
			Arguments.of( ProductManager.CheckInterval.HOUR, now, null, now, now + TimeUnit.HOURS.toMillis( 1 ) ),
			Arguments.of( ProductManager.CheckInterval.DAY, now, null, now, now + TimeUnit.DAYS.toMillis( 1 ) ),
			Arguments.of( ProductManager.CheckInterval.WEEK, now, null, now, now + TimeUnit.DAYS.toMillis( 7 ) ),
			Arguments.of( ProductManager.CheckInterval.MONTH, now, null, now, now + TimeUnit.DAYS.toMillis( 30 ) )
		);
	}

	@ParameterizedTest
	@MethodSource( "provideCheckScheduleOptions" )
	void scheduleUpdateCheckWithSchedule(
		ProductManager.CheckWhen checkWhen,
		Integer checkHour,
		Long priorLastCheckTime,
		Long priorNextCheckTime,
		Long expectedLastCheckTime,
		Long expectedNextCheckTime
	) {
		// given
		assertThat( productManager ).isNotNull();
		assertThat( getProgram().isProgramUpdated() ).isFalse();
		assertThat( getProgram().isUpdateInProgress() ).isFalse();

		// These cause events to be fired that cause scheduleUpdateCheck to be called
		productManager.getSettings().set( ProductManager.LAST_CHECK_TIME, priorLastCheckTime );
		productManager.getSettings().set( ProductManager.NEXT_CHECK_TIME, priorNextCheckTime );
		productManager.setCheckOption( ProductManager.CheckOption.SCHEDULE );
		getProgram().getSettings().set( ProductManager.SCHEDULE_WHEN, checkWhen.name().toLowerCase() );
		getProgram().getSettings().set( ProductManager.SCHEDULE_HOUR, checkHour );

		// when
		productManager.scheduleUpdateCheck( false );

		// then
		Long lastCheckTime = productManager.getLastUpdateCheck();
		Long nextCheckTime = productManager.getNextUpdateCheck();

		// The last check time may be null or the expected value
		if( lastCheckTime != null ) {
			assertThat( lastCheckTime ).isCloseTo( expectedLastCheckTime, within( TimeUnit.SECONDS.toMillis( TOLERANCE ) ) );
		}

		// The next check time should be some time in the future
		assertThat( nextCheckTime ).isCloseTo( expectedNextCheckTime, within( TimeUnit.SECONDS.toMillis( TOLERANCE ) ) );
	}

	private static Stream<Arguments> provideCheckScheduleOptions() {
		int hourOfDay = 5;
		long now = System.currentTimeMillis();
		List<Arguments> arguments = new ArrayList<>();

		// Days of the week
		for( ProductManager.CheckWhen checkWhen : ProductManager.CheckWhen.values() ) {
			Calendar calendar = Calendar.getInstance();
			calendar.setTimeInMillis( now );
			// Sunday = 1, Monday = 2, ..., Saturday = 7
			int nowDayOfWeek = calendar.get( Calendar.DAY_OF_WEEK );

			int dayOffset;
			if( checkWhen == ProductManager.CheckWhen.DAILY ) {
				dayOffset = 1;
			} else {
				dayOffset = checkWhen.ordinal() - nowDayOfWeek;
			}

			if( dayOffset < 1 ) dayOffset += 7;

			calendar.add( Calendar.DAY_OF_MONTH, dayOffset );
			calendar.set( Calendar.HOUR_OF_DAY, hourOfDay );
			calendar.set( Calendar.MINUTE, 0 );
			calendar.set( Calendar.SECOND, 0 );
			calendar.set( Calendar.MILLISECOND, 0 );

			arguments.add( Arguments.of( checkWhen, hourOfDay, null, null, null, calendar.getTime().getTime() ) );
		}

		return arguments.stream();
	}

}
