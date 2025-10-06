package com.avereon.xenon;

import com.avereon.event.EventHandler;
import com.avereon.product.Rb;
import com.avereon.settings.Settings;
import com.avereon.skill.Identity;
import com.avereon.xenon.asset.Asset;
import com.avereon.xenon.asset.AssetEvent;
import com.avereon.xenon.asset.OpenAssetRequest;
import com.avereon.xenon.asset.exception.ResourceException;
import com.avereon.xenon.notice.Notice;
import com.avereon.xenon.task.Task;
import com.avereon.xenon.task.TaskChain;
import com.avereon.xenon.workpane.Tool;
import com.avereon.xenon.workpane.ToolEvent;
import com.avereon.xenon.workpane.ToolException;
import com.avereon.xenon.workpane.Workpane;
import com.avereon.xenon.workspace.Workspace;
import com.avereon.zerra.javafx.Fx;
import lombok.CustomLog;

import java.net.URI;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * The ProgramTool is a {@link Tool} with added functionality for use with the
 * {@link Xenon}.
 *
 * <h2>ProgramTool Lifecycle</h2>
 * There are several steps in the tool lifecycle. Some are called only once and
 * some are called multiple times:
 * <dl>
 *   <dt>Constructor</dt>
 *   <dd>The {@link Tool constructor} is called only once and should
 *   create all the UI components needed for the tool. However, the provided
 *   {@link Asset} may not be loaded and should not be used in the constructor
 *   other than to call {@code super(asset)}. After the constructor completes,
 *   the tool will added to the workarea, possibly before the asset is loaded.</dd>
 *
 *   <dt>{@link #ready} [FX thread]</dt>
 *   <dd>The {@link #ready} method is called one time
 *   when both the tool and asset are ready to be used. Specifically it is
 *   called when the tool has been added to the workarea and the asset data
 *   model has been populated. Asset data model event handlers should be
 *   registered in this step. The {@link OpenAssetRequest} parameter can be used
 *   to find other information about opening the asset, usually with the query
 *   parameters. Note, while it is safe to update the tool UI in this step,
 *   {@link #open} will be called immediately afterward, so it is usually more
 *   efficient just to update the UI there.</dd>
 *
 *   <dt>{@link #open} [FX thread]</dt>
 *   <dd>Called when the tool is opened. This method is not called before
 *   {@link #ready} has been called and may be called any number of times after
 *   {@link #ready} is called. The {@link OpenAssetRequest} parameter can be used
 *   to find other information about opening the asset, usually with the query
 *   parameters. It is recommended to update the tool UI state from the asset
 *   model in this step.</dd>
 *
 *   <dt>{@link #allocate} [FX thread]</dt>
 *   <dd>Called when the tool is added to the workarea. This step may be called
 *   before the asset is ready and before open is called, the state of the asset
 *   should not be assumed.</dd>
 *
 *   <dt>{@link #display} [FX thread]</dt>
 *   <dd>Called when the tool is displayed (made the visible tool in the view
 *   pane). This step may be called before the asset is ready and before open is
 *   called, the state of the asset should not be assumed. This method is often,
 *   but not always, followed by the activate step. This step is a good place to
 *   restart animation threads or other work that is paused when the tool is
 *   concealed.</dd>
 *
 *   <dt>{@link #activate} [FX thread]</dt>
 *   <dd>Called when the tool is activated. This step may be called before the
 *   asset is ready and before open is called, the state of the asset should not
 *   be assumed.</dd>
 *
 *   <dt>{@link #deactivate} [FX thread]</dt>
 *   <dd>Called when the tool is deactivated. This step may be called before the
 *   asset is ready and before open is called, the state of the asset should not
 *   be assumed.</dd>
 *
 *   <dt>{@link #conceal} [FX thread]</dt>
 *   <dd>Called when the tool is concealed. This step may be called before the
 *   asset is ready and before open is called, the state of the asset should not
 *   be assumed. This step is a good place to pause animation threads or other
 *   work that can or should be paused while the tool is not visible.</dd>
 *
 *   <dt>{@link #deallocate} [FX thread]</dt>
 *   <dd>Called when the tool is removed from the workarea. This step may be
 *   called before the asset is ready and before open is called, the state of
 *   the asset should not be assumed.</dd>
 * </dl>
 * <ul>
 *   <li>Constructor</li>
 * </ul>
 */
@CustomLog
public abstract class ProgramTool extends Tool {

	public static final int ASSET_READY_TIMEOUT = 10;

	public static final int TOOL_READY_TIMEOUT = 2;

	private final XenonProgramProduct product;

	private boolean isReady;

	private boolean setActiveWhenReady;

	private boolean setActiveWhenOpen;

	public ProgramTool( XenonProgramProduct product, Asset asset ) {
		super( asset );
		this.product = product;
		setTitle( asset.getName() );
		setGraphic( product.getProgram().getIconLibrary().getIcon( asset.getIcon(), "broken" ) );
		setCloseGraphic( product.getProgram().getIconLibrary().getIcon( "workarea-close" ) );
	}

	public final XenonProgramProduct getProduct() {
		return product;
	}

	public final Xenon getProgram() {
		return product.getProgram();
	}

	public void setIcon( String icon ) {
		setGraphic( getProgram().getIconLibrary().getIcon( icon ) );
	}

	public Set<URI> getAssetDependencies() {
		return Collections.emptySet();
	}

	public Settings getAssetSettings() {
		return getProgram().getSettingsManager().getAssetSettings( getAsset() );
	}

	public Settings getSettings() {
		return getProgram().getSettingsManager().getSettings( ProgramSettings.TOOL, getUid() );
	}

	public boolean changeCurrentAsset() {
		return true;
	}

	@Override
	public String getUid() {
		return getProperties().get( Identity.KEY ).toString();
	}

	@Override
	public void setUid( String id ) {
		getProperties().put( Identity.KEY, id );
	}

	@Override
	public void close() {
		Set<Tool> tools = getProgram().getWorkspaceManager().getAssetTools( getAsset() );
		if( !tools.contains( this ) ) return;

		Fx.run( () -> {
			if( getAsset().isNewOrModified() ) {
				if( getProgram().getWorkspaceManager().handleModifiedAssets( ProgramScope.TOOL, Set.of( getAsset() ) ) ) super.close();
			} else if( tools.size() == 1 ) {
				getProgram().getResourceManager().close( getAsset() );
			} else {
				super.close();
			}
		} );
	}

	/**
	 * Check if the tool is ready for use. Ready for use means that both the tool
	 * and it's associated asset are initialized and loaded.
	 *
	 * @return True if ready for use, false otherwise.
	 */
	protected boolean isReady() {
		return isReady;
	}

	/**
	 * The tool and asset are ready.
	 */
	// THREAD JavaFX Application Thread
	protected void ready( OpenAssetRequest request ) throws ToolException {}

	/**
	 * Called to open or reopen the tool. This is called at least once after
	 * {@link #ready} has been called but may be called more than once. It is
	 * called each time the asset handled by this tool is opened. If it is
	 * opened another time it may have different request parameters such as
	 * a different query string or fragment.
	 *
	 * @param request The request used to open the asset
	 */
	protected void open( OpenAssetRequest request ) throws ToolException {}

	@Override
	protected void activate() throws ToolException {
		if( changeCurrentAsset() ) pullMenus();
		if( changeCurrentAsset() ) pullTools();
		super.conceal();
	}

	protected void runTask( Runnable runnable ) {
		getProgram().getTaskManager().submit( Task.of( "", runnable ) );
	}

	protected void pushMenus( String descriptor ) {
		getProgram().getWorkspaceManager().getActiveWorkspace().pushMenuActions( descriptor );
	}

	protected void pullMenus() {
		getProgram().getWorkspaceManager().getActiveWorkspace().pullMenuActions();
	}

	protected void pushTools( String descriptor ) {
		getProgram().getWorkspaceManager().getActiveWorkspace().pushToolbarActions( descriptor );
	}

	protected void pullTools() {
		getProgram().getWorkspaceManager().getActiveWorkspace().pullToolbarActions();
	}

	protected ProgramTool pushAction( String key, ProgramAction action ) {
		getProgram().getActionLibrary().getAction( key ).pushAction( action );
		return this;
	}

	protected ProgramTool pullAction( String key, ProgramAction action ) {
		getProgram().getActionLibrary().getAction( key ).pullAction( action );
		return this;
	}

	protected Workspace getWorkspace() {
		return getProgram().getWorkspaceManager().findWorkspace( this );
	}

	protected void addStylesheet( String stylesheet ) {
		ClassLoader loader = product.getClass().getClassLoader();
		getStylesheets().add( Objects.requireNonNull( loader.getResource( stylesheet ) ).toExternalForm() );
	}

	public void setActiveWhenReady() {
		this.setActiveWhenReady = true;
	}

	public void setActiveWhenOpen() {
		this.setActiveWhenOpen = true;
	}

	static void waitForReady( OpenAssetRequest request, ProgramTool tool ) {
		TaskChain.of( "wait for ready", () -> {
			waitForTool( tool );
			return null;
		} ).link( () -> {
			waitForAsset( request.getAsset() );
			return null;
		} ).link( () -> {
			tool.callToolReady( request );
			return null;
		} ).run( tool.getProgram() );
	}

	private static void waitForTool( ProgramTool tool ) throws TimeoutException, InterruptedException {
		CountDownLatch latch = new CountDownLatch( 1 );
		javafx.event.EventHandler<ToolEvent> handler = e -> latch.countDown();

		try {
			tool.addEventFilter( ToolEvent.ADDED, handler );
			if( tool.getToolView() == null ) {
				boolean timeout = !latch.await( TOOL_READY_TIMEOUT, TimeUnit.SECONDS );
				//if( timeout ) log.atWarning().log( "Timeout waiting for tool to be allocated: %s", tool );
				if( timeout ) throw new TimeoutException( "Timeout waiting for tool to be created: " + tool );
			}
		} finally {
			tool.removeEventFilter( ToolEvent.ADDED, handler );
		}
	}

	private static void waitForAsset( Asset asset ) throws ResourceException, TimeoutException, InterruptedException {
		CountDownLatch latch = new CountDownLatch( 1 );
		EventHandler<AssetEvent> handler = e -> latch.countDown();
		asset.register( AssetEvent.LOADED, handler );
		try {
			if( asset.exists() && !asset.isLoaded() ) {
				boolean timeout = !latch.await( ASSET_READY_TIMEOUT, TimeUnit.SECONDS );
				if( timeout ) {
					//log.atWarning().log( "Timeout waiting for asset to load: %s", asset );
					throw new TimeoutException( "Timeout waiting for asset to load: " + asset );
				}
			}
		} finally {
			asset.unregister( AssetEvent.LOADED, handler );
		}
	}

	private void callToolReady( OpenAssetRequest request ) {
		// Set the isReady flag to true
		isReady = true;

		// Determine if the asset is missing
		// TODO This logic, and notice, about missing assets should be moved to the asset manager
		boolean assetMissing;
		try {
			assetMissing = !request.getAsset().isNew() && !request.getAsset().exists();
		} catch( ResourceException exception ) {
			assetMissing = true;
		}
		final boolean finalAssetMissing = assetMissing;

		final Workpane pane = getWorkpane();

		Fx.run( () -> {
			// Notify the user if the asset is missing
			if( finalAssetMissing ) {
				String title = Rb.text(RbKey.ASSET, "asset-missing" );
				String message = Rb.text( RbKey.ASSET, "asset-is-missing", request.getAsset().getSimpleName(), request.getAsset().getUri() );
				Notice notice = new Notice( title, message );
				getProgram().getNoticeManager().addNotice( notice );
			}

			// Set the tool active and call ready and open
			try {
				ready( request );
				// Fire tool ready event
				if( setActiveWhenReady ) doSetActiveWhenReady();
				fireEvent( new ProgramToolEvent( this, ProgramToolEvent.READY, pane, this ) );

				open( request );
				// Fire tool open event
				if( setActiveWhenOpen ) getWorkpane().setActiveTool( this );
				fireEvent( new ProgramToolEvent( this, ProgramToolEvent.TOOL_OPEN_REQUEST_FINISHED, pane, this ) );
			} catch( ToolException exception ) {
				log.atSevere().withCause( exception ).log();
			}
		} );
	}

	void doSetActiveWhenReady() {
		getWorkpane().setActiveTool( this );
		setActiveWhenReady = false;
	}

}
