package com.xeomar.xenon;

import com.xeomar.product.Product;
import com.xeomar.product.ProductCard;
import com.xeomar.settings.Settings;
import com.xeomar.util.Configurable;
import com.xeomar.util.Controllable;
import com.xeomar.xenon.update.ProductCatalog;
import com.xeomar.xenon.update.ProductUpdate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.TimeUnit;

/**
 * The update manager handles discovery, staging and applying product updates.
 * <p>
 * Discovery involves checking for updates over the network (usually over the Internet) and comparing the release information of installed packs with the release information of the discovered packs. If the discovered pack is determined to
 * be newer than the installed pack it is considered an update.
 * <p>
 * Staging involves downloading new pack data and preparing it to be applied by the update application.
 * <p>
 * Applying involves configuring and executing a separate update process to apply the staged updates. This requires the calling process to terminate to allow the update process to change required files.
 */
public class UpdateManager implements Controllable<UpdateManager>, Configurable {

	public enum CheckOption {
		MANUAL,
		STARTUP,
		INTERVAL,
		SCHEDULE
	}

	public enum CheckInterval {
		MONTH,
		WEEK,
		DAY,
		HOUR
	}

	public enum CheckWhen {
		DAILY,
		SUNDAY,
		MONDAY,
		TUESDAY,
		WEDNESDAY,
		THURSDAY,
		FRIDAY,
		SATURDAY
	}

	public enum FoundOption {
		SELECT,
		STORE,
		STAGE
	}

	public enum ApplyOption {
		VERIFY,
		IGNORE,
		RESTART
	}

	private static final Logger log = LoggerFactory.getLogger( UpdateManager.class );

	public static final String DEFAULT_CATALOG_FILE_NAME = "catalog.xml";

	public static final String DEFAULT_PRODUCT_FILE_NAME = "product.xml";

	public static final String PRODUCT_DESCRIPTOR_PATH = "META-INF/" + DEFAULT_PRODUCT_FILE_NAME;

	public static final String UPDATER_JAR_NAME = "updater.jar";

	public static final String UPDATER_LOG_NAME = "updater.log";

	public static final String UPDATE_FOLDER_NAME = "updates";

	private static final String CHECK = "check";

	private static final String FOUND = "found";

	private static final String APPLY = "apply";

	private static final String CATALOGS_SETTINGS_KEY = "catalogs";

	private static final String REMOVES_SETTINGS_KEY = "removes";

	private static final String UPDATES_SETTINGS_KEY = "updates";

	private static final String PRODUCT_ENABLED_KEY = "enabled";

	private static final int POSTED_UPDATE_CACHE_TIMEOUT = 60000;

	private static final int MILLIS_IN_HOUR = 3600000;

	private static final int NO_CHECK = -1;

	private Program program;

	private Settings settings;

	private Set<ProductCatalog> catalogs;

	private Map<String, Module> modules;

	private File homeModuleFolder;

	private File userProductFolder;

	private CheckOption checkOption;

	private FoundOption foundOption;

	private ApplyOption applyOption;

	private File updater;

	private Map<String, Product> products;

	private Map<String, ProductCard> productCards;

	private Map<String, ProductUpdate> updates;

	private Map<String, ProductState> productStates;

	private Set<String> includedProducts;

	private Set<ProductCard> postedUpdateCache;

	private long postedUpdateCacheTime;

	private Timer timer;

	private UpdateCheckTask task;

	private Set<UpdateManagerListener> listeners;

	public UpdateManager( Program program ) {
		this.program = program;

		catalogs = new CopyOnWriteArraySet<>();
		modules = new ConcurrentHashMap<>();
		updates = new ConcurrentHashMap<>();
		products = new ConcurrentHashMap<>();
		productCards = new ConcurrentHashMap<>();
		productStates = new ConcurrentHashMap<>();
		listeners = new CopyOnWriteArraySet<>();

		// Register included products.
		includedProducts = new HashSet<>();
		includedProducts.add( program.getCard().getProductKey() );
		includedProducts.add( new com.xeomar.annex.Program().getMetadata().getProductKey() );

		// Create the posted update cache.
		postedUpdateCache = new CopyOnWriteArraySet<>();
	}

