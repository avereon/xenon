package com.avereon.xenon;

import com.avereon.settings.Settings;
import com.avereon.util.Log;
import com.avereon.xenon.asset.Asset;
import com.avereon.xenon.asset.OpenAssetRequest;
import com.avereon.xenon.workpane.Tool;
import com.avereon.xenon.workpane.ToolException;
import javafx.application.Platform;

import java.net.URI;
import java.util.Collections;
import java.util.Set;

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
public abstract class ProgramTool extends Tool {

	private static final System.Logger log = Log.get();

	private ProgramProduct product;

	private String uid;

	public ProgramTool( ProgramProduct product, Asset asset ) {
		super( asset );
		this.product = product;
		setTitle( getAsset().getName() );
		setCloseGraphic( product.getProgram().getIconLibrary().getIcon( "workarea-close" ) );
	}

	public ProgramProduct getProduct() {
		return product;
	}

	public Program getProgram() {
		return product.getProgram();
	}

	public Set<URI> getAssetDependencies() {
		return Collections.unmodifiableSet( Collections.emptySet() );
	}

	public Settings getSettings() {
		return getProgram().getSettingsManager().getSettings( ProgramSettings.TOOL, getUid() );
	}

	public String getUid() {
		return uid;
	}

	public void setUid( String uid ) {
		this.uid = uid;
	}

	@Override
	public void close() {
		Set<Tool> tools = getProgram().getWorkspaceManager().getAssetTools( getAsset() );
		if( !tools.contains( this ) ) return;

		Platform.runLater( () -> {
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
	protected void open( OpenAssetRequest request ) {}

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

	/**
	 * Called when the asset is ready to be used by the tool.
	 */
	// FIXME This needs to be split into two methods
	// One that is called one time for the tool when the asset is ready
	// Another for when the asset is opened again with, possibly, different parameters
	@Deprecated
	public final void callAssetReady( OpenAssetRequest request ) {
		Platform.runLater( () -> open( request ) );
	}

}
