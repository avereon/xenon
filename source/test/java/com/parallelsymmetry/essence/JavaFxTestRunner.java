package com.parallelsymmetry.essence;

import javafx.application.Platform;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;

import java.util.concurrent.CountDownLatch;

/**
 * This runner can be used to run JUnit tests on the JavaFx thread. This class
 * can be used as a parameter to the {@link RunWith} annotation. Example:
 * <p>
 * <pre>
 * <code>&#64;RunWith( JavaFxTestRunner.class )
 * public class SomeUnitTest {
 *   &#64;Test
 *   public void testSomeMethod() {
 *    //...
 *   }
 * }</code>
 * </pre>
 */
public class JavaFxTestRunner extends BlockJUnit4ClassRunner {

	/**
	 * Creates a test runner, that initializes the JavaFx runtime.
	 *
	 * @param clazz The class under test.
	 * @throws InitializationError if the test class is malformed.
	 */
	public JavaFxTestRunner( final Class<?> clazz ) throws InitializationError {
		super( clazz );
		try {
			setupJavaFX();
		} catch( final InterruptedException e ) {
			throw new InitializationError( "Could not initialize the JavaFx platform." );
		}
	}

	private static void setupJavaFX() throws InterruptedException {
		final CountDownLatch latch = new CountDownLatch( 1 );
		Platform.startup( latch::countDown );
		latch.await();
	}

}
