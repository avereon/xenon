package com.avereon.xenon;

import com.avereon.event.EventHandler;
import com.avereon.product.ProductBundle;
import com.avereon.settings.Settings;
import com.avereon.skill.Identity;
import com.avereon.skill.WritableIdentity;
import com.avereon.util.Log;
import com.avereon.xenon.asset.Asset;
import com.avereon.xenon.asset.AssetEvent;
import com.avereon.xenon.asset.OpenAssetRequest;
import com.avereon.xenon.task.Task;
import com.avereon.xenon.workpane.Tool;
import com.avereon.xenon.workpane.ToolEvent;
import com.avereon.xenon.workpane.ToolException;
import com.avereon.xenon.workspace.Workspace;
import com.avereon.zerra.javafx.Fx;

import java.net.URI;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * The ProgramTool is a {@link Tool} with added functionality for use with the
 * {@link Program}.
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
public abstract class ProgramTool extends Tool implements WritableIdentity {

	public static final int ASSET_READY_TIMEOUT = 10;

	public static final int TOOL_READY_TIMEOUT = 2;

	private static final System.Logger log = Log.get();

	private final ProgramProduct product;

	private boolean isReady;

	public ProgramTool( ProgramProduct product, Asset asset ) {
		super( asset );
		this.product = product;
		setTitle( asset.getName() );
		setGraphic( product.getProgram().getIconLibrary().getIcon( asset.getIcon(), "broken" ) );
		setCloseGraphic( product.getProgram().getIconLibrary().getIcon( "workarea-close" ) );
	}

	public final ProgramProduct getProduct() {
		return product;
	}

	public final Program getProgram() {
		return product.getProgram();
	}

	public void setIcon( String icon ) {
		setGraphic( getProgram().getIconLibrary().getIcon( icon ) );
	}

	public Set<URI> getAssetDependencies() {
		return Collections.unmodifiableSet( Collections.emptySet() );
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
				getProgram().getAssetManager().close( getAsset() );
			} else {
				super.close();
			}
		} );
	}

	/**
	 * A convenience method to get the product resource bundle from the tool.
	 *
	 * @return The product resource bundle
	 */
	protected ProductBundle rb() {
		return getProduct().rb();
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

	protected void runTask( Runnable runnable ) {
		getProgram().getTaskManager().submit( Task.of( "", runnable ) );
	}

	protected void pushToolActions( String... actions ) {
		getProgram().getWorkspaceManager().getActiveWorkspace().pushToolbarActions( actions );
	}

	protected void pullToolActions() {
		getProgram().getWorkspaceManager().getActiveWorkspace().pullToolbarActions();
	}

	protected ProgramTool pushAction( String key, Action action ) {
		getProgram().getActionLibrary().getAction( key ).pushAction( action );
		return this;
	}

	protected ProgramTool pullAction( String key, Action action ) {
		getProgram().getActionLibrary().getAction( key ).pullAction( action );
		return this;
	}

	protected Workspace getWorkspace() {
		return getProgram().getWorkspaceManager().findWorkspace( this );
	}

	protected void addStylesheet( String stylesheet ) {
		getStylesheets().add( Objects.requireNonNull( product.getClassLoader().getResource( stylesheet ) ).toExternalForm() );
	}

	void waitForReady( OpenAssetRequest request ) {
		Task<Void> toolLatch = getProgram().getTaskManager().submit( new ToolAddedLatch( this ) );
		Task<Void> assetLatch = getProgram().getTaskManager().submit( new AssetLoadedLatch( getAsset() ) );

		try {
			toolLatch.get();
			assetLatch.get();
			Fx.run( () -> {
				try {
					isReady = true;
					ready( request );
					open( request );
				} catch( ToolException exception ) {
					log.log( Log.ERROR, exception );
				}
			} );
		} catch( Exception exception ) {
			log.log( Log.ERROR, exception );
		}
	}

	private static class ToolAddedLatch extends Task<Void> {

		private final CountDownLatch latch = new CountDownLatch( 1 );

		private final ProgramTool tool;

		public ToolAddedLatch( ProgramTool tool ) {
			this.tool = tool;
		}

		@Override
		public Void call() throws Exception {
			javafx.event.EventHandler<ToolEvent> h = e -> latch.countDown();
			tool.addEventFilter( ToolEvent.ADDED, h );
			try {
				if( tool.getToolView() == null ) {
					latch.await( TOOL_READY_TIMEOUT, TimeUnit.SECONDS );
					if( latch.getCount() > 0 ) log.log( Log.WARN, "Timeout waiting for tool to be allocated: " + tool );
				}
			} finally {
				tool.removeEventFilter( ToolEvent.ADDED, h );
			}
			return null;
		}

	}

	private static class AssetLoadedLatch extends Task<Void> {

		private final CountDownLatch latch = new CountDownLatch( 1 );

		private final Asset asset;

		public AssetLoadedLatch( Asset asset ) {
			this.asset = asset;
		}

		@Override
		public Void call() throws Exception {
			EventHandler<AssetEvent> h = e -> latch.countDown();
			asset.register( AssetEvent.LOADED, h );
			try {
				if( !asset.isLoaded() ) {
					latch.await( ASSET_READY_TIMEOUT, TimeUnit.SECONDS );
					if( latch.getCount() > 0 ) log.log( Log.WARN, "Timeout waiting for asset to load: " + asset );
				}
			} finally {
				asset.unregister( AssetEvent.LOADED, h );
			}
			return null;
		}

	}

}
