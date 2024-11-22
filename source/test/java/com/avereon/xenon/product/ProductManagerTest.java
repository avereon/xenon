package com.avereon.xenon.product;

import com.avereon.xenon.ProgramTestCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

@ExtendWith( MockitoExtension.class )
class ProductManagerTest extends ProgramTestCase {

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

	@ParameterizedTest
	@MethodSource( "provideCheckIntervalOptions" )
	void scheduleUpdateCheck(
		ProductManager.CheckOption checkOption,
		ProductManager.CheckInterval checkInterval,
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
		productManager.setCheckOption( checkOption );
		getProgram().getSettings().set( ProductManager.INTERVAL_UNIT, checkInterval.name().toLowerCase() );

		// when
		productManager.scheduleUpdateCheck( false );

		// then
		Long lastCheckTime = productManager.getLastUpdateCheck();
		Long nextCheckTime = productManager.getNextUpdateCheck();

		// There was not a last check time so it is null
		assertThat( lastCheckTime ).isEqualTo( expectedLastCheckTime );

		// The next check time should be some time in the future
		assertThat( nextCheckTime ).isCloseTo( expectedNextCheckTime, within( TimeUnit.SECONDS.toMillis( 5 ) ) );
	}

	private static Stream<Arguments> provideCheckIntervalOptions() {
		long now = System.currentTimeMillis();
		return Stream.of(
			Arguments.of( ProductManager.CheckOption.INTERVAL, ProductManager.CheckInterval.HOUR, null, null, 0L, now ),
			Arguments.of( ProductManager.CheckOption.INTERVAL, ProductManager.CheckInterval.DAY, null, null, 0L, now ),
			Arguments.of( ProductManager.CheckOption.INTERVAL, ProductManager.CheckInterval.WEEK, null, null, 0L, now ),
			Arguments.of( ProductManager.CheckOption.INTERVAL, ProductManager.CheckInterval.MONTH, null, null, 0L, now ),
			Arguments.of( ProductManager.CheckOption.INTERVAL, ProductManager.CheckInterval.HOUR, now, null, now, now + TimeUnit.HOURS.toMillis( 1 ) ),
			Arguments.of( ProductManager.CheckOption.INTERVAL, ProductManager.CheckInterval.DAY, now, null, now, now + TimeUnit.DAYS.toMillis( 1 ) ),
			Arguments.of( ProductManager.CheckOption.INTERVAL, ProductManager.CheckInterval.WEEK, now, null, now, now + TimeUnit.DAYS.toMillis( 7 ) ),
			Arguments.of( ProductManager.CheckOption.INTERVAL, ProductManager.CheckInterval.MONTH, now, null, now, now + TimeUnit.DAYS.toMillis( 30 ) )
		);
	}

}
