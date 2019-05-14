package com.xeomar.xenon.update;

import com.xeomar.product.Product;
import com.xeomar.product.ProductCard;
import com.xeomar.settings.Settings;
import com.xeomar.settings.SettingsEvent;
import com.xeomar.settings.SettingsListener;
import com.xeomar.util.*;
import com.xeomar.xenon.*;
import com.xeomar.xenon.task.TaskFuture;
import com.xeomar.xenon.util.Lambda;
import javafx.application.Platform;
import org.slf4j.Logger;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.lang.module.Configuration;
import java.lang.module.ModuleFinder;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.*;

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
		SELECT,
		STORE,
		APPLY
	}

	public enum ApplyOption {
		VERIFY,
		IGNORE,
		RESTART
	}

	private static final Logger log = LogUtil.get( MethodHandles.lookup().lookupClass() );

	public static final String LAST_CHECK_TIME = "product-update-last-check-time";

	public static final String NEXT_CHECK_TIME = "product-update-next-check-time";

	private static final String MODULE_INSTALL_FOLDER_NAME = "modules";

	private static final String UPDATE_FOLDER_NAME = "updates";

	private static final String CHECK = "product-update-check";

	private static final String INTERVAL_UNIT = CHECK + "-interval-unit";

	private static final String SCHEDULE_WHEN = CHECK + "-schedule-when";

	private static final String SCHEDULE_HOUR = CHECK + "-schedule-hour";

	private static final String CHANNEL = "product-update-channel";

	private static final String NOTICE = "product-update-notice";

	private static final String FOUND = "product-update-found";

	private static final String APPLY = "product-update-apply";

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

	private Set<RepoCard> repos;

	private Map<String, Mod> modules;

	private Path homeModuleFolder;

	private Path userModuleFolder;

	private CheckOption checkOption;

	private FoundOption foundOption;

	private ApplyOption applyOption;

	private Map<String, Product> products;

	private Map<String, ProductCard> productCards;

	private Map<String, ProductUpdate> updates;

	private Map<String, ProductState> productStates;

	private Set<ProductCard> postedUpdateCache;

	private Set<ProductCard> includedProducts;

	private Set<ProductCard> availableCards;

	private long postedUpdateCacheTime;

	private Timer timer;

	private UpdateCheckTask task;

	private Set<ProductManagerListener> listeners;

	private long lastAvailableCheck;

	private RepoClient repoClient;

	private boolean productReposRegistered;

	public ProductManager( Program program ) {
		this.program = program;

		repos = new CopyOnWriteArraySet<>();
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
		includedProducts.add( new com.xeomar.zenna.Program().getCard() );
	}

	public Set<RepoCard> getRepos() {
		return new HashSet<>( repos );
	}

	public void registerProductRepos( List<RepoCard> repos ) {
		if( productReposRegistered ) return;
		productReposRegistered = true;
		this.repos.removeAll( repos );
		this.repos.addAll( repos );
		saveRepos();
	}

	public RepoCard addRepo( RepoCard repo ) {
		this.repos.add( repo );
		saveRepos();
		return repo;
	}

	public RepoCard removeRepo( RepoCard repo ) {
		this.repos.remove( repo );
		saveRepos();
		return repo;
	}

	public void setRepoEnabled( RepoCard catalog, boolean enabled ) {
		if( !repos.contains( catalog ) ) return;
		catalog.setEnabled( enabled );
		saveRepos();
	}

	/**
	 * Get the set of available modules for this product.
	 *
	 * @param force The user intentionally wants to load the available products
	 * @return A set of the available product cards
	 */
	public Set<ProductCard> getAvailableProducts( boolean force ) {
		if( !force && availableCards != null ) return availableCards;

		if( !force && System.currentTimeMillis() - lastAvailableCheck < 1000 ) return Set.of();
		lastAvailableCheck = System.currentTimeMillis();

		// FIXME Implement the following with a chainable task implementation

		// Download all the catalog cards
		log.warn( "Downloading catalog cards..." );
		Set<CatalogCard> catalogCards = repoClient.getCatalogCards( getRepos() );
		if( catalogCards.size() == 0 ) return Set.of();

		// Download all the product cards
		log.warn( "Downloading product cards..." );
		Set<ProductCard> cards = repoClient.getProductCards( catalogCards );

		return availableCards = cards;
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

	public Set<ProductCard> getProductCards() {
		return new HashSet<>( productCards.values() );
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

	private void registerProduct( Mod mod ) {
		// Treat mods like other products
		registerProduct( (Product)mod );

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

	public void installProducts( ProductCard... cards ) throws Exception {
		installProducts( Set.of( cards ) );
	}

	public void installProducts( Set<ProductCard> cards ) throws Exception {
		log.trace( "Number of products to install: " + cards.size() );
		program.getTaskManager().submit( new InstallProducts( program, cards ) );
	}

	public void uninstallProducts( ProductCard... cards ) throws Exception {
		uninstallProducts( Set.of( cards ) );
	}

	public void uninstallProducts( Set<ProductCard> cards ) throws Exception {
		log.trace( "Number of products to remove: " + cards.size() );
		program.getTaskManager().submit( new UninstallProducts( program, cards ) );
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
		return getProductCard( card ) != null;
	}

	public ProductCard getProductCard( ProductCard card ) {
		return productCards.get( card.getProductKey() );
	}

	/**
	 * Determines if a specific release of a product is installed.
	 *
	 * @param card
	 * @return
	 */
	public boolean isReleaseInstalled( ProductCard card ) {
		return isInstalled( card ) && getProductCard( card ).getRelease().equals( card.getRelease() );
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
		new ProductManagerEvent( this, ProductManagerEvent.Type.PRODUCT_CHANGED, card ).fire( listeners );
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
		new ProductManagerEvent( this, ProductManagerEvent.Type.PRODUCT_CHANGED, card ).fire( listeners );
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
	 * Schedule the update check task according to the settings. This method may safely be called as many times as necessary from any thread.
	 *
	 * @param startup True if the method is called at program start
	 */
	public synchronized void scheduleUpdateCheck( boolean startup ) {
		// If the program has not been updated and the UPDATE_IN_PROGRESS flag is
		// set, don't schedule update checks. This probably means there is a
		// problem applying an update. Otherwise, it should be safe to schedule
		// update checks.
		if( !program.isProgramUpdated() && program.getProgramParameters().isSet( ProgramFlag.UPDATE_IN_PROGRESS ) ) return;

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
		timer.schedule( task = new UpdateCheckTask( this ), delay );

		// Log the next update check time
		String date = DateUtil.format( new Date( nextCheckTime ), DateUtil.DEFAULT_DATE_FORMAT, DateUtil.LOCAL_TIME_ZONE );
		log.debug( "Next check scheduled for: " + (delay == 0 ? "now" : date) );
	}

	public void checkForUpdates() {
		if( !isEnabled() ) return;

		try {
			log.trace( "Checking for staged updates..." );
			int stagedUpdateCount = stagePostedUpdates();
			if( stagedUpdateCount > 0 ) {
				log.debug( "Staged updates found, restarting..." );
				program.requestUpdate( ProgramFlag.UPDATE_IN_PROGRESS );
			}
		} catch( Exception exception ) {
			log.error( "Error checking for updates", exception );
		}
	}

	/**
	 * Gets the set of posted product updates. If there are no posted updates found an empty set is returned.
	 *
	 * @return The set of posted updates.
	 * @throws ExecutionException If a task execution exception occurs
	 * @throws InterruptedException If the calling thread is interrupted
	 * @throws URISyntaxException If a URI cannot be resolved correctly
	 */
	public Set<ProductCard> findPostedUpdates( boolean force ) throws Exception {
		return new FindPostedUpdatesTask( program, force ).call();
	}

	void cacheSelectedUpdates( Set<ProductCard> packs ) throws Exception {
		// TODO Finish implementing ProductManager.cacheSelectedUpdates()
		throw new RuntimeException( "Method not implemented yet." );
	}

	void stageCachedUpdates( Set<ProductCard> packs ) throws Exception {
		// TODO Finish implementing ProductManager.stageCachedUpdates()
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
	public int stagePostedUpdates() throws Exception, ExecutionException, InterruptedException, URISyntaxException {
		if( !isEnabled() ) return 0;
		new StageUpdates( program, findPostedUpdates( false ) ).call();
		return updates.size();
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
	 * Apply updates. If updates are found then the method returns the number of updates applied.
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
				result = userApplyStagedUpdates();
			} catch( Exception exception ) {
				log.warn( "Failed to apply staged updates", exception );
			}
		} else {
			log.debug( "No staged updates detected." );
		}
		return result;
	}

	/**
	 * Launch the update program to apply the staged updates. This method is generally called when the program starts and, if the update program is successfully started, the program should be terminated to allow for the updates to be
	 * applied.
	 *
	 * @return The number of updates applied.
	 */
	public int userApplyStagedUpdates() {
		// The updates should already be staged at this point
		log.info( "Update manager enabled: " + isEnabled() );
		if( !isEnabled() || getStagedUpdateCount() == 0 ) return 0;

		log.info( "Starting update process..." );
		Platform.runLater( () -> program.requestUpdate() );
		return updates.size();
	}

	public void applySelectedUpdates( ProductCard update ) {
		applySelectedUpdates( Set.of( update ) );
	}

	public abstract void applySelectedUpdates( Set<ProductCard> updates );

	void clearStagedUpdates() {
		// Remove the updates settings
		updates.clear();
		saveUpdates( updates );
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

		if( "STAGE".equals( settings.get( FOUND, FoundOption.SELECT.name() ).toUpperCase() ) ) {
			settings.set( FOUND, FoundOption.APPLY.name().toLowerCase() );
		}

		this.checkOption = CheckOption.valueOf( settings.get( CHECK, CheckOption.MANUAL.name() ).toUpperCase() );
		this.foundOption = FoundOption.valueOf( settings.get( FOUND, FoundOption.SELECT.name() ).toUpperCase() );
		this.applyOption = ApplyOption.valueOf( settings.get( APPLY, ApplyOption.VERIFY.name() ).toUpperCase() );

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

	public void uiIsAvailable() {
		modules.values().forEach( this::callModCreate );
	}

	public void uiWillStop() {
		modules.values().forEach( this::callModDestroy );
	}

	@SuppressWarnings( "Convert2Diamond" )
	private void loadRepos() {
		// NOTE The TypeReference must have the parameterized type in it, the diamond operator cannot be used here
		repos.addAll( updateSettings.get( REPOS_SETTINGS_KEY, new TypeReference<Set<RepoCard>>() {}, repos ) );

		// Remove old repos
		repos.remove( new RepoCard( "https://xeomar.com/download/xenon/catalog/card/{0}" ) );
		repos.remove( new RepoCard( "https://xeomar.com/download" ) );

		// Force some values for normal repos
		repos.forEach( ( r ) -> {
			r.setRemovable( true );
			r.setRank( 0 );
		} );
	}

	private void saveRepos() {
		updateSettings.set( REPOS_SETTINGS_KEY, repos );
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

	private void doInstallMod( ProductCard card, Set<ProductResource> resources ) throws Exception {
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

	private void doRemoveMod( Mod mod ) {
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

	private void copyProductResources( Set<ProductResource> resources, Path folder ) throws IOException {
		if( resources == null ) return;

		for( ProductResource resource : resources ) {
			if( resource.getLocalFile() == null ) continue;
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

	private URI getSchemeResolvedUri( URI uri ) {
		if( uri == null ) return null;
		return uri.getScheme() == null ? Paths.get( uri.getPath() ).toUri() : uri;
	}

	@SuppressWarnings( "Convert2Diamond" )
	private Set<InstalledProduct> getStoredRemovedProducts() {
		return getSettings().get( REMOVES_SETTINGS_KEY, new TypeReference<Set<InstalledProduct>>() {}, Set.of() );
	}

	@Deprecated
	private Map<String, String> getProductParameters( ProductCard card, String type ) {
		Map<String, String> parameters = new HashMap<>();

		parameters.put( "artifact", card.getArtifact() );
		parameters.put( "category", "product" );
		parameters.put( "channel", getProductChannel() );
		parameters.put( "platform", OperatingSystem.getFamily().name().toLowerCase() );
		parameters.put( "type", type );

		return parameters;
	}

	public Path getHomeModuleFolder() {
		return homeModuleFolder;
	}

	public Path getUserModuleFolder() {
		return userModuleFolder;
	}

	private String getProductChannel() {
		String channel = settings.get( CHANNEL, "latest" );
		log.debug( "Using product channel: " + channel );
		return channel;
	}

	// TODO Each product could use a different channel
	// If not specified it should use the product channel

	private void loadModulePathMods() {
		ServiceLoader.load( Mod.class ).forEach( ( mod ) -> loadMod( mod, null ) );
	}

	private void loadStandardMods( Path source ) {
		// In this context module refers to Java modules and mod refers to program mods
		ModuleFinder moduleFinder = ModuleFinder.of( source );
		Configuration bootConfiguration = ModuleLayer.boot().configuration();
		Configuration moduleConfiguration = bootConfiguration.resolveAndBind( moduleFinder, ModuleFinder.of(), Set.of() );
		ModuleLayer moduleLayer = ModuleLayer.defineModulesWithOneLoader( moduleConfiguration, List.of( ModuleLayer.boot() ), null ).layer();
		ServiceLoader.load( moduleLayer, Mod.class ).forEach( ( mod ) -> loadMod( mod, source ) );
	}

	private void loadMod( Mod mod, Path source ) {
		ProductCard card = mod.getCard();

		// Ignore included products
		if( isIncludedProduct( card ) ) return;

		// Check if mod is already loaded
		if( getMod( card.getProductKey() ) != null ) return;

		// Initialize the mod
		mod.init( program, card );

		// Set the mod install folder
		card.setInstallFolder( source );

		// Register the product
		registerProduct( mod );

		log.debug( "Mod loaded: " + card.getProductKey() );
	}

	private void unloadMod( Mod mod ) {
		// Not sure what to do to unload a mod
	}

	final class FindPostedUpdatesTask extends ProgramTask<Set<ProductCard>> {

		private boolean force;

		private long postedCacheAge;

		private Set<ProductCard> oldCards;

		private Set<ProductCard> availableCards;

		private Set<TaskFuture<Download>> taskFutures;

		//private Map<ProductCard, DownloadTask> taskMap;

		private URISyntaxException uriSyntaxException;

		public FindPostedUpdatesTask( Program program, boolean force ) {
			super( program, program.getResourceBundle().getString( BundleKey.UPDATE, "task-updates-find-posted" ) );

			this.force = force;
			this.availableCards = new HashSet<>();
			this.postedCacheAge = System.currentTimeMillis() - postedUpdateCacheTime;

			// If the posted update cache is still valid no further setup needed
			if( !force && postedCacheAge < POSTED_UPDATE_CACHE_TIMEOUT ) return;

			// Update when the last update check occurred.
			getSettings().set( LAST_CHECK_TIME, System.currentTimeMillis() );

			// Schedule the next update check.
			scheduleUpdateCheck( false );

			// WORKAROUND This is part of a task pattern
			// This is part of a task pattern where this task is a collection of
			// download tasks are created and submitted to the task manager in the
			// constructor and processed in the call() method. This pattern gives a
			// nice user experience but is not obvious in the code. The pattern might
			// need to be extracted from the known implementations (3 as of this
			// writing).

			// Download the descriptors for each product.
			taskFutures = new HashSet<>();
			//taskMap = new HashMap<>();
			oldCards = getProductCards();

			// TODO For each repo, create download task for the installed products
			// This should result in a number of download tasks repos.size() *
			// products.size() that should be sent to the task manager. As all the
			// download tasks are processed in the call() method below, they may, or
			// may not, return a product card. Also, more than one product card may
			// end up getting returned if a product is hosted in more than one repo.

			for( ProductCard installedCard : oldCards ) {
				for( RepoCard repo : getRepos() ) {
					// FIXME This can be improved by using the repo catalogs
					// and only looking for products hosted by a repo
					URI uri = repoClient.getProductUri( repo, installedCard.getArtifact(), "product", "card" );
					DownloadTask<ProductCard> task = new DownloadTask( program, uri );
					task.setCarryOn( installedCard );
					taskFutures.add( program.getTaskManager().submit( task ) );
				}
			}
		}

		/**
		 * This method takes a set of download futures for product cards and
		 * determines what products can be updated.
		 *
		 * @param futures
		 * @return A map of the updateable product key to product card
		 */
		private Map<String, ProductCard> determineUpdateableVersions( Set<TaskFuture<Download>> futures ) {
			Map<String, ProductCard> cards = new HashMap<>();

			for( TaskFuture<Download> future : futures ) {
				DownloadTask<ProductCard> task = (DownloadTask)future.getTask();
				ProductCard currentProduct = task.getCarryOn();
				String key = currentProduct.getProductKey();

				try {
					ProductCard postedProduct;
					try( InputStream input = task.get().getInputStream() ) {
						postedProduct = new ProductCard().load( input, task.getUri() );
					} catch( IOException exception ) {
						log.warn( "Error loading product card: " + task.getUri(), exception );
						continue;
					}

					// We only want something in the result if the posted version is greater than the current version
					if( Version.compareVersions( postedProduct.getVersion(), currentProduct.getVersion() ) > 1 ) {
						// Determine the newer version if there is more than one posted version
						cards.compute( key, ( k, installedProduct ) -> {
							if( installedProduct != null && Version.compareVersions( installedProduct.getVersion(), postedProduct.getVersion() ) > 0 ) return installedProduct;
							if( installedProduct != null ) log.debug( "Installed: " + installedProduct.getProductKey() + " " + installedProduct.getRelease() );
							log.debug( "Available: " + postedProduct.getProductKey() + " " + postedProduct.getRelease() );
							return postedProduct;
						} );
					}
				} catch( ExecutionException exception ) {
					if( exception.getCause().getCause() instanceof FileNotFoundException ) {
						log.debug( "File not found: " + exception.getCause().getCause().getMessage() );
					} else {
						log.error( "Error downloading product card: " + key, exception );
					}
				} catch( InterruptedException exception ) {
					if( exception.getCause().getCause() instanceof FileNotFoundException ) ;
					log.warn( "Interrupted downloading product card: " + key );
				}
			}

			return cards;
		}

		public Set<ProductCard> call() throws Exception {
			if( !isEnabled() ) return availableCards;

			// If the posted update cache is still valid return the updates in the cache
			if( !force && postedCacheAge < POSTED_UPDATE_CACHE_TIMEOUT ) return new HashSet<>( postedUpdateCache );

			// Collect all the requested product cards
			Map<String, ProductCard> updateableProductVersions = determineUpdateableVersions( taskFutures );

			availableCards = new HashSet<>( updateableProductVersions.values() );

			//			// Determine what products have posted updates.
			//			ExecutionException executionException = null;
			//			InterruptedException interruptedException = null;
			//			for( ProductCard installedCard : oldCards ) {
			//				try {
			//					//					DownloadTask task = taskMap.get( installedCard );
			//					//					if( task == null ) continue;
			//
			//					ProductCard availableCard = updatableProductVersions.get( installedCard.getProductKey() );
			//					//					try( InputStream input = task.get().getInputStream() ) {
			//					//						availableCard = new ProductCard().load( input, task.getUri() );
			//					//					} catch( IOException exception ) {
			//					//						log.warn( "Error loading product card: " + task.getUri(), exception );
			//					//						continue;
			//					//					}
			//
			//					//					// Validate the pack key.
			//					//					if( !installedCard.getProductKey().equals( availableCard.getProductKey() ) ) {
			//					//						log.warn( "Pack mismatch: " + installedCard.getProductKey() + " != " + availableCard.getProductKey() );
			//					//						continue;
			//					//					}
			//
			//					log.debug( "Installed: " + installedCard.getProductKey() + " " + installedCard.getRelease() );
			//					log.debug( "Available: " + availableCard.getProductKey() + " " + availableCard.getRelease() );
			//
			//					if( availableCard.getRelease().compareTo( installedCard.getRelease() ) > 0 ) {
			//						log.debug( "Update found for: " + installedCard.getProductKey() + " > " + availableCard.getRelease() );
			//						availableCards.add( availableCard );
			//					}
			//
			//					// TODO Remove use of forced updates
			//					// Forced updates are used for development
			//					if( program.getExecMode() == ExecMode.DEV ) {
			//						log.debug( "Update forced for: " + installedCard.getProductKey() + " > " + availableCard.getRelease() );
			//						availableCards.add( availableCard );
			//					}
			//				} catch( ExecutionException exception ) {
			//					if( executionException == null ) executionException = exception;
			//				} catch( InterruptedException exception ) {
			//					if( interruptedException == null ) interruptedException = exception;
			//				}
			//			}

			// If there is an exception and there are no updates, throw the exception.
			//			if( availableCards.size() == 0 ) {
			//				if( uriSyntaxException != null ) throw uriSyntaxException;
			////				if( executionException != null ) throw executionException;
			////				if( interruptedException != null ) throw interruptedException;
			//			}

			// Cache the discovered updates.
			postedUpdateCacheTime = System.currentTimeMillis();
			postedUpdateCache = new CopyOnWriteArraySet<>( availableCards );

			return availableCards;
		}
	}

	private final class CreateUpdate extends ProgramTask<ProductUpdate> {

		private Set<ProductResource> resources;

		private ProductCard updateCard;

		private Path updatePack;

		CreateUpdate( Program program, ProductCard updateCard, Path updatePack ) {
			super( program, "Stage update: " + updateCard.getName() + " " + updateCard.getVersion() );
			resources = new HashSet<>();
			this.updateCard = updateCard;
			this.updatePack = updatePack;

			// Determine all the resources to download.
			try {
				URI codebase = updateCard.getProductUri( getProductParameters( updateCard, "card" ) );
				log.debug( "Resource codebase: " + codebase );
				PackProvider provider = new PackProvider( program, updateCard, getProductParameters( updateCard, "pack" ) );
				resources = provider.getResources( codebase );
				setTotal( resources.size() );

				log.debug( "Product resource count: " + resources.size() );

				for( ProductResource resource : resources ) {
					URI uri = getSchemeResolvedUri( resource.getUri() );
					log.debug( "Resource source: " + uri );

					// Submit download resource task
					resource.setFuture( program.getTaskManager().submit( new DownloadTask( program, uri ) ) );
				}
			} catch( URISyntaxException exception ) {
				log.error( "Error creating pack download", exception );
			}
		}

		@Override
		public ProductUpdate call() throws Exception {
			// Wait for all resources to be downloaded.
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

			// Verify the resources have all been staged successfully
			//Set<ProductResource> resources = productResources.get( updateCard );
			if( !areResourcesValid( resources ) ) {
				log.warn( "Update missing resources: " + updateCard );
				return null;
			}

			stageResources( updatePack, this::setProgress );

			Path installFolder = getProductInstallFolder( updateCard );
			if( isInstalled( updateCard ) ) installFolder = getProductCard( updateCard ).getInstallFolder();

			log.debug( "Update staged: " + updateCard.getProductKey() + " " + updateCard.getRelease() );
			log.debug( "           to: " + updatePack );

			// Notify listeners the update is staged.
			new ProductManagerEvent( ProductManager.this, ProductManagerEvent.Type.PRODUCT_STAGED, updateCard ).fire( listeners );

			return new ProductUpdate( updateCard, updatePack, installFolder );
		}

		private void stageResources( Path updatePack, LongCallback progressCallback ) throws IOException {
			// If there is only one resource and it is already an update pack then
			// just copy it. Otherwise, collect all packs and files into one zip
			// file as the update pack.
			if( resources.size() == 1 && resources.iterator().next().getType() == ProductResource.Type.PACK ) {
				Path file = resources.iterator().next().getLocalFile();
				setTotal( Files.size( file ) );
				FileUtil.copy( resources.iterator().next().getLocalFile(), updatePack, progressCallback );
			} else {
				// Collect everything into one zip file
				Path updateFolder = FileUtil.createTempFolder( "update", "folder" );
				copyProductResources( resources, updateFolder );
				setTotal( FileUtil.getDeepSize( updateFolder ) );
				FileUtil.zip( updateFolder, updatePack, progressCallback );
				FileUtil.deleteOnExit( updateFolder );
			}
		}

	}

	final class StageUpdates extends ProgramTask<Integer> {

		/**
		 * Attempt to stage the product packs described by the specified product cards.
		 *
		 * @param updateCards The set of update cards to stage
		 */
		Set<ProductCard> updateCards;

		private Set<Future<ProductUpdate>> updateFutures;

		public StageUpdates( Program program, Set<ProductCard> updateCards ) {
			super( program, program.getResourceBundle().getString( BundleKey.UPDATE, "task-updates-stage-selected" ) );
			this.updateCards = updateCards;

			Path stageFolder = program.getDataFolder().resolve( UPDATE_FOLDER_NAME );

			log.debug( "Number of packs to stage: " + updateCards.size() );
			log.trace( "Pack stage folder: " + stageFolder );

			try {
				Files.createDirectories( stageFolder );
			} catch( IOException exception ) {
				log.warn( "Error creating update stage folder: " + stageFolder, exception );
				return;
			}

			updateFutures = new HashSet<>();
			for( ProductCard card : updateCards ) {
				Path updatePack = stageFolder.resolve( getStagedUpdateFileName( card ) );
				updateFutures.add( program.getTaskManager().submit( new CreateUpdate( program, card, updatePack ) ) );
			}
		}

		@Override
		public Integer call() throws Exception {
			if( updateCards.size() == 0 ) return 0;

			for( Future<ProductUpdate> updateFuture : updateFutures ) {
				try {
					ProductUpdate update = updateFuture.get();

					// If the update is null then there was a problem creating the update locally
					if( update == null ) continue;

					ProductCard updateCard = update.getCard();

					// Verify the product is registered
					if( !isInstalled( updateCard ) ) {
						log.warn( "Product not registered: " + updateCard );
						continue;
					}

					// Verify the product is installed
					Path installFolder = getProductCard( updateCard ).getInstallFolder();
					boolean installFolderValid = installFolder != null && Files.exists( installFolder );
					if( !installFolderValid ) {
						log.warn( "Missing install folder: " + installFolder );
						log.warn( "Product not installed:  " + updateCard );
						continue;
					}

					// Remove any old staged updates for this product.
					updates.remove( update.getCard().getProductKey(), update );
					// Add the update to the set of staged updates.
					updates.put( update.getCard().getProductKey(), update );
				} catch( ExecutionException exception ) {
					log.error( "Error creating product update pack", exception );
				} catch( InterruptedException exception ) {
					break;
				}
			}

			program.getTaskManager().submit( Lambda.task( "Store staged update settings", () -> saveUpdates( updates ) ) );

			log.debug( "Product update count: " + updates.size() );

			return updates.size();
		}

	}

	/**
	 * This task is only applicable when a product is not already installed. If
	 * the product is already installed it should go through the update process.
	 */
	private final class InstallProducts extends ProgramTask<Integer> {

		/**
		 * Attempt to stage the product packs described by the specified product cards.
		 *
		 * @param updateCards The set of update cards to stage
		 */
		Set<ProductCard> updateCards;

		private Set<Future<ProductUpdate>> updateFutures;

		InstallProducts( Program program, Set<ProductCard> updateCards ) {
			super( program, program.getResourceBundle().getString( BundleKey.UPDATE, "task-updates-stage-selected" ) );
			this.updateCards = updateCards;

			log.debug( "Number of packs to stage: " + updateCards.size() );

			Path stageFolder = program.getDataFolder().resolve( UPDATE_FOLDER_NAME );
			log.trace( "Pack stage folder: " + stageFolder );

			try {
				Files.createDirectories( stageFolder );
			} catch( IOException exception ) {
				log.warn( "Error creating install stage folder: " + stageFolder, exception );
				return;
			}

			updateFutures = new HashSet<>();
			for( ProductCard card : updateCards ) {
				Path updatePack = stageFolder.resolve( getStagedUpdateFileName( card ) );
				updateFutures.add( program.getTaskManager().submit( new CreateUpdate( program, card, updatePack ) ) );
			}
		}

		@Override
		public Integer call() throws Exception {
			if( updateCards.size() == 0 ) return 0;

			Set<InstalledProduct> installedProducts = new HashSet<>();

			for( Future<ProductUpdate> updateFuture : updateFutures ) {
				try {
					ProductUpdate update = updateFuture.get();

					// If the update is null then there was a problem creating the update locally
					if( update == null ) continue;
					ProductCard card = update.getCard();

					log.debug( "Product downloaded: " + update.getCard().getProductKey() );

					// Install the products.
					try {
						ProductResource resource = new ProductResource( ProductResource.Type.PACK, update.getSource() );
						doInstallMod( card, Set.of( resource ) );
						installedProducts.add( new InstalledProduct( getProductInstallFolder( card ) ) );
					} catch( Exception exception ) {
						log.error( "Error installing: " + card, exception );
					}
				} catch( InterruptedException exception ) {
					break;
				} catch( Exception exception ) {
					log.error( "Error creating product install pack", exception );
				}
			}

			log.debug( "Product install count: " + installedProducts.size() );

			return installedProducts.size();
		}

	}

	private final class UninstallProducts extends ProgramTask<Integer> {

		private Set<ProductCard> cards;

		UninstallProducts( Program program, Set<ProductCard> cards ) {
			super( program, program.getResourceBundle().getString( BundleKey.UPDATE, "task-updates-remove-selected" ) );
			this.cards = cards;
		}

		@Override
		public Integer call() throws Exception {
			// Remove the products.
			Set<InstalledProduct> removedProducts = new HashSet<>();
			for( ProductCard card : cards ) {
				try {
					doRemoveMod( getMod( card.getProductKey() ) );
					removedProducts.add( new InstalledProduct( getProductInstallFolder( card ) ) );
				} catch( Exception exception ) {
					log.error( "Error uninstalling: " + card, exception );
				}
			}

			Set<InstalledProduct> products = new HashSet<>( getStoredRemovedProducts() );
			products.addAll( removedProducts );
			getSettings().set( REMOVES_SETTINGS_KEY, products );

			return removedProducts.size();
		}

	}

	private final class SettingsChangeHandler implements SettingsListener {

		@Override
		public void handleEvent( SettingsEvent event ) {
			if( event.getType() != SettingsEvent.Type.CHANGED ) return;

			String key = event.getKey();

			if( CHECK.equals( key ) ) setCheckOption( CheckOption.valueOf( event.getNewValue().toString().toUpperCase() ) );
			if( key.startsWith( CHECK ) ) scheduleUpdateCheck( false );
		}

	}

	/**
	 * NOTE This class is Persistent and changing the package will most likely
	 * result in a ClassNotFoundException being thrown at runtime.
	 */
	static final class InstalledProduct {

		private Path target;

		/*
		 * This constructor is used by the settings API via reflection.
		 */
		@SuppressWarnings( "unused" )
		public InstalledProduct() {}

		public InstalledProduct( Path target ) {
			this.target = target;
		}

		public Path getTarget() {
			return target;
		}

		public void setTarget( Path target ) {
			this.target = target;
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

	private static final class ProductState {

		boolean updatable;

		boolean removable;

		ProductState() {
			this.updatable = false;
			this.removable = false;
		}

	}

}
