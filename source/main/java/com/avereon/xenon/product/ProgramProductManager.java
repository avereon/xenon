package com.avereon.xenon.product;

import com.avereon.product.ProductCard;
import com.avereon.util.LogUtil;
import com.avereon.xenon.Program;
import com.avereon.xenon.task.Task;
import org.slf4j.Logger;

import java.lang.invoke.MethodHandles;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

public class ProgramProductManager extends ProductManager {

	private static final Logger log = LogUtil.get( MethodHandles.lookup().lookupClass() );

	private Program program;

	public ProgramProductManager( Program program ) {
		super( program );
		this.program = program;
	}

	@Override
	public void checkForUpdates() {
		checkForUpdates( false );
	}

	public void checkForUpdates( boolean interactive ) {
		new ProductManagerLogic( program ).checkForUpdates( interactive );
	}

	public Task<Collection<ProductUpdate>> updateProducts( ProductCard update, boolean interactive ) {
		return updateProducts( new DownloadRequest( update ), interactive );
	}

	public Task<Collection<ProductUpdate>> updateProducts( DownloadRequest update, boolean interactive ) {
		return updateProducts( Set.of( update ), interactive );
	}

	/**
	 * Starts a new task to apply the selected updates.
	 *
	 * @param updates The updates to apply.
	 */
	@Override
	public Task<Collection<ProductUpdate>> updateProducts( Set<ProductCard> updates ) {
		return updateProducts( updates.stream().map( DownloadRequest::new ).collect( Collectors.toSet() ), false );
	}

	public Task<Collection<ProductUpdate>> updateProducts( Set<DownloadRequest> updates, boolean interactive ) {
		return new ProductManagerLogic( program ).stageAndApplyUpdates( updates, interactive );
	}

	/**
	 * Apply staged updates found at program start, if any.
	 */
	public void applyStagedUpdatesAtStart() {
		int stagedUpdateCount = getStagedUpdateCount();
		log.info( "Staged update count: " + stagedUpdateCount );
		if( !isEnabled() || stagedUpdateCount == 0 ) return;

		if( program.isUpdateInProgress() ) {
			program.setUpdateInProgress( false );
			clearStagedUpdates();
		} else {
			new ProductManagerLogic( program ).notifyUpdatesReadyToApply( false );
		}
	}

}
