package com.avereon.xenon.update;

import com.avereon.product.ProductCard;
import com.avereon.util.LogUtil;
import com.avereon.xenon.Program;
import com.avereon.xenon.task.Task;
import org.slf4j.Logger;

import java.lang.invoke.MethodHandles;
import java.util.Collection;
import java.util.Set;

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

	/**
	 * Starts a new task to apply the selected updates.
	 *
	 * @param updates The updates to apply.
	 */
	@Override
	public Task<Collection<ProductUpdate>> applySelectedUpdates( Set<ProductCard> updates ) {
		return applySelectedUpdates( updates, false );
	}

	public Task<Collection<ProductUpdate>> applySelectedUpdates( Set<ProductCard> updates, boolean interactive ) {
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
