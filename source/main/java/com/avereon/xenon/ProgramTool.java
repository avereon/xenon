package com.avereon.xenon;

import com.avereon.product.ProductBundle;
import com.avereon.settings.Settings;
import com.avereon.skill.Identity;
import com.avereon.skill.WritableIdentity;
import com.avereon.util.Log;
import com.avereon.xenon.asset.Asset;
import com.avereon.xenon.asset.OpenAssetRequest;
import com.avereon.xenon.workpane.Tool;
import com.avereon.xenon.workpane.ToolException;
import com.avereon.xenon.workspace.Workspace;
import com.avereon.zerra.javafx.Fx;
import javafx.scene.Node;

import java.net.URI;
import java.util.Collections;
import java.util.Objects;
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
public abstract class ProgramTool extends Tool implements WritableIdentity {

	private static final System.Logger log = Log.get();

	private final ProgramProduct product;

	public ProgramTool( ProgramProduct product, Asset asset ) {
		super( asset );
		this.product = product;
		setTitle( asset.getName() );
		setGraphic( product.getProgram().getIconLibrary().getIcon( asset.getIcon() ) );
		setCloseGraphic( product.getProgram().getIconLibrary().getIcon( "workarea-close" ) );
	}

	public ProgramProduct getProduct() {
		return product;
	}

	public Program getProgram() {
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
	 * The tool and asset are ready.
	 */
	@SuppressWarnings( "RedundantThrows" )
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
	@SuppressWarnings( "RedundantThrows" )
	protected void open( OpenAssetRequest request ) throws ToolException {}

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
		// Don't start with the parent, start with the workpane
		Node node = getWorkpane();

		Workspace workspace = null;
		while( node != null && workspace == null ) {
			workspace = (Workspace)node.getProperties().get( Workspace.WORKSPACE_PROPERTY_KEY);
			node = node.getParent();
		}

		return workspace;
	}

	protected void addStylesheet( String stylesheet ) {
		getStylesheets().add( Objects.requireNonNull( product.getClassLoader().getResource( stylesheet ) ).toExternalForm() );
	}

}
