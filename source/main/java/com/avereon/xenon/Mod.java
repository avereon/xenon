package com.avereon.xenon;

import com.avereon.product.Product;
import com.avereon.product.ProductBundle;
import com.avereon.product.ProductCard;
import com.avereon.settings.Settings;
import com.avereon.util.Log;
import com.avereon.xenon.asset.AssetType;
import com.avereon.xenon.tool.settings.SettingsPage;
import com.avereon.zerra.image.VectorImage;

import java.io.IOException;
import java.lang.System.Logger;
import java.nio.file.Path;
import java.util.Map;

/**
 * The Mod class provides the basic interface and implementation of a Mod. Mods
 * are the way to provide functionality in the program. Otherwise the program is
 * nothing more than a framework.
 * <p/>
 * Subclasses should use the {@link #register()}, {@link #startup()},
 * {@link #shutdown()} and {@link #unregister()} lifecycle methods to interact
 * with the program.
 * <p/>
 * The Mod also implements ProgramProduct which provides access to the program,
 * the Mod class loader and the Mod resource bundles.
 */
public abstract class Mod implements ProgramProduct, Comparable<Mod> {

	private static final String DEFAULT_SETTINGS = "settings/default.properties";

	private static final String SETTINGS_PAGES = "settings/pages.xml";

	private static final Logger log = Log.get();

	private Program program;

	private Product parent;

	private ProductCard card;

	private ProductBundle resourceBundle;

	private Map<String, SettingsPage> settingsPages;

	public Mod() {
		card = new ProductCard().jsonCard( this );
	}

	@Override
	public Program getProgram() {
		return program;
	}

	@Override
	public ProductCard getCard() {
		return card;
	}

	@Override
	public Product getParent() {
		return parent;
	}

	public void setParent( Product parent ) {
		this.parent = parent;
	}

	@Override
	public ProductBundle rb() {
		if( resourceBundle == null ) resourceBundle = new ProductBundle( getProgram(), this );
		return resourceBundle;
	}

	/**
	 * A convenience method to register an icon.
	 *
	 * @param id The icon id
	 * @param icon The icon to register
	 * @return the mod
	 */
	protected Mod registerIcon( String id, VectorImage icon ) {
		getProgram().getIconLibrary().register( id, icon );
		return this;
	}

	/**
	 * A convenience method to unregister an icon.
	 *
	 * @param id The icon id
	 * @param icon The icon to unregister
	 * @return the mod
	 */
	protected Mod unregisterIcon( String id, VectorImage icon ) {
		getProgram().getIconLibrary().unregister( id, icon );
		return this;
	}

	/**
	 * A convenience method to register an action.
	 *
	 * @param bundle
	 * @param id
	 * @return
	 */
	protected Mod registerAction( ProductBundle bundle, String id ) {
		getProgram().getActionLibrary().register( bundle, id );
		return this;
	}

	/**
	 * A convenience method to unregister an action.
	 *
	 * @param id
	 * @return
	 */
	protected Mod unregisterAction( String id ) {
		//getProgram().getActionLibrary().unregister( id );
		return this;
	}

	/**
	 * A convenience method to register an asset type.
	 *
	 * @param type
	 * @return
	 */
	protected Mod registerAssetType( AssetType type ) {
		getProgram().getAssetManager().addAssetType( type );
		return this;
	}

	/**
	 * A convenience method to unregister an asset type.
	 *
	 * @param type
	 * @return
	 */
	protected Mod unregisterAssetType( AssetType type ) {
		getProgram().getAssetManager().removeAssetType( type );
		return this;
	}

	/**
	 * A convenience method to register a tool.
	 *
	 * @param assetType
	 * @param metadata
	 * @return
	 */
	protected Mod registerTool( AssetType assetType, ToolRegistration metadata ) {
		getProgram().getToolManager().registerTool( assetType, metadata );
		return this;
	}

	/**
	 * A convenience method to register a tool.
	 *
	 * @param product The program product providing the asset type and tool
	 * @param assetType The asset type associated with the tool
	 * @param toolClass The tool class
	 * @return The tool registration
	 */
	protected ToolRegistration registerTool( ProgramProduct product, AssetType assetType, Class<? extends ProgramTool> toolClass ) {
		ToolRegistration registration = new ToolRegistration( product, toolClass );
		getProgram().getToolManager().registerTool( assetType, registration );
		return registration;
	}

	/**
	 * A convenience method to unregister a tool.
	 *
	 * @param assetType
	 * @param type
	 * @return
	 */
	protected Mod unregisterTool( AssetType assetType, Class<? extends ProgramTool> type ) {
		getProgram().getToolManager().unregisterTool( assetType, type );
		return this;
	}

	protected Mod loadDefaultSettings() throws IOException {
		getSettings().loadDefaultValues( this, DEFAULT_SETTINGS );
		return this;
	}

	protected Mod registerSettingsPages() {
		settingsPages = getProgram().getSettingsManager().addSettingsPages( this, getSettings(), SETTINGS_PAGES );
		return this;
	}

	protected Mod unregisterSettingsPages() {
		getProgram().getSettingsManager().removeSettingsPages( settingsPages );
		return this;
	}

	/**
	 * Called by the product manager to initialize the mod. This method should not
	 * be called by other classes.
	 *
	 * @param program The program reference
	 * @param card The Mod product card
	 */
	public final void init( Program program, ProductCard card ) {
		if( this.program != null ) return;
		this.program = program;
		this.card = card;
	}

	/**
	 * Called by the program to register a mod instance. This method is typically
	 * called before the program frame and workspaces are created and allows the
	 * mod to register icons, actions, asset types, tools, etc. This method is
	 * also called as part of the mod install process before the {@link #startup}
	 * method is called.
	 */
	public void register() {}

	/**
	 * Called by the program to startup a mod instance. This method is typically
	 * called after the program frame and workspaces are created, but not
	 * necessarily visible, and allows the mod to perform any work needed once the
	 * UI is generated. This method is also called as part of the mod install
	 * process after the {@link #register} method is called. This method is also
	 * called when a mod is enabled from the product tool.
	 */
	public void startup() throws Exception {}

	/**
	 * Called by the program to shutdown a mod instance. This method is typically
	 * called before the program frame and workspaces are destroyed. This allows
	 * the mod to perform any work needed before the UI is destroyed. This method
	 * is also called as part of the mod uninstall process before the
	 * {@link #unregister} method is called. This method is also called when a mod
	 * is disabled from the product tool.
	 */
	public void shutdown() throws Exception {}

	/**
	 * Called by the program to unregister a mod instance. This method is
	 * typically called after the program frame and workspaces are destroyed and
	 * allows the mod to unregister icons, actions, asset types, tools, etc.
	 * This method is also called as part of the mod uninstall process after the
	 * {@link #shutdown} method is called.
	 */
	public void unregister() {}

	@Override
	public Settings getSettings() {
		return getProgram().getSettingsManager().getProductSettings( getCard() );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Path getDataFolder() {
		return getProgram().getDataFolder().resolve( getCard().getProductKey() );
	}

	/**
	 * This implementation only compares the product card artifact values.
	 */
	@Override
	public int compareTo( Mod that ) {
		return this.getCard().getArtifact().compareTo( that.getCard().getArtifact() );
	}

	/**
	 * This implementation only returns the product card name.
	 */
	@Override
	public String toString() {
		return getCard().getName();
	}

}