	public int getCatalogCount() {
		return catalogs.size();
	}

	public void addCatalog( ProductCatalog source ) {
		catalogs.add( source );
		saveSettings();
	}

	public void removeCatalog( ProductCatalog source ) {
		catalogs.remove( source );
		saveSettings();
	}

	public void setCatalogEnabled( ProductCatalog catalog, boolean enabled ) {
		catalog.setEnabled( enabled );
		saveSettings();
	}

	public Set<ProductCatalog> getCatalogs() {
		return new HashSet<>( catalogs );
	}

	public Set<Module> getModules() {
		return new HashSet<>( modules.values() );
	}

	public Product getProduct( String productKey ) {
		return productKey == null ? program : products.get( productKey );
	}

	public Set<ProductCard> getProductCards() {
		return new HashSet<>( productCards.values() );
	}

	public void registerProduct( Product product ) {
		String productKey = product.getCard().getProductKey();
		products.put( productKey, product );
		productCards.put( productKey, product.getCard() );
		productStates.put( productKey, new ProductState() );
	}

	public void unregisterProduct( Product product ) {
		String productKey = product.getCard().getProductKey();
		products.remove( productKey );
		productCards.remove( productKey );
		productStates.remove( productKey );
	}

	public int getInstalledProductCount() {
		return productCards.size();
	}

	/**
	 * Determines if a product is installed regardless of release.
	 *
	 * @param card
	 * @return
	 */
	public boolean isInstalled( ProductCard card ) {
		return productCards.get( card.getProductKey() ) != null;
	}

	/**
	 * Determines if a specific release of a product is installed.
	 *
	 * @param card
	 * @return
	 */
	public boolean isReleaseInstalled( ProductCard card ) {
		ProductCard internal = productCards.get( card.getProductKey() );
		return internal != null && internal.getRelease().equals( card.getRelease() );
	}

	public boolean isUpdatable( ProductCard card ) {
		ProductState state = productStates.get( card.getProductKey() );
		return state != null && state.updatable;
	}

	public void setUpdatable( ProductCard card, boolean updatable ) {
		if( isUpdatable( card ) == updatable ) return;
		ProductState state = productStates.get( card.getProductKey() );
		if( state == null ) return;

		state.updatable = updatable;
		new UpdateManagerEvent( this, UpdateManagerEvent.Type.PRODUCT_CHANGED, card ).fire( listeners );
	}

	public boolean isRemovable( ProductCard card ) {
		ProductState state = productStates.get( card.getProductKey() );
		return state != null && state.removable;
	}

	public void setRemovable( ProductCard card, boolean removable ) {
		if( isRemovable( card ) == removable ) return;
		ProductState state = productStates.get( card.getProductKey() );
		if( state == null ) return;

		state.removable = removable;
		new UpdateManagerEvent( this, UpdateManagerEvent.Type.PRODUCT_CHANGED, card ).fire( listeners );
	}

	public boolean isEnabled( ProductCard card ) {
		return program.getSettingsManager().getProductSettings( card ).getBoolean( PRODUCT_ENABLED_KEY, false );
	}

	public void setEnabled( ProductCard card, boolean enabled ) {
		if( isEnabled( card ) == enabled ) return;

		// NEXT Implement setEnabledImpl()
		//		setEnabledImpl( card, enabled );

		Settings settings = program.getSettingsManager().getProductSettings( card );
		settings.set( PRODUCT_ENABLED_KEY, enabled );
		settings.flush();
		log.trace( "Set enabled: ", settings.getPath(), ": ", enabled );

		new UpdateManagerEvent( this, enabled ? UpdateManagerEvent.Type.PRODUCT_ENABLED : UpdateManagerEvent.Type.PRODUCT_DISABLED, card ).fire( listeners );
	}

	/**
	 * Get the path to the updater library.
	 *
	 * @return
	 */
	public File getUpdaterPath() {
		return updater;
	}

	/**
	 * Get the path to the updater library.
	 *
	 * @param file
	 */
	public void setUpdaterPath( File file ) {
		this.updater = file;
	}

	public void checkForUpdates() {
		// TODO Implement checkForUpdates()
	}

