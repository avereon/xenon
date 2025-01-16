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

		// These cause events to be fired that cause scheduleUpdateCheck to be called
		productManager.getSettings().set( ProductManager.LAST_CHECK_TIME, null );
		productManager.getSettings().set( ProductManager.NEXT_CHECK_TIME, null );
		productManager.setCheckOption( ProductManager.CheckOption.MANUAL );

		// when
		productManager.scheduleUpdateCheck( false );

		// then
		assertThat( productManager.getNextUpdateCheck() ).isEqualTo( null );
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
		getProgram().getSettings().set( ProductManager.INTERVAL_UNIT, checkInterval.name().toLowerCase() );

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
		productManager.getSettings().set( ProductManager.LAST_CHECK_TIME, priorLastCheckTime );
		productManager.getSettings().set( ProductManager.NEXT_CHECK_TIME, priorNextCheckTime );

		productManager.setCheckOption( ProductManager.CheckOption.SCHEDULE );
		getProgram().getSettings().set( ProductManager.SCHEDULE_WHEN, checkWhen.name().toLowerCase() );
		getProgram().getSettings().set( ProductManager.SCHEDULE_HOUR, checkHour );

		// when
		productManager.scheduleUpdateCheck( false );

		// then
		assertThat( productManager.getNextUpdateCheck() ).isCloseTo( expectedNextCheckTime, within( TimeUnit.SECONDS.toMillis( TOLERANCE ) ) );
	}

	private static Stream<Arguments> provideCheckScheduleOptions() {
		int hourOfDay = 5;
		long now = System.currentTimeMillis();
		List<Arguments> arguments = new ArrayList<>();

		// Days of the week
		for( ProductManager.CheckWhen checkWhen : ProductManager.CheckWhen.values() ) {
			Calendar calendar = Calendar.getInstance( TimeZone.getTimeZone( "UTC" ) );
			calendar.setTimeInMillis( now );
			// Sunday = 1, Monday = 2, ..., Saturday = 7
			int nowDayOfWeek = calendar.get( Calendar.DAY_OF_WEEK );

			// Calculate the day offset to the next check day
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

			arguments.add( Arguments.of( checkWhen, hourOfDay, null, null, calendar.getTimeInMillis() ) );
		}

		return arguments.stream();
	}

	// 1732424400000L 1732510800000L difference was 86400000L (1 day)
	// 1732424400000L 1733029200000L difference was 604800000L (7 days)

	@Test
	void getNextIntervalDelay() {
		long mock_now = 581825594;
		long immediately = 0L;

		assertThat( ProductManager.getNextIntervalDelay( mock_now, ProductManager.CheckInterval.HOUR, null ) ).isEqualTo( immediately );
		assertThat( ProductManager.getNextIntervalDelay( mock_now, ProductManager.CheckInterval.HOUR, mock_now ) ).isEqualTo( 3600000L );

		// NEXT Add some parameterized values
	}

}
