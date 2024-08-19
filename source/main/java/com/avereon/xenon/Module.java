package com.avereon.xenon;

import com.avereon.product.*;
import com.avereon.settings.Settings;
import com.avereon.xenon.asset.AssetType;
import com.avereon.xenon.product.ProductManager;
import com.avereon.xenon.tool.settings.SettingsPage;
import com.avereon.zarra.image.VectorImage;
import lombok.CustomLog;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

/**
 * The {@link Module} class provides the basic interface and implementation of
 * a Module. Modules are the way to provide functionality in the {@link Program}.
 * Otherwise, the {@link Program} is nothing more than a framework.
 * <p/>
 * Subclasses should use the {@link #register()}, {@link #startup()},
 * {@link #shutdown()} and {@link #unregister()} lifecycle methods to interact
 * with the program.
 * <p/>
 * The Module also implements {@link ProgramProduct} which provides access to
 * the program, the Module class loader and the Module resource bundles.
 */
@SuppressWarnings( "UnusedReturnValue" )
@CustomLog
public abstract class Module implements XenonProgramProduct, Comparable<Module> {

	/**
	 * Module lifecycle status values.
	 */
	public enum Status {
		REGISTERED,
		STARTED,
		STOPPED,
		UNREGISTERED
	}

	private static final String DEFAULT_SETTINGS = "settings/default.properties";

	private static final String SETTINGS_PAGES = "settings/pages.xml";

	/**
	 * The module {@link Status}. This is used to track the lifecycle of the module.
	 */
	@Getter
	@Setter
	private Status status;

	/**
	 * The module {@link Program} instance.
	 */
	@Getter
	// Set by init()
	private Xenon program;

	/**
	 * The module parent product. This should only be called by the program
	 * {@link ProductManager}.
	 */
	@Getter
	@Setter
	private Product parent;

	/**
	 * The module {@link ProductCard}.
	 */
	@Getter
	private ProductCard card;

	private Map<String, SettingsPage> settingsPages;

	public Module() {
		card = ProductCard.card( this );
	}

	/**
	 * A convenience method to register an icon.
	 *
	 * @param id The icon id
	 * @param icon The icon to register
	 * @return the mod
	 */
	protected Module registerIcon( String id, VectorImage icon ) {
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
	protected Module unregisterIcon( String id, VectorImage icon ) {
		getProgram().getIconLibrary().unregister( id, icon );
		return this;
	}

	/**
	 * A convenience method to register an action.
	 *
	 * @param bundle The program product providing the resource bundle
	 * @param id The action id
	 * @return This module
	 */
	protected Module registerAction( Product bundle, String id ) {
		getProgram().getActionLibrary().register( bundle, id );
		return this;
	}

	/**
	 * A convenience method to unregister an action.
	 *
	 * @param id The action id
	 * @return This module
	 */
	protected Module unregisterAction( String id ) {
		getProgram().getActionLibrary().unregister( id );
		return this;
	}

	/**
	 * A convenience method to register an asset type.
	 *
	 * @param type The asset type to register
	 * @return This module
	 */
	protected Module registerAssetType( AssetType type ) {
		getProgram().getAssetManager().addAssetType( type );
		return this;
	}

	/**
	 * A convenience method to unregister an asset type.
	 *
	 * @param type The asset type to unregister
	 * @return This module
	 */
	protected Module unregisterAssetType( AssetType type ) {
		getProgram().getAssetManager().removeAssetType( type );
		return this;
	}

	/**
	 * A convenience method to register a tool.
	 *
	 * @param assetType The asset type associated with the tool
	 * @param metadata The tool registration
	 * @return This module
	 */
	protected Module registerTool( AssetType assetType, ToolRegistration metadata ) {
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
	protected ToolRegistration registerTool( XenonProgramProduct product, AssetType assetType, Class<? extends ProgramTool> toolClass ) {
		ToolRegistration registration = new ToolRegistration( product, toolClass );
		getProgram().getToolManager().registerTool( assetType, registration );
		return registration;
	}

	/**
	 * A convenience method to unregister a tool.
	 *
	 * @param assetType The asset type associated with the tool
	 * @param type The tool class
	 * @return This module
	 */
	protected Module unregisterTool( AssetType assetType, Class<? extends ProgramTool> type ) {
		getProgram().getToolManager().unregisterTool( assetType, type );
		return this;
	}

	/**
	 * A convenience method to register the module default settings.
	 *
	 * @return The module
	 * @throws IOException if an error occurs loading the settings
	 */
	protected Module loadDefaultSettings() throws IOException {
		getSettings().loadDefaultValues( this, DEFAULT_SETTINGS );
		return this;
	}

	protected Module registerSettingsPages() {
		settingsPages = getProgram().getSettingsManager().addSettingsPages( this, getSettings(), SETTINGS_PAGES );
		return this;
	}

	protected Module unregisterSettingsPages() {
		getProgram().getSettingsManager().removeSettingsPages( settingsPages );
		return this;
	}

	/**
	 * Called by the product manager to initialize the module. This method should
	 * not be called by other classes.
	 *
	 * @param program The program reference
	 * @param card The Mod product card
	 */
	public final void init( Xenon program, ProductCard card ) {
		if( this.program != null ) return;
		this.program = program;
		this.card = card;
		Rb.init( this );
	}

	/**
	 * Called by the program to register a module instance. This method is typically
	 * called before the program frame and workspaces are created and allows the
	 * module to register icons, actions, asset types, tools, etc. This method is
	 * also called as part of the module installation process before the
	 * {@link #startup} method is called.
	 */
	public void register() {}

	/**
	 * Called by the program to start a module instance. This method is typically
	 * called after the program frame and workspaces are created, but not
	 * necessarily visible, and allows the module to perform any work needed once the
	 * UI is generated. This method is also called as part of the module installation
	 * process after the {@link #register} method is called. This method is also
	 * called when a module is enabled from the product tool.
	 */
	public void startup() throws Exception {}

	/**
	 * Called by the program to shut down a module instance. This method is typically
	 * called before the program frame and workspaces are destroyed. This allows
	 * the module to perform any work needed before the UI is destroyed. This method
	 * is also called as part of the module uninstallation process before the
	 * {@link #unregister} method is called. This method is also called when a mod
	 * is disabled from the product tool.
	 */
	public void shutdown() throws Exception {}

	/**
	 * Called by the program to unregister a module instance. This method is
	 * typically called after the program frame and workspaces are destroyed and
	 * allows the module to unregister icons, actions, asset types, tools, etc.
	 * This method is also called as part of the module uninstallation process after
	 * the {@link #shutdown} method is called.
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
	public int compareTo( Module that ) {
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
