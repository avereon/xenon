package com.xeomar.xenon;

import com.xeomar.product.ProductCard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.concurrent.ExecutionException;

public class ProgramUpdateManager extends UpdateManager {

	private static final Logger log = LoggerFactory.getLogger( ProgramUpdateManager.class );

	public ProgramUpdateManager( Program program ) {
		super( program );
	}

	// NEXT Override checkForUpdates
	@Override
	public void checkForUpdates() {
		checkForUpdates( false );
	}

	public void checkForUpdates( boolean interactive ) {
		// TODO getProgram().getExecutor().submit( new CheckForUpdates( interactive ) );
	}

	private final class CheckForUpdates extends ProgramTask<Void> {

		private boolean interactive;

		public CheckForUpdates( Program program, boolean interactive ) {
			super( program, program.getResourceBundle().getString( BundleKey.UPDATE, "task-updates-check" ) );
			this.interactive = interactive;
		}

		@Override
		public Void call() throws Exception {
			// Get the installed packs.
			Set<ProductCard> installedPacks = getProductCards();

			// Get the posted updates.
			Set<ProductCard> postedUpdates = null;
			try {
				postedUpdates = getPostedUpdates( interactive );
			} catch( ExecutionException exception ) {
				log.warn( exception.getClass().getName(), exception.getMessage() );
				log.trace( "Error getting posted updates", exception );
			}

			// Notify the user if updates are not available.
			boolean notAvailable = postedUpdates == null || postedUpdates.size() == 0;
			if( notAvailable ) {
				if( interactive ) {
					String message = getProgram().getResourceBundle().getString( BundleKey.UPDATE, "updates-not-available" );
					if( postedUpdates == null ) message = getProgram().getResourceBundle().getString( BundleKey.UPDATE, "exception-updates-source-cannot-connect" );
					// TODO getProgram().notify( getProgram().getResourceBundle().getString( BundleKey.UPDATE, "updates" ), message, JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE );
				}
				return null;
			}

			//			// Generate the update options.
			//			Map<String, UpdateOption> updateOptions = new HashMap<String, UpdateOption>( postedUpdates.size() );
			//			for( ProductCard pack : postedUpdates ) {
			//				updateOptions.put( pack.getProductKey(), new UpdateOption( pack ) );
			//			}
			//
			//			if( program.isRunning() ) EventQueue.invokeLater( new HandleFoundUpdates( installedPacks, updateOptions, interactive ) );

			return null;
		}

	}

}
