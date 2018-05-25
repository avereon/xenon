package com.xeomar.xenon.update;

import com.xeomar.product.Product;
import com.xeomar.product.ProductCard;
import com.xeomar.settings.Settings;
import com.xeomar.settings.SettingsEvent;
import com.xeomar.settings.SettingsListener;
import com.xeomar.util.*;
import com.xeomar.xenon.*;
import com.xeomar.xenon.Module;
import javafx.application.Platform;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutionException;
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

	public static final String MODULE_INSTALL_FOLDER_NAME = "modules";

	public static final String DEFAULT_CATALOG_FILE_NAME = "catalog.card";

	public static final String DEFAULT_PRODUCT_FILE_NAME = "META-INF/product.card";

	public static final String PRODUCT_DESCRIPTOR_PATH = "META-INF/" + DEFAULT_PRODUCT_FILE_NAME;

	public static final String UPDATER_LOG_NAME = "updater.log";

	public static final String UPDATE_FOLDER_NAME = "updates";

	public static final String LAST_CHECK_TIME = "product-update-last-check-time";

	public static final String NEXT_CHECK_TIME = "product-update-next-check-time";

	private static final String CHECK = "product-update-check";

	private static final String INTERVAL_UNIT = CHECK + "-interval-unit";

	private static final String SCHEDULE_WHEN = CHECK + "-schedule-when";

	private static final String SCHEDULE_HOUR = CHECK + "-schedule-hour";

	private static final String NOTICE = "product-update-notice";

	private static final String FOUND = "product-update-found";

	private static final String APPLY = "product-update-apply";

	private static final String CATALOGS_SETTINGS_KEY = "catalogs";

	private static final String REMOVES_SETTINGS_KEY = "removes";

	private static final String UPDATES_SETTINGS_KEY = "updates";

	private static final String PRODUCT_ENABLED_KEY = "enabled";

	private static final int POSTED_UPDATE_CACHE_TIMEOUT = 60000;

	private static final int MILLIS_IN_HOUR = 3600000;

	private static final int NO_CHECK = -1;

	private Program program;

	private Settings settings;

	private Set<MarketCard> catalogs;

	private Map<String, Module> modules;

	private Path homeModuleFolder;

	private Path userProductFolder;

	private CheckOption checkOption;

	private FoundOption foundOption;

	private ApplyOption applyOption;

	private Map<String, Product> products;

	private Map<String, ProductCard> productCards;

	private Map<String, ProductUpdate> updates;

	private Map<String, ProductState> productStates;

	private Set<ProductCard> postedUpdateCache;

	private Set<String> includedProducts;

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
		postedUpdateCache = new CopyOnWriteArraySet<>();
		listeners = new CopyOnWriteArraySet<>();

		// Register included products
		includedProducts = new HashSet<>();
		includedProducts.add( program.getCard().getProductKey() );
		includedProducts.add( new com.xeomar.annex.Program().getCard().getProductKey() );
	}

	public int getCatalogCount() {
		return catalogs.size();
	}

	public void addCatalog( MarketCard source ) {
		catalogs.add( source );
		saveCatalogs();
	}

	public void removeCatalog( MarketCard source ) {
		catalogs.remove( source );
		saveCatalogs();
	}

	public void setCatalogEnabled( MarketCard catalog, boolean enabled ) {
		catalog.setEnabled( enabled );
		saveCatalogs();
	}

	public Set<MarketCard> getCatalogs() {
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

	public void installProducts( ProductCard... cards ) throws Exception {
		installProducts( new HashSet<>( Arrays.asList( cards ) ) );
	}

	public void installProducts( Set<ProductCard> cards ) throws Exception {
		log.debug( "Number of products to install: " + cards.size() );

		//		// Download the product resources.
		//		Map<ProductCard, Set<ProductResource>> productResources = downloadProductResources( cards );
		//
		//		// TODO All the product resources may not have been successfully downloaded.
		//
		//		// Install the products.
		//		Set<InstalledProduct> installedProducts = new HashSet<InstalledProduct>();
		//		for( ProductCard card : cards ) {
		//			try {
		//				installedProducts.add( new InstalledProduct( getProductInstallFolder( card ) ) );
		//				installProductImpl( card, productResources );
		//			} catch( Exception exception ) {
		//				Log.write( exception );
		//			}
		//		}
		//
		//		Set<InstalledProduct> products = getStoredRemovedProducts();
		//		products.removeAll( installedProducts );
		//		service.getSettings().putNodeSet( REMOVES_SETTINGS_KEY, products );
	}

	public void uninstallProducts( ProductCard... cards ) throws Exception {
		uninstallProducts( new HashSet<>( Arrays.asList( cards ) ) );
	}

	public void uninstallProducts( Set<ProductCard> cards ) throws Exception {
		log.trace( "Number of products to remove: " + cards.size() );

		//		// Remove the products.
		//		Set<InstalledProduct> removedProducts = new HashSet<InstalledProduct>();
		//		for( ProductCard card : cards ) {
		//			removedProducts.add( new InstalledProduct( getProductInstallFolder( card ) ) );
		//			removeProductImpl( getProduct( card.getProductKey() ) );
		//		}
		//
		//		Set<InstalledProduct> products = getStoredRemovedProducts();
		//		products.addAll( removedProducts );
		//		service.getSettings().putNodeSet( REMOVES_SETTINGS_KEY, products );
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
		return program.getSettingsManager().getProductSettings( card ).get( PRODUCT_ENABLED_KEY, Boolean.class, false );
	}

	public void setEnabled( ProductCard card, boolean enabled ) {
		if( isEnabled( card ) == enabled ) return;

		// TODO Implement setEnabledImpl()
		//		setEnabledImpl( card, enabled );

		Settings settings = program.getSettingsManager().getProductSettings( card );
		settings.set( PRODUCT_ENABLED_KEY, enabled );
		settings.flush();
		log.trace( "Set enabled: ", settings.getPath(), ": ", enabled );

		new UpdateManagerEvent( this, enabled ? UpdateManagerEvent.Type.PRODUCT_ENABLED : UpdateManagerEvent.Type.PRODUCT_DISABLED, card ).fire( listeners );
	}

	public CheckOption getCheckOption() {
		return checkOption;
	}

	public void setCheckOption( CheckOption checkOption ) {
		this.checkOption = checkOption;
		settings.set( CHECK, checkOption.name().toLowerCase() );
	}

	public FoundOption getFoundOption() {
		return foundOption;
	}

	public void setFoundOption( FoundOption foundOption ) {
		this.foundOption = foundOption;
		settings.set( FOUND, foundOption.name().toLowerCase() );
	}

	public ApplyOption getApplyOption() {
		return applyOption;
	}

	public void setApplyOption( ApplyOption applyOption ) {
		this.applyOption = applyOption;
		settings.set( APPLY, applyOption.name().toLowerCase() );
	}

	public long getLastUpdateCheck() {
		return getSettings().get( LAST_CHECK_TIME, Long.class, 0L );
	}

	public long getNextUpdateCheck() {
		return getSettings().get( NEXT_CHECK_TIME, Long.class, 0L );
	}

	/**
	 * Schedule the update check task according to the settings. This method may
	 * safely be called as many times as necessary from any thread.
	 *
	 * @param startup True if the method is called at program start
	 */
	public synchronized void scheduleUpdateCheck( boolean startup ) {
		if( task != null ) {
			boolean alreadyRun = task.scheduledExecutionTime() < System.currentTimeMillis();
			task.cancel();
			task = null;
			if( !alreadyRun ) log.trace( "Current check for updates task cancelled for new schedule." );
		}

		// Don't schedule tasks if the NOUPDATECHECK flag is set
		if( program.getProgramParameters().isSet( ProgramFlag.NOUPDATECHECK ) ) return;

		Settings checkSettings = getSettings();

		long lastUpdateCheck = getLastUpdateCheck();
		long timeSinceLastCheck = System.currentTimeMillis() - lastUpdateCheck;
		long delay;

		// This is required to avoid a memory use problem during testing
		if( TestUtil.isTest() ) checkOption = CheckOption.MANUAL;

		switch( checkOption ) {
			case MANUAL:
				delay = NO_CHECK;
				break;
			case STARTUP:
				delay = startup ? 0 : NO_CHECK;
				break;
			case INTERVAL: {
				CheckInterval intervalUnit = CheckInterval.valueOf( checkSettings.get( INTERVAL_UNIT, CheckInterval.DAY.name() ).toUpperCase() );
				delay = getNextIntervalTime( System.currentTimeMillis(), intervalUnit, lastUpdateCheck, timeSinceLastCheck );
				break;
			}
			case SCHEDULE: {
				CheckWhen scheduleWhen = CheckWhen.valueOf( checkSettings.get( SCHEDULE_WHEN, CheckWhen.DAILY.name() ).toUpperCase() );
				int scheduleHour = checkSettings.get( SCHEDULE_HOUR, Integer.class, 0 );
				delay = getNextScheduleTime( System.currentTimeMillis(), scheduleWhen, scheduleHour );
				break;
			}
			default: {
				delay = NO_CHECK;
				break;
			}
		}

		if( delay == NO_CHECK ) {
			log.debug( "No update check scheduled." );
			return;
		}

		// Create the update check task.
		task = new UpdateCheckTask( this );

		// Schedule the update check task.
		timer.schedule( task, delay );

		long nextCheckTime = System.currentTimeMillis() + delay;

		// Set the next update check time in the settings.
		checkSettings.set( NEXT_CHECK_TIME, nextCheckTime );

		// Log the next update check time.
		String date = DateUtil.format( new Date( nextCheckTime ), DateUtil.DEFAULT_DATE_FORMAT, DateUtil.LOCAL_TIME_ZONE );
		log.debug( "Next check scheduled for: " + (delay == 0 ? "now" : date) );
	}

	// NEXT Overlay ProgramProductManager methods implementations
	public void checkForUpdates() {
		if( !isEnabled() ) return;

		try {
			log.trace( "Checking for staged updates..." );
			int stagedUpdateCount = stagePostedUpdates();
			if( stagedUpdateCount > 0 ) {
				log.debug( "Staged updates found, restarting..." );
				program.requestRestart( ProgramFlag.NOUPDATECHECK );
			}
		} catch( Exception exception ) {
			log.error( "Error checking for updates", exception );
		}
	}

	public Set<ProductCard> findPostedUpdates() throws ExecutionException, InterruptedException, URISyntaxException {
		return findPostedUpdates( false );
	}

	/**
	 * Gets the set of posted product updates. If there are no posted updates
	 * found an empty set is returned.
	 *
	 * @return The set of posted updates.
	 * @throws ExecutionException If a task execution exception occurs
	 * @throws InterruptedException If the calling thread is interrupted
	 * @throws URISyntaxException If a URI cannot be resolved correctly
	 */
	public Set<ProductCard> findPostedUpdates( boolean force ) throws ExecutionException, InterruptedException, URISyntaxException {
		Set<ProductCard> availableCards = new HashSet<>();
		if( !isEnabled() ) return availableCards;

		// If the posted update cache is still valid return the updates in the cache.
		long postedCacheAge = System.currentTimeMillis() - postedUpdateCacheTime;
		if( !force && postedCacheAge < POSTED_UPDATE_CACHE_TIMEOUT ) return new HashSet<>( postedUpdateCache );

		// Update when the last update check occurred.
		getSettings().set( LAST_CHECK_TIME, System.currentTimeMillis() );

		// Schedule the next update check.
		scheduleUpdateCheck( false );

		// Download the descriptors for each product.
		Set<ProductCard> oldCards = getProductCards();
		Map<ProductCard, DownloadTask> tasks = new HashMap<>();
		URISyntaxException uriSyntaxException = null;
		for( ProductCard installedCard : oldCards ) {
			try {
				URI codebase = installedCard.getCardUri( getProductChannel() );
				URI uri = getResolvedUpdateUri( codebase );
				if( uri == null ) {
					log.warn( "Installed pack does not have source defined: " + installedCard.toString() );
					continue;
				} else {
					log.debug( "Installed pack source: " + uri );
				}

				DownloadTask task = new DownloadTask( program, uri );
				tasks.put( installedCard, task );
				program.getExecutor().submit( task );
			} catch( URISyntaxException exception ) {
				uriSyntaxException = exception;
			}
		}

		// Determine what products have posted updates.
		ExecutionException executionException = null;
		InterruptedException interruptedException = null;
		for( ProductCard installedCard : oldCards ) {
			try {
				DownloadTask task = tasks.get( installedCard );
				if( task == null ) continue;

				ProductCard availableCard;
				try( InputStream input = task.get().getInputStream() ) {
					availableCard = new ProductCard().load( input, task.getUri() );
				} catch( IOException exception ) {
					log.warn( "Error loading product card: " + task.getUri(), exception );
					continue;
				}

				// Validate the pack key.
				if( !installedCard.getProductKey().equals( availableCard.getProductKey() ) ) {
					log.warn( "Pack mismatch: " + installedCard.getProductKey() + " != " + availableCard.getProductKey() );
					continue;
				}

				log.debug( "Installed: " + installedCard.getProductKey() + " " + installedCard.getRelease() );
				log.debug( "Available: " + availableCard.getProductKey() + " " + availableCard.getRelease() );

				// FIXME Force available update for testing
				availableCards.add( availableCard );
				//				if( availableCard.getRelease().compareTo( installedCard.getRelease() ) > 0 ) {
				//					log.debug( "Update found for: " + installedCard.getProductKey() + " > " + availableCard.getRelease() );
				//					availableCards.add( availableCard );
				//				}
			} catch( ExecutionException exception ) {
				if( executionException == null ) executionException = exception;
			} catch( InterruptedException exception ) {
				if( interruptedException == null ) interruptedException = exception;
			}
		}

		// If there is an exception and there are no updates, throw the exception.
		if( availableCards.size() == 0 ) {
			if( uriSyntaxException != null ) throw uriSyntaxException;
			if( executionException != null ) throw executionException;
			if( interruptedException != null ) throw interruptedException;
		}

		// Cache the discovered updates.
		postedUpdateCacheTime = System.currentTimeMillis();
		postedUpdateCache = new CopyOnWriteArraySet<>( availableCards );

		return availableCards;
	}

	public boolean cacheSelectedUpdates( Set<ProductCard> packs ) throws Exception {
		throw new RuntimeException( "Method not implemented yet." );
	}

	public boolean stageCachedUpdates( Set<ProductCard> packs ) throws Exception {
		throw new RuntimeException( "Method not implemented yet." );
	}

	/**
	 * Attempt to stage the product packs from posted updates.
	 *
	 * @return true if one or more product packs were staged.
	 * @throws IOException If an IO error occurs
	 * @throws ExecutionException If an execution error occurs
	 * @throws InterruptedException If the method is interrupted
	 * @throws URISyntaxException If a URI cannot be resolved correctly
	 */
	public int stagePostedUpdates() throws IOException, ExecutionException, InterruptedException, URISyntaxException {
		if( !isEnabled() ) return 0;
		stageUpdates( findPostedUpdates() );
		return updates.size();
	}

	public Path getProductInstallFolder( ProductCard card ) {
		Path installFolder = program.getDataFolder().resolve( MODULE_INSTALL_FOLDER_NAME );
		return installFolder.resolve( card.getGroup() + "." + card.getArtifact() );
	}

	public Map<ProductCard, Set<ProductResource>> stageUpdates( ProductCard... updateCards ) throws IOException {
		return stageUpdates( Set.of( updateCards ) );
	}

	/**
	 * Attempt to stage the product packs described by the specified product
	 * cards.
	 *
	 * @param updateCards The set of update cards to stage
	 * @return true if one or more product packs were staged.
	 * @throws IOException If an IO error occurs
	 */
	public Map<ProductCard, Set<ProductResource>> stageUpdates( Set<ProductCard> updateCards ) throws IOException {
		if( updateCards.size() == 0 ) return null;

		Path stageFolder = program.getDataFolder().resolve( UPDATE_FOLDER_NAME );
		Files.createDirectories( stageFolder );

		log.debug( "Number of packs to stage: " + updateCards.size() );
		log.trace( "Pack stage folder: " + stageFolder );

		// Download the product resources.
		Map<ProductCard, Set<ProductResource>> productResources = downloadProductResources( updateCards );
		log.debug( "Product resource count: " + productResources.size() );

		// Create an update for each product.
		for( ProductCard updateCard : updateCards ) {
			// Verify the product is registered
			ProductCard productCard = productCards.get( updateCard.getProductKey() );
			if( productCard == null ) {
				log.warn( "Product not registered: " + updateCard );
				continue;
			}

			// Verify the product is installed
			Path installFolder = productCard.getInstallFolder();
			boolean installFolderValid = installFolder != null && Files.exists( installFolder );
			if( !installFolderValid ) {
				log.warn( "Product not installed: " + updateCard );
				log.debug( "Missing install folder: " + installFolder );
				continue;
			}

			// Verify the resources have all been staged successfully
			Set<ProductResource> resources = productResources.get( updateCard );
			if( !areResourcesValid( resources ) ) {
				log.warn( "Update missing resources: " + updateCard );
				continue;
			}

			Path updatePack = stageFolder.resolve( getStagedUpdateFileName( updateCard ) );
			createUpdatePack( productResources.get( updateCard ), updatePack );

			ProductUpdate update = new ProductUpdate( updateCard, updatePack, installFolder );

			// Remove any old staged updates for this product.
			updates.remove( update );

			// Add the update to the set of staged updates.
			updates.put( update.getCard().getProductKey(), update );

			// Notify listeners the update is staged.
			new UpdateManagerEvent( this, UpdateManagerEvent.Type.PRODUCT_STAGED, updateCard ).fire( listeners );

			log.debug( "Update staged: " + updateCard.getProductKey() + " " + updateCard.getRelease() );
			log.debug( "Update pack:   " + updatePack );
		}

		saveUpdates();

		return productResources;
	}

	private String getStagedUpdateFileName( ProductCard card ) {
		return card.getGroup() + "." + card.getArtifact() + ".pack";
	}

	public Set<ProductUpdate> getStagedUpdates() {
		Set<ProductUpdate> staged = new HashSet<>();
		Set<ProductUpdate> remove = new HashSet<>();

		for( ProductUpdate update : updates.values() ) {
			if( Files.exists( update.getSource() ) ) {
				staged.add( update );
				log.debug( "Staged update found: " + update.getSource() );
			} else {
				remove.add( update );
				log.warn( "Staged update missing: " + update.getSource() );
			}
		}

		// Remove updates that cannot be found.
		if( remove.size() > 0 ) {
			for( ProductUpdate update : remove ) {
				updates.remove( update );
			}
			saveUpdates();
		}

		return staged;
	}

	int getStagedUpdateCount() {
		return getStagedUpdates().size();
	}

	public boolean areUpdatesStaged() {
		return getStagedUpdateCount() > 0;
	}

	public boolean isStaged( ProductCard card ) {
		for( ProductUpdate update : getStagedUpdates() ) {
			if( card.equals( update.getCard() ) ) return true;
		}
		return false;
	}

	public boolean isReleaseStaged( ProductCard card ) {
		ProductUpdate update = updates.get( card.getProductKey() );
		if( update == null ) return false;

		ProductCard internal = update.getCard();
		return internal != null && internal.getRelease().equals( card.getRelease() );
	}

	/**
	 * Apply updates. If updates are found then the method returns the number of
	 * updates applied.
	 *
	 * @return The number of updates applied.
	 */
	public final int updateProduct( String... extras ) {
		if( program.getHomeFolder() == null ) {
			log.warn( "Program not running from updatable location." );
			return 0;
		}

		log.trace( "Checking for staged updates..." );

		// If updates are staged, apply them.
		int result = 0;
		int updateCount = getStagedUpdateCount();
		if( updateCount > 0 ) {
			log.info( "Staged updates detected: {}", updateCount );
			try {
				result = userApplyStagedUpdates( extras );
			} catch( Exception exception ) {
				log.warn( "Failed to apply staged updates", exception );
			}
		} else {
			log.debug( "No staged updates detected." );
		}
		return result;
	}

	public int userApplyStagedUpdates( String... extras ) {
		return applyStagedUpdates( extras );
	}

	/**
	 * Launch the update program to apply the staged updates. This method is
	 * generally called when the program starts and, if the update program is
	 * successfully started, the program should be terminated to allow for the
	 * updates to be applied.
	 *
	 * @param extras Extra commands to add to the update program when launched.
	 * @return The number of updates applied.
	 */
	public int applyStagedUpdates( String... extras ) {
		log.info( "Update manager enabled: " + isEnabled() );
		if( !isEnabled() || getStagedUpdateCount() == 0 ) return 0;

		log.info( "Starting update process..." );

		// Store the update count to be returned after the collection is cleared
		int count = updates.size();

		Platform.runLater( () -> program.requestUpdate( ProgramFlag.UPDATE_IN_PROGRESS ) );

		return count;
	}

	void clearStagedUpdates() {
		// Remove the updates settings.
		updates.clear();
		saveUpdates();
	}

	//	public void loadProducts( File... folders ) throws Exception {
	//		ClassLoader parent = getClass().getClassLoader();
	//
	//		// Look for modules in the specified folders.
	//		for( File folder : folders ) {
	//			if( !folder.exists() ) continue;
	//			if( !folder.isDirectory() ) continue;
	//
	//			// Look for simple modules (not common).
	//			File[] jars = folder.listFiles( FileUtil.JAR_FILE_FILTER );
	//			for( File jar : jars ) {
	//				Log.write( Log.DEBUG, "Searching for module in: " + jar.toURI() );
	//				URI uri = URI.create( "jar:" + jar.toURI().toASCIIString() + "!/" + PRODUCT_DESCRIPTOR_PATH );
	//				ProductCard card = new ProductCard( jar.getParentFile().toURI(), new XmlDescriptor( uri ) );
	//				if( !isReservedProduct( card ) ) loadSimpleModule( card, jar.toURI(), parent );
	//			}
	//
	//			// Look for normal modules (most common).
	//			File[] moduleFolders = folder.listFiles( FileUtil.FOLDER_FILTER );
	//			for( File moduleFolder : moduleFolders ) {
	//				Log.write( Log.DEBUG, "Searching for module in: " + moduleFolder.toURI() );
	//
	//				jars = moduleFolder.listFiles( FileUtil.JAR_FILE_FILTER );
	//				for( File jar : jars ) {
	//					try {
	//						URI uri = URI.create( "jar:" + jar.toURI().toASCIIString() + "!/" + PRODUCT_DESCRIPTOR_PATH );
	//						ProductCard card = new ProductCard( jar.getParentFile().toURI(), new XmlDescriptor( uri ) );
	//						if( !isReservedProduct( card ) ) loadNormalModule( card, moduleFolder.toURI(), parent );
	//					} catch( FileNotFoundException exception ) {
	//						// Not finding a product card is a common situation with dependencies.
	//					} catch( Throwable throwable ) {
	//						Log.write( throwable, jar );
	//					}
	//				}
	//			}
	//		}
	//	}

	public static Map<String, ProductCard> getProductCardMap( Set<ProductCard> cards ) {
		if( cards == null ) return null;

		Map<String, ProductCard> map = new HashMap<>();
		for( ProductCard card : cards ) {
			map.put( card.getProductKey(), card );
		}

		return map;
	}

	public static boolean areResourcesValid( Set<ProductResource> resources ) {
		for( ProductResource resource : resources ) {
			if( !resource.isValid() ) return false;
		}

		return true;
	}

	public static long getNextIntervalTime( long currentTime, CheckInterval intervalUnit, long lastUpdateCheck, long timeSinceLastCheck ) {
		long delay;
		long intervalDelay = 0;
		switch( intervalUnit ) {
			case MONTH: {
				intervalDelay = 30L * 24L * MILLIS_IN_HOUR;
				break;
			}
			case WEEK: {
				intervalDelay = 7L * 24L * MILLIS_IN_HOUR;
				break;
			}
			case DAY: {
				intervalDelay = 24L * MILLIS_IN_HOUR;
				break;
			}
			case HOUR: {
				intervalDelay = (long)MILLIS_IN_HOUR;
				break;
			}
		}

		if( timeSinceLastCheck > intervalDelay ) {
			// Check now and schedule again.
			delay = 0;
		} else {
			// Schedule the next interval.
			delay = (lastUpdateCheck + intervalDelay) - currentTime;
		}
		return delay;
	}

	public static long getNextScheduleTime( long currentTime, CheckWhen scheduleWhen, int scheduleHour ) {
		Calendar calendar = new GregorianCalendar( DateUtil.DEFAULT_TIME_ZONE );

		// Calculate the next update check.
		calendar.setTimeInMillis( currentTime );
		calendar.set( Calendar.HOUR_OF_DAY, scheduleHour );
		calendar.set( Calendar.MINUTE, 0 );
		calendar.set( Calendar.SECOND, 0 );
		calendar.set( Calendar.MILLISECOND, 0 );
		if( scheduleWhen != CheckWhen.DAILY ) calendar.set( Calendar.DAY_OF_WEEK, scheduleWhen.ordinal() );
		long result = calendar.getTimeInMillis() - currentTime;

		// If past the scheduled time, add a day or week.
		if( result < 0 ) {
			if( scheduleWhen == CheckWhen.DAILY ) {
				result += 24 * MILLIS_IN_HOUR;
			} else {
				result += 7 * 24 * MILLIS_IN_HOUR;
			}
		}

		return result;
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

		this.checkOption = CheckOption.valueOf( settings.get( CHECK, CheckOption.MANUAL.name() ).toUpperCase() );
		this.foundOption = FoundOption.valueOf( settings.get( FOUND, FoundOption.SELECT.name() ).toUpperCase() );
		this.applyOption = ApplyOption.valueOf( settings.get( APPLY, ApplyOption.VERIFY.name() ).toUpperCase() );

		// Load the product catalogs
		loadCatalogs();

		// Load the product updates
		loadUpdates();
	}

	@Override
	public Settings getSettings() {
		return settings;
	}

	protected boolean isEnabled() {
		return !program.getParameters().getNamed().containsKey( ProgramFlag.NOUPDATE );
	}

	@Override
	public boolean isRunning() {
		return timer != null;
	}

	@Override
	public UpdateManager start() {
		//		cleanRemovedProducts();
		//
		getSettings().addSettingsListener( new SettingsChangeHandler() );

		// Create the update check timer.
		timer = new Timer( true );

		// Define the module folders.
		homeModuleFolder = program.getHomeFolder().resolve( MODULE_INSTALL_FOLDER_NAME );
		userProductFolder = program.getDataFolder().resolve( MODULE_INSTALL_FOLDER_NAME );

		// Create the default module folders list.
		List<Path> moduleFolders = new ArrayList<>();
		moduleFolders.add( homeModuleFolder );
		moduleFolders.add( userProductFolder );

		// Check for module paths in the parameters.
		List<String> modulePaths = program.getProgramParameters().getValues( "module" );
		if( modulePaths != null ) {
			for( String path : modulePaths ) {
				Path folder = Paths.get( path );
				if( Files.exists( folder ) ) moduleFolders.add( folder );
			}
		}

		// TODO Load modules
		//		loadProducts( moduleFolders.toArray( new File[ moduleFolders.size() ] ) );

		return this;
	}

	@Override
	public UpdateManager awaitStart( long timeout, TimeUnit unit ) throws InterruptedException {
		return this;
	}

	@Override
	public UpdateManager restart() {
		try {
			stop();
			awaitStop( 1, TimeUnit.SECONDS );
			start();
		} catch( InterruptedException exception ) {
			// Intentionally ignore exception
		}
		return this;
	}

	@Override
	public UpdateManager awaitRestart( long timeout, TimeUnit unit ) throws InterruptedException {
		return this;
	}

	@Override
	public UpdateManager stop() {
		if( timer != null ) timer.cancel();
		timer = null;
		return this;
	}

	@Override
	public UpdateManager awaitStop( long timeout, TimeUnit unit ) throws InterruptedException {
		return this;
	}

	private void loadCatalogs() {
		// NOTE The TypeReference must have the parameterized type in it, the diamond operator cannot be used here
		catalogs = settings.get( CATALOGS_SETTINGS_KEY, new TypeReference<Set<MarketCard>>() {}, catalogs );
	}

	private void saveCatalogs() {
		settings.set( CATALOGS_SETTINGS_KEY, catalogs );
	}

	private void loadUpdates() {
		// NOTE The TypeReference must have the parameterized type in it, the diamond operator cannot be used here
		updates = settings.get( UPDATES_SETTINGS_KEY, new TypeReference<Map<String, ProductUpdate>>() {}, updates );
	}

	private void saveUpdates() {
		settings.set( UPDATES_SETTINGS_KEY, updates );
	}

	private boolean isReservedProduct( ProductCard card ) {
		return includedProducts.contains( card.getProductKey() );
	}

	//	private void installProductImpl( ProductCard card, Map<ProductCard, Set<ProductResource>> productResources ) throws Exception {
	//		File installFolder = getProductInstallFolder( card );
	//
	//		Log.write( Log.TRACE, "Install product to: " + installFolder );
	//		Set<ProductResource> resources = productResources.get( card );
	//
	//		// Install all the resource files to the install folder.
	//		copyProductResources( resources, installFolder );
	//
	//		// Load the product.
	//		loadProducts( userProductFolder );
	//
	//		// Set the enabled state.
	//		setEnabledImpl( card, isEnabled( card ) );
	//
	//		// Notify listeners of install.
	//		fireProductManagerEvent( new ProductManagerEvent( this, Type.PRODUCT_INSTALLED, card ) );
	//
	//		// Enable the product.
	//		setEnabled( card, true );
	//	}
	//
	//	private void removeProductImpl( ServiceProduct product ) {
	//		ProductCard card = product.getCard();
	//
	//		File installFolder = getProductInstallFolder( card );
	//
	//		Log.write( Log.TRACE, "Remove product from: " + installFolder );
	//
	//		// Disable the product.
	//		setEnabled( card, false );
	//
	//		// Remove the module.
	//		modules.remove( card.getProductKey() );
	//
	//		// Remove the product from the manager.
	//		unregisterProduct( product );
	//
	//		// Remove the product settings.
	//		ProductUtil.getSettings( product ).removeNode();
	//
	//		// Notify listeners of remove.
	//		fireProductManagerEvent( new ProductManagerEvent( this, Type.PRODUCT_REMOVED, card ) );
	//	}
	//
	//	private void setEnabledImpl( ProductCard card, boolean enabled ) {
	//		ServiceModule module = modules.get( card.getProductKey() );
	//		if( module == null ) return;
	//
	//		if( enabled ) {
	//			//loaders.add( module.getClass().getClassLoader() );
	//
	//			try {
	//				module.register();
	//				module.create();
	//			} catch( Throwable throwable ) {
	//				Log.write( throwable );
	//			}
	//		} else {
	//			try {
	//				module.destroy();
	//				module.unregister();
	//			} catch( Throwable throwable ) {
	//				Log.write( throwable );
	//			}
	//
	//			//loaders.remove( module.getClass().getClassLoader() );
	//		}
	//	}
	//
	//	private void cleanRemovedProducts() {
	//		// Check for products marked for removal and remove the files.
	//		Set<InstalledProduct> products = getStoredRemovedProducts();
	//		for( InstalledProduct product : products ) {
	//			FileUtil.delete( product.getTarget() );
	//		}
	//		service.getSettings().removeNode( REMOVES_SETTINGS_KEY );
	//	}

	private URI getResolvedUpdateUri( URI uri ) {
		if( uri == null ) return null;

		if( uri.getScheme() == null ) uri = Paths.get( uri.getPath() ).toUri();
		return uri;
	}

	//	private Set<InstalledProduct> getStoredRemovedProducts() {
	//		Set<InstalledProduct> products = new HashSet<InstalledProduct>();
	//		Set<Settings> productSettings = service.getSettings().getChildNodes( REMOVES_SETTINGS_KEY );
	//		for( Settings settings : productSettings ) {
	//			InstalledProduct product = new InstalledProduct();
	//			product.loadSettings( settings );
	//			products.add( product );
	//		}
	//		return products;
	//	}

	private void createUpdatePack( Set<ProductResource> resources, Path update ) throws IOException {
		Path updateFolder = FileUtil.createTempFolder( "update", "folder" );

		copyProductResources( resources, updateFolder );

		FileUtil.deleteOnExit( updateFolder );

		FileUtil.zip( updateFolder, update );
	}

	private void copyProductResources( Set<ProductResource> resources, Path folder ) throws IOException {
		if( resources == null ) return;

		for( ProductResource resource : resources ) {
			if( resource.getLocalFile() == null ) continue;
			switch( resource.getType() ) {
				case FILE: {
					// Just copy the file.
					String path = resource.getUri().getPath();
					String name = path.substring( path.lastIndexOf( "/" ) + 1 );
					Path target = folder.resolve( name );
					FileUtil.copy( resource.getLocalFile(), target );
					break;
				}
				case PACK: {
					// Unpack the file.
					FileUtil.unzip( resource.getLocalFile(), folder );
					break;
				}
			}
		}
	}

	private Map<ProductCard, Set<ProductResource>> downloadProductResources( Set<ProductCard> cards ) {
		// Determine all the resources to download.
		Map<ProductCard, Set<ProductResource>> productResources = new HashMap<>();

		for( ProductCard card : cards ) {
			try {
				URI codebase = card.getCardUri( getProductChannel() );
				log.debug( "Resource codebase: " + codebase );
				PackProvider provider = new PackProvider( program, card, getProductChannel() );
				Set<ProductResource> resources = provider.getResources( codebase );

				log.debug( "Product resource count: " + resources.size() );

				for( ProductResource resource : resources ) {
					URI uri = getResolvedUpdateUri( resource.getUri() );
					log.debug( "Resource source: " + uri );

					// Submit download resource task
					resource.setFuture( program.getExecutor().submit( new DownloadTask( program, uri ) ) );
				}

				productResources.put( card, resources );
			} catch( URISyntaxException exception ) {
				log.error( "Error creating pack download", exception );
			}
		}

		// Wait for all resources to be downloaded.
		for( ProductCard card : cards ) {
			Set<ProductResource> resources = productResources.get( card );
			if( resources == null ) continue;
			for( ProductResource resource : resources ) {
				try {
					resource.waitFor();
					log.debug( "Resource target: " + resource.getLocalFile() );

					// TODO Verify resources are secure by checking digital signatures.
					// Reference: http://docs.oracle.com/javase/6/docs/technotes/guides/security/crypto/HowToImplAProvider.html#CheckJARFile

				} catch( Exception exception ) {
					resource.setThrowable( exception );
					log.error( "Error downloading resource: " + resource, exception );
				}
			}
		}

		return productResources;
	}

	private String getProductChannel() {
		return "latest";
	}

	//	private URI resolveCardUri( ProductCard card, String uri ) throws URISyntaxException {
	//		String artifact = card.getArtifact();
	//		String channel = "latest";
	//		return new URI( MessageFormat.format( uri, artifact, channel ) );
	//	}

	//	/**
	//	 * A folder module is and unpacked module contained in a folder.
	//	 *
	//	 * @param card
	//	 * @param folder
	//	 * @param parent
	//	 * @return
	//	 * @throws Exception
	//	 */
	//	private ServiceModule loadFolderModule( ProductCard card, File folder, ClassLoader parent ) throws Exception {
	//		URI codebase = folder.toURI();
	//
	//		card.setInstallFolder( folder );
	//
	//		// Create the class loader.
	//		ProductClassLoader loader = new ProductClassLoader( new URL[]{ folder.toURL() }, parent, codebase );
	//		return loadModule( card, loader, "FOLDER", true, true );
	//	}
	//
	//	/**
	//	 * A simple module is entirely contained inside a jar file.
	//	 *
	//	 * @param card
	//	 * @param jarUri
	//	 * @param parent
	//	 * @return
	//	 * @throws Exception
	//	 */
	//	private ServiceModule loadSimpleModule( ProductCard card, URI jarUri, ClassLoader parent ) throws Exception {
	//		URI codebase = UriUtil.getParent( jarUri );
	//
	//		// Get the jar file.
	//		File jarfile = new File( jarUri );
	//		card.setInstallFolder( jarfile.getParentFile() );
	//
	//		// Create the class loader.
	//		ProductClassLoader loader = new ProductClassLoader( new URL[]{ jarfile.toURI().toURL() }, parent, codebase );
	//		return loadModule( card, loader, "SIMPLE", true, true );
	//	}
	//
	//	/**
	//	 * A normal module, the most common, is entirely contained in a folder.
	//	 *
	//	 * @param card
	//	 * @param moduleFolderUri
	//	 * @param parent
	//	 * @return
	//	 * @throws Exception
	//	 */
	//	private ServiceModule loadNormalModule( ProductCard card, URI moduleFolderUri, ClassLoader parent ) throws Exception {
	//		// Get the folder to load from.
	//		File folder = new File( moduleFolderUri );
	//		card.setInstallFolder( folder );
	//
	//		// Find all the jars.
	//		Set<URL> urls = new HashSet<URL>();
	//		File[] files = folder.listFiles( FileUtil.JAR_FILE_FILTER );
	//		for( File file : files ) {
	//			urls.add( file.toURI().toURL() );
	//		}
	//
	//		// Create the class loader.
	//		ProductClassLoader loader = new ProductClassLoader( urls.toArray( new URL[ urls.size() ] ), parent, moduleFolderUri );
	//		return loadModule( card, loader, "NORMAL", true, true );
	//	}
	//
	//	private ServiceModule loadModule( ProductCard card, ClassLoader loader, String source, boolean updatable, boolean removable ) throws Exception {
	//		// Ignore included products.
	//		if( includedProducts.contains( card.getProductKey() ) ) return null;
	//
	//		// Check if module is already loaded.
	//		ServiceModule module = modules.get( card.getProductKey() );
	//		if( module != null ) return module;
	//
	//		// Validate class name.
	//		String className = card.getProductClassName();
	//		if( className == null ) return null;
	//
	//		// Load the module.
	//		try {
	//			Log.write( Log.DEBUG, "Loading ", source, " module: ", card.getProductKey() );
	//
	//			Class<?> moduleClass = loader.loadClass( className );
	//			Constructor<?> constructor = findConstructor( moduleClass );
	//
	//			module = (ServiceModule)constructor.newInstance( service, card );
	//			registerProduct( module, updatable, removable );
	//			Log.write( Log.TRACE, source, " module loaded:  ", card.getProductKey() );
	//		} catch( Throwable throwable ) {
	//			Log.write( Log.WARN, source, " module failed:  ", card.getProductKey(), " (", className, ")" );
	//			Log.write( Log.TRACE, throwable );
	//			return null;
	//		}
	//
	//		return module;
	//	}
	//
	//	private Constructor<?> findConstructor( Class<?> moduleClass ) throws NoSuchMethodException, SecurityException {
	//		// Look for a constructor that has assignable parameters.
	//		Constructor<?>[] constructors = moduleClass.getConstructors();
	//		if( constructors.length == 0 ) throw new NoSuchMethodException( "No constructors found: " + moduleClass.getName() );
	//
	//		for( Constructor<?> constructor : constructors ) {
	//			Class<?>[] types = constructor.getParameterTypes();
	//
	//			if( types.length != 2 ) continue;
	//
	//			boolean nameService = Service.class.getName().equals( types[ 0 ].getName() );
	//			boolean nameProductCard = ProductCard.class.getName().equals( types[ 1 ].getName() );
	//			boolean instanceofService = Service.class.isAssignableFrom( types[ 0 ] );
	//			boolean instanceofProductCard = ProductCard.class.isAssignableFrom( types[ 1 ] );
	//
	//			if( nameService && !instanceofService ) {
	//				Log.write( Log.WARN, "Class name matched but not assignable: ", Service.class.getName() );
	//				Log.write( Log.WARN, "This is usually due to a copy of service.jar in the module folder." );
	//			}
	//
	//			if( nameProductCard && !instanceofProductCard ) {
	//				Log.write( Log.WARN, "Class name matched but not assignable: ", ProductCard.class.getName() );
	//				Log.write( Log.WARN, "This is usually due to a copy of utility.jar in the module folder." );
	//			}
	//
	//			if( instanceofService && instanceofProductCard ) return constructor;
	//		}
	//
	//		throw new NoSuchMethodException( "Module constructor not found: " + JavaUtil.getClassName( moduleClass ) + "( " + JavaUtil.getClassName( Service.class ) + ", " + JavaUtil.getClassName( ProductCard.class ) + " )" );
	//	}

	private void registerProduct( Module module, boolean updatable, boolean removable ) {
		ProductCard card = module.getCard();

		// Register the product.
		registerProduct( module );

		// Add the module to the collection.
		modules.put( card.getProductKey(), module );

		// Set the enabled flag.
		setUpdatable( card, card.getCardUri() != null );
		setRemovable( card, true );
	}

	/**
	 * NOTE: This class is Persistent and changing the package will most likely result in a ClassNotFoundException being thrown at runtime.
	 *
	 * @author SoderquistMV
	 */
	static final class InstalledProduct implements Configurable {

		private Path target;

		private Settings settings;

		/*
		 * This constructor is used by the settings API via reflection.
		 */
		public InstalledProduct() {}

		public InstalledProduct( Path target ) {
			this.target = target;
		}

		public Path getTarget() {
			return target;
		}

		@Override
		public void setSettings( Settings settings ) {
			if( settings == null || this.settings != null ) return;

			this.settings = settings;

			String targetPath = settings.get( "target" );
			target = targetPath == null ? null : Paths.get( targetPath );
			settings.set( "target", target == null ? null : target.toString() );
		}

		@Override
		public Settings getSettings() {
			return settings;
		}

		@Override
		public String toString() {
			return target.toString();
		}

		@Override
		public int hashCode() {
			return target.toString().hashCode();
		}

		@Override
		public boolean equals( Object object ) {
			return object instanceof InstalledProduct && this.toString().equals( object.toString() );
		}

	}

	private final class SettingsChangeHandler implements SettingsListener {

		@Override
		public void handleEvent( SettingsEvent event ) {
			if( event.getType() != SettingsEvent.Type.CHANGED ) return;
			if( CHECK.equals( event.getKey() ) ) setCheckOption( CheckOption.valueOf( event.getNewValue().toString().toUpperCase() ) );
			if( event.getKey().startsWith( CHECK ) ) scheduleUpdateCheck( false );
		}

	}

	private static final class UpdateCheckTask extends TimerTask {

		private UpdateManager updateManager;

		UpdateCheckTask( UpdateManager updateManager ) {
			this.updateManager = updateManager;
		}

		@Override
		public void run() {
			updateManager.checkForUpdates();
		}

	}

	private static final class ProductState {

		boolean updatable;

		boolean removable;

		ProductState() {
			this.updatable = false;
			this.removable = false;
		}

	}

}