	@Override
	public boolean isRunning() {
		return false;
	}

	@Override
	public UpdateManager start() {
		return this;
	}

	@Override
	public UpdateManager awaitStart( long timeout, TimeUnit unit ) throws InterruptedException {
		return this;
	}

	@Override
	public UpdateManager restart() {
		return this;
	}

	@Override
	public UpdateManager awaitRestart( long timeout, TimeUnit unit ) throws InterruptedException {
		return this;
	}

	@Override
	public UpdateManager stop() {
		return this;
	}

	@Override
	public UpdateManager awaitStop( long timeout, TimeUnit unit ) throws InterruptedException {
		return this;
	}

	public void addProductManagerListener( UpdateManagerListener listener ) {
		listeners.add( listener );
	}

	public void removeProductManagerListener( UpdateManagerListener listener ) {
		listeners.remove( listener );
	}

	@Override
	public void setSettings( Settings settings ) {
		if( settings == null || this.settings != null ) return;

		this.settings = settings;

		//		// TODO Load the product catalogs
		//		Set<ProductCatalog> catalogsSet = new CopyOnWriteArraySet<ProductCatalog>();
		//		Set<Settings> catalogsSettings = settings.getChildNodes( CATALOGS_SETTINGS_KEY );
		//		for( Settings catalogSettings : catalogsSettings ) {
		//			ProductCatalog catalog = new ProductCatalog();
		//			catalog.loadSettings( catalogSettings );
		//			catalogsSet.add( catalog );
		//		}
		//		this.catalogs = catalogsSet;
		//
		//		// TODO Load the product updates
		//		Map<String, ProductUpdate> updatesMap = new ConcurrentHashMap<String, ProductUpdate>();
		//		Map<String, Settings> updatesSettings = settings.getNodeMap( UPDATES_SETTINGS_KEY, this.updates );
		//		for( String key : updatesSettings.keySet() ) {
		//			Settings updateSettings = updatesSettings.get( key );
		//			ProductUpdate update = new ProductUpdate();
		//			update.loadSettings( updateSettings );
		//			updatesMap.put( key, update );
		//		}
		//		this.updates = updatesMap;

		Settings updateSettings = settings.getNode( "update" );
		this.checkOption = CheckOption.valueOf( updateSettings.get( CHECK, CheckOption.INTERVAL ).toUpperCase() );
		this.foundOption = FoundOption.valueOf( updateSettings.get( FOUND, FoundOption.SELECT ).toUpperCase() );
		this.applyOption = ApplyOption.valueOf( updateSettings.get( APPLY, ApplyOption.VERIFY ).toUpperCase() );
	}

	@Override
	public Settings getSettings() {
		return settings;
	}

	private void saveSettings() {
		// TODO settings.putNodeSet( CATALOGS_SETTINGS_KEY, catalogs );
		// TODO settings.putNodeMap( UPDATES_SETTINGS_KEY, updates );

		Settings updateSettings = settings.getNode( "update" );
		updateSettings.set( CHECK, checkOption.name().toLowerCase() );
		updateSettings.set( FOUND, foundOption.name().toLowerCase() );
		updateSettings.set( APPLY, applyOption.name().toLowerCase() );

		settings.flush();
	}

	//	private final class SettingChangeHandler implements SettingListener {
	//
	//		@Override
	//		public void settingChanged( SettingEvent event ) {
	//			if( CHECK.equals( event.getKey() ) ) {
	//				setCheckOption( CheckOption.valueOf( event.getNewValue().toUpperCase() ) );
	//			} else if( event.getFullPath().startsWith( ServiceSettingsPath.UPDATE_SETTINGS_PATH + "/check" ) ) {
	//				scheduleUpdateCheck( false );
	//			}
	//		}
	//
	//	}

	private final class UpdateCheckTask extends TimerTask {

		private UpdateManager updateManager;

		public UpdateCheckTask( UpdateManager updateManager ) {
			this.updateManager = updateManager;
		}

		@Override
		public void run() {
			updateManager.checkForUpdates();
		}

	}

	private final class ProductState {

		public boolean updatable;

		public boolean removable;

		public ProductState() {
			this.updatable = false;
			this.removable = false;
		}

	}

}
