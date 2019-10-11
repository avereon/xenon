package com.avereon.xenon.product;

import com.avereon.product.Product;
import com.avereon.product.ProductCard;
import com.avereon.product.RepoCard;
import com.avereon.settings.Settings;
import com.avereon.settings.SettingsEvent;
import com.avereon.settings.SettingsListener;
import com.avereon.util.*;
import com.avereon.xenon.Mod;
import com.avereon.xenon.Program;
import com.avereon.xenon.ProgramFlag;
import com.avereon.xenon.task.Task;
import com.avereon.xenon.task.TaskManager;
import com.avereon.xenon.util.Lambda;
import javafx.application.Platform;
import org.slf4j.Logger;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.lang.module.Configuration;
import java.lang.module.ModuleFinder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * The update manager handles discovery, staging and applying product updates.
 * <p>
 * Discovery involves checking for updates over the network (usually over the
 * Internet) and comparing the release information of installed packs with the
 * release information of the discovered packs. If the discovered pack is
 * determined to be newer than the installed pack it is considered an update.
 * <p>
 * Staging involves downloading new pack data and preparing it to be applied by
 * the update application.
 * <p>
 * Applying involves configuring and executing a separate update process to
 * apply the staged updates. This requires the calling process to terminate to
 * allow the update process to change required files.
 */
public abstract class ProductManager implements Controllable<ProductManager>, Configurable {

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
		NOTIFY,
		STORE,
		APPLY
	}

	private static final Logger log = LogUtil.get( MethodHandles.lookup().lookupClass() );

	public static final String LAST_CHECK_TIME = "product-update-last-check-time";

	public static final String NEXT_CHECK_TIME = "product-update-next-check-time";

	static final String UPDATE_FOLDER_NAME = "updates";

	private static final String MODULE_INSTALL_FOLDER_NAME = "modules";

	private static final String CHECK = "product-update-check";

	private static final String INTERVAL_UNIT = CHECK + "-interval-unit";

	private static final String SCHEDULE_WHEN = CHECK + "-schedule-when";

	private static final String SCHEDULE_HOUR = CHECK + "-schedule-hour";

	private static final String FOUND = "product-update-found";

	private static final String REPOS_SETTINGS_KEY = "repos";

	private static final String REMOVES_SETTINGS_KEY = "removes";

	private static final String UPDATES_SETTINGS_KEY = "updates";

	private static final String PRODUCT_ENABLED_KEY = "enabled";

	private static final int POSTED_UPDATE_CACHE_TIMEOUT = 60000;

	private static final int MILLIS_IN_HOUR = 3600000;

	private static final int NO_CHECK = -1;

	private Program program;

	private Settings settings;

	private Settings updateSettings;

	private Map<String, RepoState> providerRepos;

	private Map<String, RepoState> repos;

	private Map<String, Mod> modules;

	private Path homeModuleFolder;

	private Path userModuleFolder;

	private CheckOption checkOption;

	private FoundOption foundOption;

	private Map<String, Product> products;

	private Map<String, ProductCard> productCards;

	private Map<String, ProductUpdate> updates;

	private Map<String, ProductState> productStates;

	private Set<ProductCard> postedUpdateCache;

	private Set<ProductCard> includedProducts;

	private final Object availableProductsLock = new Object();

	private Set<ProductCard> availableProducts;

	private final Object availableUpdatesLock = new Object();

	private Set<ProductCard> availableUpdates;

	private long postedUpdateCacheTime;

	private final Object scheduleLock = new Object();

	private Timer timer;

	private UpdateCheckTask task;

	private Set<ProductManagerListener> listeners;

	private long lastAvailableProductCheck;

	private long lastAvailableUpdateCheck;

	private RepoClient repoClient;

	private boolean productReposRegistered;

	public ProductManager( Program program ) {
		this.program = program;

		repos = new ConcurrentHashMap<>();
		modules = new ConcurrentHashMap<>();
		updates = new ConcurrentHashMap<>();
		products = new ConcurrentHashMap<>();
		productCards = new ConcurrentHashMap<>();
		productStates = new ConcurrentHashMap<>();
		postedUpdateCache = new CopyOnWriteArraySet<>();
		listeners = new CopyOnWriteArraySet<>();

		repoClient = new V2RepoClient( program );

		// Register included products
		includedProducts = new HashSet<>();
		includedProducts.add( program.getCard() );
		includedProducts.add( new com.avereon.zenna.Program().getCard() );
	}

	public Set<RepoState> getRepos() {
		return new HashSet<>( repos.values() );
	}

	public void registerProviderRepos( Collection<RepoState> repos ) {
		if( providerRepos != null ) return;
		providerRepos = new ConcurrentHashMap<>();
		repos.forEach( ( repo ) -> providerRepos.put( repo.getInternalId(), repo ) );
	}

	public RepoCard addRepo( RepoCard repo ) {
		log.warn( "upsert repo=" + repo );
		this.repos.put( repo.getInternalId(), new RepoState( repo ) );
		saveRepos();
		return repo;
	}

	public RepoCard removeRepo( RepoCard repo ) {
		log.warn( "remove repo=" + repo );
		this.repos.remove( repo.getInternalId() );
		saveRepos();
		return repo;
	}

	public boolean isRepoEnabled( RepoCard repo ) {
		return repos.containsKey( repo.getInternalId() ) && repos.get( repo.getInternalId() ).isEnabled();
	}

	public void setRepoEnabled( RepoCard repo, boolean enabled ) {
		if( !repos.containsKey( repo.getInternalId() ) ) return;
		repos.get( repo.getInternalId() ).setEnabled( enabled );
		saveRepos();
	}

	/**
	 * Get the set of available modules for this product.
	 *
	 * @param force The user intentionally wants to load the available products
	 * @return A set of the available product cards
	 */
	public Collection<ProductCard> getAvailableProducts( boolean force ) {
		TaskManager.taskThreadCheck();
		synchronized( availableProductsLock ) {
			if( !force && availableProducts != null ) return new HashSet<>( availableProducts );

			if( !force && System.currentTimeMillis() - lastAvailableProductCheck < 1000 ) return Set.of();
			lastAvailableProductCheck = System.currentTimeMillis();

			try {
				availableProducts = new ProductManagerLogic( program )
					.getAvailableProducts( force )
					.get()
					.stream()
					.filter( ( card ) -> "mod".equals( card.getPackaging() ) )
					.collect( Collectors.toSet() );
				return new HashSet<>( availableProducts );
			} catch( Exception exception ) {
				// TODO Notify the user there was a problem refreshing available updates
				exception.printStackTrace();
			}

			return Set.of();
		}
	}

	public Set<Mod> getModules() {
		return new HashSet<>( modules.values() );
	}

	public Product getProduct( String productKey ) {
		return productKey == null ? program : products.get( productKey );
	}

	public Mod getMod( String productKey ) {
		return modules.get( productKey );
	}

	/**
	 * Get the product cards for the currently installed products including the
	 * program and all mods.
	 *
	 * @return A new set of currently installed product cards
	 */
	public Set<ProductCard> getInstalledProductCards( boolean force ) {
		// TODO The force flag could be used to refresh the installed product information
		return new HashSet<>( productCards.values() );
	}

	Map<String, ProductCard> getInstalledProductCardsMap() {
		return productCards;
	}

	public void registerProduct( Product product ) {
		ProductCard card = product.getCard();

		String productKey = card.getProductKey();
		products.put( productKey, product );
		productCards.put( productKey, card );
		productStates.put( productKey, new ProductState() );

		setUpdatable( card, true );
		setRemovable( card, false );
		setEnabled( card, true );
	}

	private void registerMod( Mod mod ) {
		// Treat mods like other products
		registerProduct( mod );

		ProductCard card = mod.getCard();

		// Add the mod to the collection
		modules.put( card.getProductKey(), mod );

		// Set the state flags
		setUpdatable( card, card.getProductUri() != null );
		setRemovable( card, true );
	}

	private void unregisterProduct( Product product ) {
		String productKey = product.getCard().getProductKey();
		products.remove( productKey );
		productCards.remove( productKey );
		productStates.remove( productKey );
	}

	private void unregisterProduct( Mod mod ) {
		ProductCard card = mod.getCard();

		// Remove the module.
		modules.remove( card.getProductKey() );

		// Treat mods like other products
		unregisterProduct( (Product)mod );
	}

	public Task<Collection<InstalledProduct>> installProducts( ProductCard... cards ) throws Exception {
		return installProducts( Set.of( cards ) );
	}

	public Task<Collection<InstalledProduct>> installProducts( Set<ProductCard> cards ) throws Exception {
		log.trace( "Number of products to install: " + cards.size() );
		return new ProductManagerLogic( program ).installProducts( cards );
	}

	public Task<Void> uninstallProducts( ProductCard... cards ) throws Exception {
		return uninstallProducts( Set.of( cards ) );
	}

	public Task<Void> uninstallProducts( Set<ProductCard> cards ) throws Exception {
		log.trace( "Number of products to remove: " + cards.size() );
		return new ProductManagerLogic( program ).uninstallProducts( cards );
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
		return getInstalledProductCard( card ) != null;
	}

	public ProductCard getInstalledProductCard( ProductCard card ) {
		return productCards.get( card.getProductKey() );
	}

	/**
	 * Determines if a specific release of a product is installed.
	 *
	 * @param card
	 * @return
	 */
	public boolean isReleaseInstalled( ProductCard card ) {
		return isInstalled( card ) && getInstalledProductCard( card ).getRelease().equals( card.getRelease() );
	}

	public ProductStatus getStatus( ProductCard card ) {
		return productStates.computeIfAbsent( card.getProductKey(), ( key ) -> new ProductState() ).getStatus();
	}

	public void setStatus( ProductCard card, ProductStatus status ) {
		productStates.computeIfAbsent( card.getProductKey(), ( key ) -> new ProductState() ).setStatus( status );
	}

	public boolean isUpdatable( ProductCard card ) {
		ProductState state = productStates.get( card.getProductKey() );
		return state != null && state.isUpdatable();
	}

	public void setUpdatable( ProductCard card, boolean updatable ) {
		if( isUpdatable( card ) == updatable ) return;
		ProductState state = productStates.get( card.getProductKey() );
		if( state == null ) return;

		state.setUpdatable( updatable );
		new ProductManagerEvent( this, ProductManagerEvent.Type.PRODUCT_CHANGED, card ).fire( listeners );
	}

	public boolean isRemovable( ProductCard card ) {
		ProductState state = productStates.get( card.getProductKey() );
		return state != null && state.isRemovable();
	}

	public void setRemovable( ProductCard card, boolean removable ) {
		if( isRemovable( card ) == removable ) return;
		ProductState state = productStates.get( card.getProductKey() );
		if( state == null ) return;

		state.setRemovable( removable );
		new ProductManagerEvent( this, ProductManagerEvent.Type.PRODUCT_CHANGED, card ).fire( listeners );
	}

	public ProductCard getProductUpdate( ProductCard card ) {
		ProductState state = productStates.get( card.getProductKey() );
		return state == null ? null : state.getUpdate();
	}

	public void setProductUpdate( ProductCard card, ProductCard update ) {
		productStates.computeIfAbsent( card.getProductKey(), ( key ) -> new ProductState() ).setUpdate( update );
	}

	public boolean isEnabled( ProductCard card ) {
		return program.getSettingsManager().getProductSettings( card ).get( PRODUCT_ENABLED_KEY, Boolean.class, false );
	}

	public void setEnabled( ProductCard card, boolean enabled ) {
		if( isEnabled( card ) == enabled ) return;

		Mod mod = getMod( card.getProductKey() );

		// Should be before after setting the enabled flag
		if( mod != null && !enabled ) callModDestroy( mod );

		Settings settings = program.getSettingsManager().getProductSettings( card );
		settings.set( PRODUCT_ENABLED_KEY, enabled );
		settings.flush();
		log.trace( "Set enabled: " + settings.getPath() + ": " + enabled );

		// Should be called after setting the enabled flag
		if( mod != null && enabled ) callModCreate( mod );

		new ProductManagerEvent( this, enabled ? ProductManagerEvent.Type.PRODUCT_ENABLED : ProductManagerEvent.Type.PRODUCT_DISABLED, card ).fire( listeners );
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

	public long getLastUpdateCheck() {
		return getSettings().get( LAST_CHECK_TIME, Long.class, 0L );
	}

	public long getNextUpdateCheck() {
		return getSettings().get( NEXT_CHECK_TIME, Long.class, 0L );
	}

	void updateLastCheckTime() {
		// Update when the last update check occurred.
		getSettings().set( LAST_CHECK_TIME, System.currentTimeMillis() );

		// Schedule the next update check.
		scheduleUpdateCheck( false );
	}

	/**
	 * Schedule the update check task according to the settings. This method may safely be called as many times as
	 * necessary from any thread.
	 *
	 * @param startup True if the method is called at program start
	 */
	public void scheduleUpdateCheck( boolean startup ) {
		synchronized( scheduleLock ) {
			// If the program has not been updated and the UPDATE_IN_PROGRESS flag is
			// set, don't schedule update checks. This probably means there is a
			// problem applying an update. Otherwise, it should be safe to schedule
			// update checks.
			if( !program.isProgramUpdated() && program.isUpdateInProgress() ) return;

			long now = System.currentTimeMillis();

			if( task != null ) {
				boolean alreadyRun = task.scheduledExecutionTime() < now;
				task.cancel();
				task = null;
				if( !alreadyRun ) log.trace( "Current check for updates task cancelled for new schedule." );
			}

			Settings checkSettings = getSettings();

			long lastUpdateCheck = getLastUpdateCheck();
			long nextUpdateCheck = getNextUpdateCheck();
			long delay = NO_CHECK;

			// This is ensures updates are not checked during tests
			if( TestUtil.isTest() ) checkOption = CheckOption.MANUAL;

			switch( checkOption ) {
				case STARTUP:
					if( startup ) delay = 0;
					break;
				case INTERVAL: {
					CheckInterval intervalUnit = CheckInterval.valueOf( checkSettings.get( INTERVAL_UNIT, CheckInterval.DAY.name() ).toUpperCase() );
					delay = getNextIntervalDelay( now, intervalUnit, lastUpdateCheck );
					if( nextUpdateCheck < (now - 1000) ) delay = 0;
					break;
				}
				case SCHEDULE: {
					CheckWhen scheduleWhen = CheckWhen.valueOf( checkSettings.get( SCHEDULE_WHEN, CheckWhen.DAILY.name() ).toUpperCase() );
					int scheduleHour = checkSettings.get( SCHEDULE_HOUR, Integer.class, 0 );
					delay = getNextScheduleDelay( now, scheduleWhen, scheduleHour );
					if( nextUpdateCheck < (now - 1000) ) delay = 0;
					break;
				}
			}

			if( delay == NO_CHECK ) {
				checkSettings.set( NEXT_CHECK_TIME, 0 );
				log.debug( "Future update check not scheduled." );
				return;
			}

			// Set the next update check time before scheduling the
			// task to prevent this method from looping rapidly
			long nextCheckTime = now + delay;
			checkSettings.set( NEXT_CHECK_TIME, nextCheckTime );

			// Schedule the update check task
			timer.schedule( task = new UpdateCheckTask( this ), delay < 0 ? 0 : delay );

			// Log the next update check time
			String date = DateUtil.format( new Date( nextCheckTime ), DateUtil.DEFAULT_DATE_FORMAT, DateUtil.LOCAL_TIME_ZONE );
			log.debug( "Next check scheduled for: " + (delay == 0 ? "now" : date) );
		}
	}

	/**
	 * Check for updates for all installed products
	 */
	public void checkForUpdates() {
		if( !isEnabled() ) return;

		try {
			log.trace( "Checking for staged updates..." );
			new ProductManagerLogic( program ).stageAndApplyUpdates( getInstalledProductCards( false ), false );
		} catch( Exception exception ) {
			log.error( "Error checking for updates", exception );
		}
	}

	public void checkForStagedUpdatesAtStart() {
		if( program.getHomeFolder() == null ) {
			log.warn( "Program not running from updatable location." );
			return;
		}

		log.trace( "Checking for staged updates..." );

		// If updates are staged, apply them.
		int updateCount = getStagedUpdateCount();
		if( updateCount > 0 ) {
			log.info( "Staged updates detected: {}", updateCount );
			try {
				applyStagedUpdatesAtStart();
			} catch( Exception exception ) {
				log.warn( "Failed to apply staged updates", exception );
			}
		} else {
			log.debug( "No staged updates detected." );
		}
	}

	public void applyStagedUpdatesAtStart() {
		applyStagedUpdates();
	}

	/**
	 * Gets the set of available product updates. If there are no posted updates
	 * found an empty set is returned.
	 *
	 * @return The set of available updates.
	 */
	public Collection<ProductCard> findAvailableUpdates( boolean force ) {
		TaskManager.taskThreadCheck();
		synchronized( availableUpdatesLock ) {
			if( !force && availableUpdates != null ) return new HashSet<>( availableUpdates );

			if( !force && System.currentTimeMillis() - lastAvailableUpdateCheck < 1000 ) return Set.of();
			lastAvailableUpdateCheck = System.currentTimeMillis();

			try {
				availableUpdates = new ProductManagerLogic( program ).findPostedUpdates( force ).get();
				return new HashSet<>( availableUpdates );
			} catch( Exception exception ) {
				// TODO Notify the user there was a problem refreshing available updates
				exception.printStackTrace();
			}

			return Set.of();
		}
	}

	void storeSelectedUpdates( Set<ProductCard> packs ) throws Exception {
		// TODO Finish implementing ProductManager.storeSelectedUpdates()
		throw new RuntimeException( "Method not implemented yet." );
	}

	void applyStoredUpdates( Set<ProductCard> packs ) throws Exception {
		// TODO Finish implementing ProductManager.applyStoredUpdates()
		throw new RuntimeException( "Method not implemented yet." );
	}

	public Path getProductInstallFolder( ProductCard card ) {
		return getUserModuleFolder().resolve( card.getGroup() + "." + card.getArtifact() );
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
				updates.remove( update.getCard().getProductKey(), update );
			}
			saveUpdates( updates );
		}

		return staged;
	}

	void setStagedUpdates( Collection<ProductUpdate> updates ) {
		Map<String, ProductUpdate> updateMap = new ConcurrentHashMap<>();

		for( ProductUpdate update : updates ) {
			updateMap.put( update.getCard().getProductKey(), update );
		}

		this.updates.clear();
		this.updates.putAll( updateMap );

		saveUpdates( this.updates );
	}

	int getStagedUpdateCount() {
		return getStagedUpdates().size();
	}

	public boolean isUpdateStaged( ProductCard card ) {
		for( ProductUpdate update : getStagedUpdates() ) {
			if( card.equals( update.getCard() ) ) return true;
		}
		return false;
	}

	public boolean isSpecificUpdateReleaseStaged( ProductCard card ) {
		ProductUpdate update = updates.get( card.getProductKey() );
		if( update == null ) return false;

		ProductCard internal = update.getCard();
		return internal != null && internal.getRelease().equals( card.getRelease() );
	}

	/**
	 * Launch the update program to apply the staged updates. This method is generally called when the program starts and,
	 * if the update program is successfully started, the program should be terminated to allow for the updates to be
	 * applied.
	 *
	 * @return The number of updates applied.
	 */
	public int applyStagedUpdates() {
		log.info( "Update manager enabled: " + isEnabled() );
		if( !isEnabled() ) return 0;

		int count = getStagedUpdates().size();
		if( count > 0 ) Platform.runLater( () -> program.requestUpdate() );

		return count;
	}

	public Task<Collection<ProductUpdate>> applySelectedUpdates( ProductCard update ) {
		return applySelectedUpdates( Set.of( update ) );
	}

	public abstract Task<Collection<ProductUpdate>> applySelectedUpdates( Set<ProductCard> updates );

	void clearStagedUpdates() {
		// Remove the updates settings
		updates.clear();
		saveUpdates( updates );
	}

	Void saveRemovedProducts( Collection<InstalledProduct> removedProducts ) {
		Set<InstalledProduct> products = new HashSet<>( program.getProductManager().getStoredRemovedProducts() );
		products.addAll( removedProducts );
		getSettings().set( REMOVES_SETTINGS_KEY, products );
		return null;
	}

	public static boolean areResourcesValid( Set<ProductResource> resources ) {
		for( ProductResource resource : resources ) {
			if( !resource.isValid() ) return false;
		}

		return true;
	}

	public static long getNextIntervalDelay( long currentTime, CheckInterval intervalUnit, long lastUpdateCheck ) {
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

		return (lastUpdateCheck + intervalDelay) - currentTime;
	}

	public static long getNextScheduleDelay( long currentTime, CheckWhen scheduleWhen, int scheduleHour ) {
		Calendar calendar = new GregorianCalendar( TimeZone.getDefault() );

		// Calculate the next update check.
		calendar.setTimeInMillis( currentTime );
		calendar.set( Calendar.HOUR_OF_DAY, scheduleHour );
		calendar.set( Calendar.MINUTE, 0 );
		calendar.set( Calendar.SECOND, 0 );
		calendar.set( Calendar.MILLISECOND, 0 );
		if( scheduleWhen != CheckWhen.DAILY ) calendar.set( Calendar.DAY_OF_WEEK, scheduleWhen.ordinal() );

		long delay = calendar.getTimeInMillis() - currentTime;

		// If past the scheduled time, add a day or week.
		if( delay < 0 ) {
			if( scheduleWhen == CheckWhen.DAILY ) {
				delay += 24 * MILLIS_IN_HOUR;
			} else {
				delay += 7 * 24 * MILLIS_IN_HOUR;
			}
		}

		return delay;
	}

	public Set<ProductManagerListener> getProductManagerListeners() {
		return new HashSet<>( listeners );
	}

	public void addProductManagerListener( ProductManagerListener listener ) {
		listeners.add( listener );
	}

	public void removeProductManagerListener( ProductManagerListener listener ) {
		listeners.remove( listener );
	}

	@Override
	public void setSettings( Settings settings ) {
		if( settings == null || this.settings != null ) return;

		// FIXME The settings passed in serve two purposes (config and updates) but should not

		// What are these settings if the update node is retrieved below?
		this.settings = settings;

		if( "STAGE".equals( settings.get( FOUND, FoundOption.NOTIFY.name() ).toUpperCase() ) ) {
			settings.set( FOUND, FoundOption.APPLY.name().toLowerCase() );
		}

		this.checkOption = CheckOption.valueOf( settings.get( CHECK, CheckOption.MANUAL.name() ).toUpperCase() );
		this.foundOption = FoundOption.valueOf( settings.get( FOUND, FoundOption.NOTIFY.name() ).toUpperCase() );

		// FIXME These settings are apparently used to store the catalogs and updates
		// Maybe the catalogs and updates should be stored in a different location
		this.updateSettings = settings.getNode( "update" );

		// Load the product catalogs
		loadRepos();

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
		if( timer == null ) return false;

		try {
			// This attempts to schedule a noop task for immediate execution
			timer.schedule( Lambda.timerTask( () -> {} ), 0 );
		} catch( IllegalStateException exception ) {
			return false;
		}

		return true;
	}

	@Override
	public ProductManager start() {
		purgeRemovedProducts();

		getSettings().addSettingsListener( new SettingsChangeHandler() );

		// Create the update check timer.
		timer = new Timer( true );

		// Define the module folders.
		homeModuleFolder = program.getHomeFolder().resolve( MODULE_INSTALL_FOLDER_NAME );
		userModuleFolder = program.getDataFolder().resolve( MODULE_INSTALL_FOLDER_NAME );

		// Create the default module folders list.
		List<Path> moduleFolders = new ArrayList<>();
		moduleFolders.add( getHomeModuleFolder() );
		moduleFolders.add( getUserModuleFolder() );

		// Check for module paths in the parameters.
		List<String> modulePaths = program.getProgramParameters().getValues( "module" );
		if( modulePaths != null ) {
			for( String path : modulePaths ) {
				Path folder = Paths.get( path );
				if( Files.exists( folder ) ) moduleFolders.add( folder );
			}
		}

		// Load the modules
		loadModules( moduleFolders.toArray( new Path[ 0 ] ) );

		// Allow the mods to register resources
		modules.values().forEach( this::callModRegister );

		return this;
	}

	@Override
	public ProductManager awaitStart( long timeout, TimeUnit unit ) throws InterruptedException {
		return this;
	}

	@Override
	public ProductManager restart() {
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
	public ProductManager awaitRestart( long timeout, TimeUnit unit ) throws InterruptedException {
		return this;
	}

	@Override
	public ProductManager stop() {
		modules.values().forEach( this::callModUnregister );

		if( timer != null ) timer.cancel();
		timer = null;
		return this;
	}

	@Override
	public ProductManager awaitStop( long timeout, TimeUnit unit ) throws InterruptedException {
		return this;
	}

	public void startMods() {
		modules.values().forEach( this::callModCreate );
	}

	public void stopMods() {
		modules.values().forEach( this::callModDestroy );
	}

	@SuppressWarnings( "Convert2Diamond" )
	private void loadRepos() {
		// NOTE The TypeReference must have the parameterized type in it, the diamond operator cannot be used here
		Set<RepoState> repoStates = updateSettings.get( REPOS_SETTINGS_KEY, new TypeReference<Set<RepoState>>() {}, new HashSet<>() );
		repoStates.forEach( ( state ) -> repos.put( state.getInternalId(), state ) );

		// Remove old repos
		//repos.remove( "https://avereon.com/download/stable" );
		//repos.remove( "https://avereon.com/download/latest" );

		repos.values().forEach( ( repo ) -> {
			if( providerRepos.containsKey( repo.getInternalId() ) ) {
				// Keep some values for provider repos
				boolean enabled = repo.isEnabled();
				repo.copyFrom( providerRepos.get( repo.getInternalId() ) );
				repo.setEnabled( enabled );
			} else {
				// Force some values for normal repos
				repo.setRemovable( true );
				repo.setRank( 0 );
			}
		} );

		providerRepos.keySet().forEach( ( id ) -> repos.putIfAbsent( id, providerRepos.get( id ) ) );

		saveRepos();
	}

	private void saveRepos() {
		updateSettings.set( REPOS_SETTINGS_KEY, repos.values() );
	}

	@SuppressWarnings( "Convert2Diamond" )
	private void loadUpdates() {
		// NOTE The TypeReference must have the parameterized type in it, the diamond operator cannot be used here
		updates = updateSettings.get( UPDATES_SETTINGS_KEY, new TypeReference<Map<String, ProductUpdate>>() {}, updates );
	}

	private void saveUpdates( Map<String, ProductUpdate> updates ) {
		updateSettings.set( UPDATES_SETTINGS_KEY, updates );
	}

	private boolean isIncludedProduct( ProductCard card ) {
		return includedProducts.contains( card );
	}

	private void loadModules( Path... folders ) {
		// Look for mods on the module path
		try {
			loadModulePathMods();
		} catch( Exception exception ) {
			log.error( "Error loading modules from module path", exception );
		}

		// Look for standard mods (most common)
		Arrays.stream( folders ).filter( ( f ) -> Files.exists( f ) ).filter( ( f ) -> Files.isDirectory( f ) ).forEach( ( folder ) -> {
			try {
				Files.list( folder ).filter( ( path ) -> Files.isDirectory( path ) ).forEach( this::loadStandardMods );
			} catch( IOException exception ) {
				log.error( "Error loading modules from: " + folder, exception );
			}
		} );
	}

	void doInstallMod( ProductCard card, Set<ProductResource> resources ) throws Exception {
		Path installFolder = getProductInstallFolder( card );

		log.debug( "Install product to: " + installFolder );

		// Install all the resource files to the install folder
		copyProductResources( resources, installFolder );

		// Load the mods
		loadModules( getUserModuleFolder() );

		// Allow the mod to register resources
		callModRegister( getMod( card.getProductKey() ) );

		// Set the enabled state
		setEnabled( card, true );

		// Notify listeners of install
		new ProductManagerEvent( ProductManager.this, ProductManagerEvent.Type.PRODUCT_INSTALLED, card ).fire( listeners );
	}

	void doRemoveMod( Mod mod ) {
		ProductCard card = mod.getCard();

		Path installFolder = card.getInstallFolder();

		log.debug( "Remove product from: " + installFolder );

		// Disable the product
		setEnabled( card, false );

		callModUnregister( mod );

		unloadMod( mod );

		// Remove the product from the manager
		unregisterProduct( mod );

		// Remove the product settings
		program.getSettingsManager().getProductSettings( card ).delete();

		// Notify listeners of remove
		new ProductManagerEvent( ProductManager.this, ProductManagerEvent.Type.PRODUCT_REMOVED, card ).fire( listeners );
	}

	private void callModRegister( Mod mod ) {
		try {
			mod.register();
		} catch( Throwable throwable ) {
			log.error( "Error registering mod: " + mod.getCard().getProductKey(), throwable );
		}
	}

	private void callModCreate( Mod mod ) {
		if( !isEnabled( mod.getCard() ) ) return;
		try {
			mod.startup();
		} catch( Throwable throwable ) {
			log.error( "Error starting mod: " + mod.getCard().getProductKey(), throwable );
		}
	}

	private void callModDestroy( Mod mod ) {
		if( !isEnabled( mod.getCard() ) ) return;
		try {
			mod.shutdown();
		} catch( Throwable throwable ) {
			log.error( "Error stopping mod: " + mod.getCard().getProductKey(), throwable );
		}
	}

	private void callModUnregister( Mod mod ) {
		try {
			mod.unregister();
		} catch( Throwable throwable ) {
			log.error( "Error unregistering mod: " + mod.getCard().getProductKey(), throwable );
		}
	}

	private void purgeRemovedProducts() {
		// Check for products marked for removal and remove the files.
		Set<InstalledProduct> products = getStoredRemovedProducts();
		for( InstalledProduct product : products ) {
			log.debug( "Purging: " + product );
			try {
				FileUtil.delete( product.getTarget() );
			} catch( IOException exception ) {
				log.error( "Error removing product: " + product, exception );
			}
		}
		getSettings().remove( REMOVES_SETTINGS_KEY );
	}

	void copyProductResources( Set<ProductResource> resources, Path folder ) throws IOException {
		if( resources == null ) return;

		for( ProductResource resource : resources ) {
			if( resource.getLocalFile() == null ) throw new ProductResourceMissingException( "Local file not found", resource );
			switch( resource.getType() ) {
				case FILE: {
					// Just copy the file
					FileUtil.copy( resource.getLocalFile(), folder.resolve( resource.getName() ) );
					break;
				}
				case PACK: {
					// Unpack the file
					FileUtil.unzip( resource.getLocalFile(), folder );
					break;
				}
			}
		}
	}

	@SuppressWarnings( "Convert2Diamond" )
	private Set<InstalledProduct> getStoredRemovedProducts() {
		return getSettings().get( REMOVES_SETTINGS_KEY, new TypeReference<Set<InstalledProduct>>() {}, Set.of() );
	}

	public Path getHomeModuleFolder() {
		return homeModuleFolder;
	}

	public Path getUserModuleFolder() {
		return userModuleFolder;
	}

	private void loadModulePathMods() {
		ServiceLoader.load( Mod.class ).forEach( ( mod ) -> loadMod( mod, null ) );
	}

	private void loadStandardMods( Path source ) {
		// In this context module refers to Java modules and mod refers to program mods
		ModuleLayer bootModuleLayer = ModuleLayer.boot();
		Configuration bootConfiguration = bootModuleLayer.configuration();
		ModuleFinder moduleFinder = ModuleFinder.of( source );
		Configuration moduleConfiguration = bootConfiguration.resolveAndBind( ModuleFinder.of(), moduleFinder, Set.of() );
		ModuleLayer moduleLayer = ModuleLayer.defineModulesWithOneLoader( moduleConfiguration, List.of( bootModuleLayer ), null ).layer();
		ServiceLoader.load( moduleLayer, Mod.class ).forEach( ( mod ) -> loadMod( mod, source ) );
	}

	private void loadMod( Mod mod, Path source ) {
		ProductCard card = mod.getCard();
		log.debug( "Loading mod: " + card.getProductKey() + " from " + (source == null ? "classpath" : source) );

		// Ignore included products
		if( isIncludedProduct( card ) ) return;

		// Check if mod is already loaded
		if( getMod( card.getProductKey() ) != null ) {
			log.warn( "Mod already loaded: " + card.getProductKey() );
			return;
		}

		// Initialize the mod
		mod.init( program, card );

		// Set the mod install folder
		card.setInstallFolder( source );

		// Register the mod
		registerMod( mod );

		log.debug( "Mod loaded: " + card.getProductKey() + " from " + (source == null ? "classpath" : source) );
	}

	private void unloadMod( Mod mod ) {
		// Not sure what to do to unload a mod
	}

	private final class SettingsChangeHandler implements SettingsListener {

		@Override
		public void handleEvent( SettingsEvent event ) {
			if( event.getType() != SettingsEvent.Type.CHANGED ) return;

			String key = event.getKey();

			switch( key ) {
				case CHECK: {
					setCheckOption( CheckOption.valueOf( event.getNewValue().toString().toUpperCase() ) );
					scheduleUpdateCheck( false );
					break;
				}
				case FOUND: {
					setFoundOption( FoundOption.valueOf( event.getNewValue().toString().toUpperCase() ) );
					break;
				}
			}
		}

	}

	private static final class UpdateCheckTask extends TimerTask {

		private ProductManager productManager;

		UpdateCheckTask( ProductManager productManager ) {
			this.productManager = productManager;
		}

		@Override
		public void run() {
			productManager.checkForUpdates();
		}

	}

}
