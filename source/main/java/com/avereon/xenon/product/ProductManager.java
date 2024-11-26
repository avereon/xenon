package com.avereon.xenon.product;

import com.avereon.event.EventHandler;
import com.avereon.log.LazyEval;
import com.avereon.log.Log;
import com.avereon.product.*;
import com.avereon.settings.Settings;
import com.avereon.settings.SettingsEvent;
import com.avereon.skill.Controllable;
import com.avereon.util.DateUtil;
import com.avereon.util.FileUtil;
import com.avereon.util.LogFlag;
import com.avereon.util.TypeReference;
import com.avereon.weave.Weave;
import com.avereon.xenon.Module;
import com.avereon.xenon.*;
import com.avereon.xenon.task.Task;
import com.avereon.xenon.task.TaskManager;
import com.avereon.xenon.util.Lambda;
import com.avereon.zarra.event.FxEventHub;
import com.avereon.zarra.javafx.Fx;
import lombok.CustomLog;
import lombok.Getter;

import java.io.IOException;
import java.lang.module.Configuration;
import java.lang.module.ModuleFinder;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * The update manager handles discovery, staging and applying product updates.
 * <p>
 * Discovery involves checking for updates over the network (usually over the Internet) and comparing the release information of installed packs with the release information of the discovered packs.
 * If the discovered pack is determined to
 * be newer than the installed pack it is considered an update.
 * <p>
 * Staging involves downloading new pack data and preparing it to be applied by the update application.
 * <p>
 * Applying involves configuring and executing a separate update process to apply the staged updates. This requires the calling process to terminate to allow the update process to change required
 * files.
 */
@CustomLog
public class ProductManager implements Controllable<ProductManager> {

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
		APPLY,
		NOTIFY,
		STORE
	}

	public static final String LAST_CHECK_TIME = "product-update-last-check-time";

	public static final String NEXT_CHECK_TIME = "product-update-next-check-time";

	private static final String UPDATE_FOLDER_NAME = "updates";

	private static final String MODULE_INSTALL_FOLDER_NAME = "modules";

	static final String CHECK = "product-update-check";

	static final String INTERVAL_UNIT = CHECK + "-interval-unit";

	static final String SCHEDULE_WHEN = CHECK + "-schedule-when";

	static final String SCHEDULE_HOUR = CHECK + "-schedule-hour";

	private static final String FOUND = "product-update-found";

	private static final String REPOS_SETTINGS_KEY = "repos";

	private static final String REMOVES_SETTINGS_KEY = "removes";

	private static final String UPDATES_SETTINGS_KEY = "updates";

	private static final String PRODUCT_ENABLED_KEY = "enabled";

	private static final int POSTED_UPDATE_CACHE_TIMEOUT = 60000;

	private static final int MILLIS_IN_HOUR = 3600000;

	private static final long NO_CHECK = Long.MIN_VALUE;

	private Xenon program;

	private Map<String, RepoState> providerRepos;

	private Map<String, RepoState> repos;

	private Map<String, Module> modules;

	private Path homeModuleFolder;

	private Path userModuleFolder;

	@Getter
	private CheckOption checkOption;

	@Getter
	private FoundOption foundOption;

	private Map<String, Product> products;

	private Map<String, ProductCard> productCards;

	private Map<String, ProductUpdate> updates;

	private Map<String, ProductState> productStates;

	private Set<ProductCard> postedUpdateCache;

	private Set<ProductCard> includedProducts;

	private final Object updateProviderReposLock = new Object();

	private final Object availableProductsLock = new Object();

	private Set<ProductCard> availableProducts;

	private final Object availableUpdatesLock = new Object();

	private Set<ProductCard> availableUpdates;

	private long postedUpdateCacheTime;

	private final Object scheduleLock = new Object();

	private Timer timer;

	private UpdateCheckTask task;

	@Getter
	private final FxEventHub eventBus;

	private long lastProviderRepoCheck;

	private long lastAvailableProductCheck;

	private long lastAvailableUpdateCheck;

	//private final RepoClient repoClient;

	//private boolean productReposRegistered;

	public ProductManager( Xenon program ) {
		this.program = program;

		repos = new ConcurrentHashMap<>();
		modules = new ConcurrentHashMap<>();
		updates = new ConcurrentHashMap<>();
		products = new ConcurrentHashMap<>();
		productCards = new ConcurrentHashMap<>();
		productStates = new ConcurrentHashMap<>();
		postedUpdateCache = new CopyOnWriteArraySet<>();
		eventBus = new FxEventHub();

		//repoClient = new V2RepoClient( program );

		// Register included products
		includedProducts = new HashSet<>();
		includedProducts.add( program.getCard() );
		includedProducts.add( new Weave().getCard() );

		getEventBus().parent( program.getFxEventHub() );
		registerProgram( program );
		try {
			registerProviderRepos( RepoState.forProduct( getClass() ) );
		} catch( IOException exception ) {
			log.atError().withCause( exception ).log( "Error loading program repos" );
		}
		loadSettings();
	}

	private Xenon getProgram() {
		return program;
	}

	public Set<RepoState> getRepos() {
		return getRepos( false );
	}

	public Set<RepoState> getRepos( boolean force ) {
		TaskManager.taskThreadCheck();

		synchronized( updateProviderReposLock ) {
			if( !force && repos != null ) return new HashSet<>( repos.values() );

			if( !force && System.currentTimeMillis() - lastProviderRepoCheck < 1000 ) return Set.of();
			lastProviderRepoCheck = System.currentTimeMillis();

			try {
				repos.values().forEach( this::updateRepo );
			} catch( Exception exception ) {
				log.atError().withCause( exception ).log( "Error getting available products" );
			}
		}

		return new HashSet<>( repos.values() );
	}

	public void updateRepo( RepoCard repo ) {
		TaskManager.taskThreadCheck();

		try {
			URI uri = URI.create( repo.getUrl() + "/catalog" );
			DownloadTask task = new DownloadTask( getProgram(), uri );
			Future<Download> future = getProgram().getTaskManager().submit( task );
			RepoState source = repos.get( repo.getInternalId() );
			source.copyFrom( CatalogCard.fromJson( future.get().getInputStream() ) );
			repo.copyFrom( source );
			saveRepos();
		} catch( Exception exception ) {
			log.atWarning().withCause( exception ).log( "Error loading repository metadata" );
		}
	}

	public void registerProviderRepos( Collection<RepoState> repos ) {
		if( providerRepos != null ) return;
		providerRepos = new ConcurrentHashMap<>();
		repos.forEach( ( repo ) -> providerRepos.put( repo.getInternalId(), repo ) );
	}

	public RepoCard addRepo( RepoCard repo ) {
		log.atWarn().log( "upsert repo=%s", repo );
		this.repos.put( repo.getInternalId(), new RepoState( repo ) );
		saveRepos();
		return repo;
	}

	public RepoCard removeRepo( RepoCard repo ) {
		log.atWarn().log( "remove repo=%s", repo );
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

	public void showPostedUpdates() {
		new ProductManagerLogic( getProgram() ).showPostedUpdates();
	}

	public void showStagedUpdates() {
		new ProductManagerLogic( getProgram() ).showStagedUpdates();
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
				availableProducts = new ProductManagerLogic( getProgram() )
					.getAvailableProducts( force )
					.get()
					.stream()
					.filter( ( card ) -> "mod".equals( card.getPackaging() ) )
					.collect( Collectors.toSet() );
				return new HashSet<>( availableProducts );
			} catch( Exception exception ) {
				log.atError().withCause( exception ).log( "Error getting available products" );
			}

			return Set.of();
		}
	}

	public Set<Module> getModules() {
		return new HashSet<>( modules.values() );
	}

	public Product getProduct( String productKey ) {
		return productKey == null ? getProgram() : products.get( productKey );
	}

	public Set<String> getModKeys() {
		return modules.keySet();
	}

	public Module getMod( String productKey ) {
		return modules.get( productKey );
	}

	/**
	 * Get the product cards for the currently installed products including the program and all mods.
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

	private void registerProgram( Xenon program ) {
		registerProduct( program );
		ProductCard card = program.getCard();
		setUpdatable( card, true );
		setRemovable( card, false );
	}

	private void registerProduct( Product product ) {
		ProductCard card = product.getCard();
		String productKey = card.getProductKey();
		products.put( productKey, product );
		productCards.put( productKey, card );
		productStates.put( productKey, new ProductState() );
	}

	void registerMod( Module module ) {
		registerProduct( module );
		ProductCard card = module.getCard();

		// Add the mod to the collection
		modules.put( card.getProductKey(), module );

		// Set the state flags
		setUpdatable( card, card.getProductUri() != null );
		setRemovable( card, true );
		// Don't set enabled here
	}

	private void unregisterProduct( Product product ) {
		String productKey = product.getCard().getProductKey();
		products.remove( productKey );
		productCards.remove( productKey );
		productStates.remove( productKey );
	}

	public void unregisterProgram( Xenon program ) {
		unregisterProduct( program );
	}

	void unregisterMod( Module module ) {
		ProductCard card = module.getCard();

		// Remove the module.
		modules.remove( card.getProductKey() );

		// Treat mods like other products
		unregisterProduct( module );
	}

	public Task<Collection<InstalledProduct>> installProducts( DownloadRequest... download ) {
		return installProducts( Set.of( download ) );
	}

	public Task<Collection<InstalledProduct>> installProducts( Set<DownloadRequest> downloads ) {
		log.atTrace().log( "Number of products to install: %s", downloads.size() );
		return new ProductManagerLogic( getProgram() ).installProducts( downloads );
	}

	public Task<Void> uninstallProducts( ProductCard... cards ) {
		return uninstallProducts( Set.of( cards ) );
	}

	public Task<Void> uninstallProducts( Set<ProductCard> cards ) {
		log.atTrace().log( "Number of products to remove: %s", cards.size() );
		return new ProductManagerLogic( getProgram() ).uninstallProducts( cards );
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

	ProductCard getInstalledProductCard( ProductCard card ) {
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

	private boolean isUpdatable( ProductCard card ) {
		ProductState state = productStates.get( card.getProductKey() );
		return state != null && state.isUpdatable();
	}

	private void setUpdatable( ProductCard card, boolean updatable ) {
		if( isUpdatable( card ) == updatable ) return;
		ProductState state = productStates.get( card.getProductKey() );
		if( state == null ) return;

		state.setUpdatable( updatable );
	}

	private boolean isRemovable( ProductCard card ) {
		ProductState state = productStates.get( card.getProductKey() );
		return state != null && state.isRemovable();
	}

	private void setRemovable( ProductCard card, boolean removable ) {
		if( isRemovable( card ) == removable ) return;
		ProductState state = productStates.get( card.getProductKey() );
		if( state == null ) return;

		state.setRemovable( removable );
	}

	public ProductCard getProductUpdate( ProductCard card ) {
		ProductState state = productStates.get( card.getProductKey() );
		return state == null ? null : state.getUpdate();
	}

	public void setProductUpdate( ProductCard card, ProductCard update ) {
		productStates.computeIfAbsent( card.getProductKey(), ( key ) -> new ProductState() ).setUpdate( update );
	}

	public boolean isEnabled( ProductCard card ) {
		return getProgram().getSettingsManager().getProductSettings( card ).get( PRODUCT_ENABLED_KEY, Boolean.class, false );
	}

	public boolean isModEnabled( Module module ) {
		return isEnabled( module.getCard() );
	}

	public void setModEnabled( ProductCard card, boolean enabled ) {
		Module module = getMod( card.getProductKey() );
		if( module != null ) {
			// Should be called before setting the enabled flag
			if( !enabled ) callModShutdown( module );

			setModEnabled( module, enabled );

			// Should be called after setting the enabled flag
			if( enabled ) callModStart( module );
		}
	}

	void setModEnabled( Module module, boolean enabled ) {
		if( isModEnabled( module ) == enabled ) return;

		Settings settings = getProgram().getSettingsManager().getProductSettings( module.getCard() );
		settings.set( PRODUCT_ENABLED_KEY, enabled );
		settings.flush();
		getEventBus().dispatch( new ModEvent( this, enabled ? ModEvent.ENABLED : ModEvent.DISABLED, module.getCard() ) );
		log.atDebug().log( "Set mod enabled: %s: %s", settings.getPath(), enabled );

	}

	public void setCheckOption( CheckOption checkOption ) {
		if( this.checkOption == checkOption ) return;
		this.checkOption = checkOption;
		getSettings().set( CHECK, checkOption.name().toLowerCase() );
	}

	public void setFoundOption( FoundOption foundOption ) {
		if( this.foundOption == foundOption ) return;
		this.foundOption = foundOption;
		getSettings().set( FOUND, foundOption.name().toLowerCase() );
	}

	public Long getLastUpdateCheck() {
		return getSettings().get( LAST_CHECK_TIME, Long.class );
	}

	public String getLastUpdateCheckText() {
		Long lastUpdateCheck = getLastUpdateCheck();
		String unknown = Rb.text( RbKey.UPDATE, "unknown" );
		return (lastUpdateCheck == null ? unknown : DateUtil.format( new Date( lastUpdateCheck ), DateUtil.DEFAULT_DATE_FORMAT ));
	}

	public Long getNextUpdateCheck() {
		return getSettings().get( NEXT_CHECK_TIME, Long.class );
	}

	public String getNextUpdateCheckText() {
		Long nextUpdateCheck = getNextUpdateCheck();
		String notScheduled = Rb.text( RbKey.UPDATE, "not-scheduled" );
		return (nextUpdateCheck == null ? notScheduled : DateUtil.format( new Date( nextUpdateCheck ), DateUtil.DEFAULT_DATE_FORMAT ));
	}

	void updateLastCheckTime() {
		// Update when the last update check occurred.
		getSettings().set( LAST_CHECK_TIME, System.currentTimeMillis() );

		// Schedule the next update check.
		scheduleUpdateCheck( false );
	}

	/**
	 * Schedule the update check task according to the settings. This method may safely be called as many times as necessary from any thread.
	 *
	 * @param startup True if the method is called at program start
	 */
	public void scheduleUpdateCheck( boolean startup ) {
		synchronized( scheduleLock ) {
			// If the program has not been updated and the UPDATE_IN_PROGRESS flag is
			// set, don't schedule update checks. This probably means there is a
			// problem applying an update. Otherwise, it should be safe to schedule
			// update checks.
			if( getProgram().isProgramUpdated() || getProgram().isUpdateInProgress() ) return;

			long now = System.currentTimeMillis();

			if( task != null ) {
				boolean currentTaskWaiting = task.scheduledExecutionTime() > now;
				if( currentTaskWaiting ) {
					task.cancel();
					task = null;
					log.atTrace().log( "Current check for updates task cancelled for new schedule." );
				}
			}

			final long delay = computeCheckDelay( startup, now );

			if( delay == NO_CHECK ) {
				getSettings().set( NEXT_CHECK_TIME, null );
				log.atDebug().log( "Future update check not scheduled." );
				return;
			}

			// Set the next update check time before scheduling the
			// task to prevent this method from looping rapidly
			long nextCheckTime = now + delay;
			getSettings().set( NEXT_CHECK_TIME, nextCheckTime );

			// Schedule the update check task
			if( updatesEnabled() ) timer.schedule( task = new UpdateCheckTask( this ), delay < 0 ? 0 : delay );

			// Log the next update check time
			String date = DateUtil.format( new Date( nextCheckTime ), DateUtil.DEFAULT_DATE_FORMAT, DateUtil.LOCAL_TIME_ZONE );
			log.atDebug().log( "Next check scheduled for: %s", LazyEval.of( () -> (delay == 0 ? "now" : date) ) );
		}
	}

	private long computeCheckDelay( boolean startup, long instant ) {
		Long lastUpdateCheck = getLastUpdateCheck();
		Long nextUpdateCheck = getNextUpdateCheck();
		long aMomentAgo = instant - TimeUnit.MINUTES.toMillis( 1 );
		long delay = NO_CHECK;

		switch( checkOption ) {
			case STARTUP:
				if( startup ) delay = 0;
				break;
			case INTERVAL: {
				CheckInterval intervalUnit = CheckInterval.valueOf( getUpdateCheckSettings().get( INTERVAL_UNIT, CheckInterval.WEEK.name() ).toUpperCase() );
				delay = getNextIntervalDelay( instant, intervalUnit, lastUpdateCheck );
				break;
			}
			case SCHEDULE: {
				CheckWhen scheduleWhen = CheckWhen.valueOf( getUpdateCheckSettings().get( SCHEDULE_WHEN, CheckWhen.DAILY.name() ).toUpperCase() );
				int scheduleHour = getUpdateCheckSettings().get( SCHEDULE_HOUR, Integer.class, 0 );
				delay = getNextScheduleDelay( instant, scheduleWhen, scheduleHour );
				break;
			}
		}
		if( nextUpdateCheck == null || nextUpdateCheck < aMomentAgo ) delay = 0;

		return delay;
	}

	/**
	 * Check for updates for all installed products
	 */
	public void checkForUpdates() {
		checkForUpdates( false );
	}

	public void checkForUpdates( boolean interactive ) {
		if( updatesEnabled() ) {
			new ProductManagerLogic( getProgram() ).checkForUpdates( interactive );
		} else {
			getSettings().set( LAST_CHECK_TIME, System.currentTimeMillis() );
			scheduleUpdateCheck( false );
		}
	}

	public void checkForStagedUpdatesAtStart() {
		if( getProgram().getHomeFolder() == null ) {
			log.atWarn().log( "Program not running from updatable location." );
			return;
		}

		log.atTrace().log( "Checking for staged updates..." );

		// If updates are staged, apply them.
		int updateCount = getStagedUpdateCount();
		if( updateCount > 0 ) {
			log.atInfo().log( "Staged updates detected: %s", updateCount );
			try {
				applyStagedUpdatesAtStart();
			} catch( Exception exception ) {
				log.atWarn().withCause( exception ).log( "Failed to apply staged updates" );
			}
		} else {
			log.atDebug().log( "No staged updates detected." );
		}
	}

	/**
	 * Apply staged updates found at program start, if any.
	 */
	public void applyStagedUpdatesAtStart() {
		int stagedUpdateCount = getStagedUpdateCount();
		log.atInfo().log( "Staged update count: %s", stagedUpdateCount );
		if( !updatesEnabled() || stagedUpdateCount == 0 ) return;

		if( getProgram().isUpdateInProgress() ) {
			getProgram().setUpdateInProgress( false );
			clearStagedUpdates();
		} else {
			new ProductManagerLogic( getProgram() ).notifyUpdatesReadyToApply( false );
			getProgram().getUpdateManager().stageUpdater();
		}
	}

	/**
	 * Gets the set of available product updates. If there are no posted updates found an empty set is returned.
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
				availableUpdates = new ProductManagerLogic( getProgram() ).findPostedUpdates( force ).get();
				return new HashSet<>( availableUpdates );
			} catch( Exception exception ) {
				log.atError().withCause( exception ).log( "Error refreshing available updates" );
			}

			return Set.of();
		}
	}

	Path getProductInstallFolder( ProductCard card ) {
		return getUserModuleFolder().resolve( card.getGroup() + "." + card.getArtifact() );
	}

	public Set<ProductUpdate> getStagedUpdates() {
		Set<ProductUpdate> staged = new HashSet<>();
		Set<ProductUpdate> remove = new HashSet<>();

		for( ProductUpdate update : updates.values() ) {
			if( Files.exists( update.getSource() ) ) {
				staged.add( update );
				log.atDebug().log( "Staged update found: %s", LazyEval.of( update::getSource ) );
			} else {
				remove.add( update );
				log.atWarn().log( "Staged update missing: %s", LazyEval.of( update::getSource ) );
			}
		}

		// Remove updates that cannot be found
		if( !remove.isEmpty() ) {
			for( ProductUpdate update : remove ) {
				updates.remove( update.getCard().getProductKey(), update );
			}
			saveUpdates( updates );
		}

		return staged;
	}

	/**
	 * Called when product updates have been staged and the collection of staged updates needs to be updated.
	 *
	 * @param updates The collection of product updates that were staged
	 */
	// THREAD TaskPool-worker
	void setStagedUpdates( Collection<ProductUpdate> updates ) {
		Map<String, ProductUpdate> updateMap = new ConcurrentHashMap<>();

		for( ProductUpdate update : updates ) {
			updateMap.put( update.getCard().getProductKey(), update );
		}

		this.updates.putAll( updateMap );

		saveUpdates( this.updates );
		getProgram().getUpdateManager().stageUpdater();
	}

	private int getStagedUpdateCount() {
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
	 * Launch the update program to apply the staged updates. This method is generally called when the program starts and, if the update program is successfully started, the program should be terminated
	 * to allow for the updates to be
	 * applied.
	 *
	 * @return The number of updates applied.
	 */
	public int applyStagedUpdates() {
		log.atInfo().log( "Updates enabled: %s", LazyEval.of( this::updatesEnabled ) );
		if( !updatesEnabled() ) return 0;

		int count = getStagedUpdates().size();
		if( count > 0 ) Fx.run( () -> getProgram().requestRestart( RestartJob.Mode.UPDATE, ProgramFlag.NODAEMON, ProgramFlag.LOG_APPEND ) );

		return count;
	}

	public Task<Collection<ProductUpdate>> updateProducts( DownloadRequest update, boolean interactive ) {
		return updateProducts( Set.of( update ), interactive );
	}

	/**
	 * Starts a new task to apply the selected updates.
	 *
	 * @param updates The updates to apply.
	 */
	public Task<Collection<ProductUpdate>> updateProducts( Set<DownloadRequest> updates, boolean interactive ) {
		log.atTrace().log( "Number of products to update: %s", LazyEval.of( updates::size ) );
		return new ProductManagerLogic( getProgram() ).stageAndApplyUpdates( updates, interactive );
	}

	void clearStagedUpdates() {
		// Remove the updates settings
		updates.clear();
		saveUpdates( updates );
	}

	Void saveRemovedProducts( Collection<InstalledProduct> removedProducts ) {
		Set<InstalledProduct> products = new HashSet<>( getProgram().getProductManager().getStoredRemovedProducts() );
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

	private static long getNextIntervalDelay( long currentTime, CheckInterval intervalUnit, Long lastUpdateCheck ) {
		if( lastUpdateCheck == null ) return 0;

		long intervalDelay = 0;
		switch( intervalUnit ) {
			case MONTH: {
				intervalDelay = TimeUnit.DAYS.toMillis( 30 );
				break;
			}
			case WEEK: {
				intervalDelay = TimeUnit.DAYS.toMillis( 7 );
				break;
			}
			case DAY: {
				intervalDelay = TimeUnit.DAYS.toMillis( 1 );
				break;
			}
			case HOUR: {
				intervalDelay = MILLIS_IN_HOUR;
				break;
			}
		}

		return (lastUpdateCheck + intervalDelay) - currentTime;
	}

	private static long getNextScheduleDelay( long currentTime, CheckWhen scheduleWhen, int scheduleHour ) {
		Calendar calendar = Calendar.getInstance( TimeZone.getTimeZone( "UTC" ) );
		calendar.setTimeInMillis( currentTime );

		// Sunday = 1, Monday = 2, ..., Saturday = 7
		// Calculate the day offset to the next check day
		int dayOffset;
		int nowDayOfWeek = calendar.get( Calendar.DAY_OF_WEEK );
		if( scheduleWhen == ProductManager.CheckWhen.DAILY ) {
			dayOffset = 1;
		} else {
			dayOffset = scheduleWhen.ordinal() - nowDayOfWeek;
		}
		if( dayOffset < 1 ) dayOffset += 7;

		// Calculate the next update check.
		calendar.add( Calendar.DAY_OF_MONTH, dayOffset );
		calendar.set( Calendar.HOUR_OF_DAY, scheduleHour );
		calendar.set( Calendar.MINUTE, 0 );
		calendar.set( Calendar.SECOND, 0 );
		calendar.set( Calendar.MILLISECOND, 0 );

		return calendar.getTimeInMillis() - currentTime;
	}

	private void loadSettings() {
		Settings programSettings = getProgram().getSettings();
		Settings managerSettings = getSettings();
		Settings updateCheckSettings = getUpdateCheckSettings();

		// Migrate check time settings from program settings to manager settings
		managerSettings.set( LAST_CHECK_TIME, programSettings.get( LAST_CHECK_TIME, Long.class ) );
		managerSettings.set( NEXT_CHECK_TIME, programSettings.get( NEXT_CHECK_TIME, Long.class ) );
		programSettings.remove( LAST_CHECK_TIME );
		programSettings.remove( NEXT_CHECK_TIME );

		// Backward compatibility for old STAGE option
		if( "STAGE".equalsIgnoreCase( updateCheckSettings.get( FOUND, FoundOption.NOTIFY.name() ) ) ) {
			updateCheckSettings.set( FOUND, FoundOption.APPLY.name().toLowerCase() );
		}

		this.checkOption = CheckOption.valueOf( updateCheckSettings.get( CHECK, CheckOption.MANUAL.name() ).toUpperCase() );
		this.foundOption = FoundOption.valueOf( updateCheckSettings.get( FOUND, FoundOption.NOTIFY.name() ).toUpperCase() );

		// Load the module sources
		loadRepos();

		// Load the module updates
		loadUpdates();
	}

	/**
	 * Get the product manager settings. The settings path is defined in
	 * {@link ManagerSettings#PRODUCT}.
	 *
	 * @return The product manager settings
	 */
	public Settings getSettings() {
		return getProgram().getSettingsManager().getSettings( ManagerSettings.PRODUCT );
	}

	/**
	 * Get the product repository settings.
	 *
	 * @return
	 */
	public Settings getRepositorySettings() {
		return getSettings();
	}

	/**
	 * Get the settings for update checking. These settings are stored in the
	 * program settings because they are changeable by the user in the settings
	 * tool.
	 *
	 * @return The settings for update checking
	 */
	public Settings getUpdateCheckSettings() {
		return getProgram().getSettings();
	}

	/**
	 * Get the settings for the product updates. These settings are stored
	 * separately from the program settings and from the product manager settings
	 * to avoid conflicts.
	 *
	 * @return The settings for the product updates
	 */
	public Settings getUpdatesSettings() {
		return getProgram().getSettingsManager().getSettings( ProgramSettings.UPDATES );
	}

	private boolean updatesEnabled() {
		return !getProgram().getProgramParameters().isTrue( ProgramFlag.NOUPDATE );
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

		getSettings().register( SettingsEvent.CHANGED, new SettingsChangeHandler() );

		// Create the update check timer.
		timer = new Timer( true );

		// Define the module folders.
		homeModuleFolder = getProgram().getHomeFolder().resolve( MODULE_INSTALL_FOLDER_NAME );
		userModuleFolder = getProgram().getDataFolder().resolve( MODULE_INSTALL_FOLDER_NAME );

		// Create the default module folders list.
		List<Path> moduleFolders = new ArrayList<>();
		moduleFolders.add( getHomeModuleFolder() );
		moduleFolders.add( getUserModuleFolder() );

		// Check for module paths in the parameters.
		List<String> modulePaths = getProgram().getProgramParameters().getValues( "module" );
		if( modulePaths != null ) {
			for( String path : modulePaths ) {
				Path folder = Paths.get( path );
				if( Files.exists( folder ) ) moduleFolders.add( folder );
			}
		}

		// Load the modules
		loadModules( moduleFolders.toArray( new Path[ 0 ] ) );

		// Disable mods specified on the command line
		List<String> disableMods = getProgram().getProgramParameters().getValues( ProgramFlag.DISABLE_MOD );
		modules.values().stream().filter( mod -> disableMods.contains( mod.getCard().getProductKey() ) ).forEach( mod -> setModEnabled( mod, false ) );
		if( !disableMods.isEmpty() ) log.atDebug().log( "Disabled mods: %s", disableMods );

		// Enable mods specified on the command line
		List<String> enableMods = getProgram().getProgramParameters().getValues( ProgramFlag.ENABLE_MOD );
		modules.values().stream().filter( mod -> enableMods.contains( mod.getCard().getProductKey() ) ).forEach( mod -> setModEnabled( mod, true ) );
		if( !enableMods.isEmpty() ) log.atDebug().log( "Enabled mods: %s", enableMods );

		return this;
	}

	@Override
	public ProductManager stop() {
		modules.values().forEach( this::callModUnregister );

		if( timer != null ) timer.cancel();

		return this;
	}

	public void startMods() {
		modules.values().forEach( this::callModStart );
	}

	public void stopMods() {
		modules.values().forEach( this::callModShutdown );
	}

	private void loadRepos() {
		Set<RepoState> repoStates = getRepositorySettings().get( REPOS_SETTINGS_KEY, new TypeReference<Set<RepoState>>() {}, new HashSet<>() );
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

		getRepos( true );
	}

	private void saveRepos() {
		getRepositorySettings().set( REPOS_SETTINGS_KEY, repos.values() );
	}

	@SuppressWarnings( "Convert2Diamond" )
	private void loadUpdates() {
		updates = getUpdatesSettings().get( UPDATES_SETTINGS_KEY, new TypeReference<Map<String, ProductUpdate>>() {}, updates );
	}

	private void saveUpdates( Map<String, ProductUpdate> updates ) {
		getUpdatesSettings().set( UPDATES_SETTINGS_KEY, updates );
	}

	private boolean isIncludedProduct( ProductCard card ) {
		return includedProducts.contains( card );
	}

	private void loadModules( Path... folders ) {
		// Look for mods on the module path
		try {
			loadModulePathMods();
		} catch( Exception exception ) {
			log.atError().withCause( exception ).log( "Error loading modules from module path" );
		}

		// Look for standard mods (most common)
		Arrays.stream( folders ).filter( Files::exists ).filter( Files::isDirectory ).forEach( ( folder ) -> {
			try {
				Files.list( folder ).filter( Files::isDirectory ).forEach( this::loadStandardMod );
			} catch( IOException exception ) {
				log.atError().withCause( exception ).log( "Error loading modules from: %s", folder );
			}
		} );
	}

	void doInstallMod( ProductCard card, Set<ProductResource> resources ) throws Exception {
		Path installFolder = getProductInstallFolder( card );

		log.atDebug().log( "Install product to: %s", installFolder );

		// Install all the resource files to the installation folder
		copyProductResources( resources, installFolder );
		log.atDebug().log( "Mod copied to: %s", installFolder );

		// Load the mod
		loadModules( installFolder );
		log.atDebug().log( "Mod loaded from: %s", installFolder );

		// Allow the mod to register resources
		callModRegister( getMod( card.getProductKey() ) );
		log.atDebug().log( "Mod registered: %s", LazyEval.of( card::getProductKey ) );

		// Set the enabled state
		setModEnabled( getMod( card.getProductKey() ), true );
		log.atDebug().log( "Mod enabled: %s", LazyEval.of( card::getProductKey ) );
	}

	void doRemoveMod( Module module ) {
		ProductCard card = module.getCard();
		Path installFolder = card.getInstallFolder();
		String source = installFolder == null ? "classpath" : installFolder.toString();

		log.atDebug().log( "Remove product from: %s", source );

		// Disable the product
		setModEnabled( module, false );
		log.atDebug().log( "Mod disabled: ", LazyEval.of( card::getProductKey ) );

		// Allow the mod to unregister resources
		callModUnregister( module );
		log.atDebug().log( "Mod unregistered: ", LazyEval.of( card::getProductKey ) );

		// Unload the mod
		unloadMod( module );
		log.atDebug().log( "Mod unloaded from: ", source );

		// Remove the product settings
		getProgram().getSettingsManager().getProductSettings( card ).delete();
	}

	void callModRegister( Module module ) {
		if( module.getStatus() != Module.Status.UNREGISTERED ) return;
		try {
			module.setStatus( Module.Status.REGISTERING );
			module.register();
			module.setStatus( Module.Status.REGISTERED );
			getEventBus().dispatch( new ModEvent( this, ModEvent.REGISTERED, module.getCard() ) );
		} catch( Throwable throwable ) {
			log.atError().withCause( throwable ).log( "Error registering mod: %s", LazyEval.of( () -> module.getCard().getProductKey() ) );
		}
	}

	void callModStart( Module module ) {
		if( module.getStatus() == Module.Status.UNREGISTERED ) callModRegister( module );
		if( !isEnabled( module.getCard() ) || module.getStatus() != Module.Status.REGISTERED ) return;
		try {
			module.setStatus( Module.Status.STARTING );
			module.startup();
			module.setStatus( Module.Status.STARTED );
			getEventBus().dispatch( new ModEvent( this, ModEvent.STARTED, module.getCard() ) );
		} catch( Throwable throwable ) {
			log.atError().withCause( throwable ).log( "Error starting mod: %s", LazyEval.of( () -> module.getCard().getProductKey() ) );
		}
		log.atDebug().log( "module=%s  status=%s", LazyEval.of( () -> module.getCard().getProductKey() ), LazyEval.of( module::getStatus ) );
	}

	void callModShutdown( Module module ) {
		if( !isEnabled( module.getCard() ) || module.getStatus() != Module.Status.STARTED ) return;
		try {
			module.setStatus( Module.Status.STOPPING );
			module.shutdown();
			module.setStatus( Module.Status.STOPPED );
			getEventBus().dispatch( new ModEvent( this, ModEvent.STOPPED, module.getCard() ) );
		} catch( Throwable throwable ) {
			log.atError().withCause( throwable ).log( "Error stopping mod: %s", LazyEval.of( () -> module.getCard().getProductKey() ) );
		}
		log.atDebug().log( "module=%s  status=%s", LazyEval.of( () -> module.getCard().getProductKey() ), LazyEval.of( module::getStatus ) );
	}

	void callModUnregister( Module module ) {
		if( module.getStatus() == Module.Status.STARTED ) callModShutdown( module );
		try {
			module.setStatus( Module.Status.UNREGISTERING );
			module.unregister();
			module.setStatus( Module.Status.UNREGISTERED );
			getEventBus().dispatch( new ModEvent( this, ModEvent.UNREGISTERED, module.getCard() ) );
		} catch( Throwable throwable ) {
			log.atError().log( "Error unregistering mod: %s", LazyEval.of( () -> module.getCard().getProductKey() ) );
		}
		log.atDebug().log( "module=%s  status=%s", LazyEval.of( () -> module.getCard().getProductKey() ), LazyEval.of( module::getStatus ) );
	}

	private void purgeRemovedProducts() {
		// Check for products marked for removal and remove the files.
		Set<InstalledProduct> products = getStoredRemovedProducts();
		for( InstalledProduct product : products ) {
			log.atDebug().log( "Purging: %s", product );
			try {
				FileUtil.delete( product.getTarget() );
			} catch( IOException exception ) {
				log.atError().withCause( exception ).log( "Error removing product: %s", product );
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

	public Path getUpdatesFolder() {
		return getProgram().getDataFolder().resolve( ProductManager.UPDATE_FOLDER_NAME );
	}

	private void loadModulePathMods() {
		log.atTrace().log( "Loading standard mod from: module-path" );
		try {
			ServiceLoader.load( Module.class ).forEach( ( mod ) -> loadMod( mod, null ) );
		} catch( Throwable throwable ) {
			log.atError().withCause( throwable ).log( "Error loading module-path mods" );
		}
	}

	private void loadStandardMod( Path folder ) {
		// In this context module refers to Java modules and mod refers to program mods
		// It is expected that each folder has only one mod

		log.atTrace().log( "Loading standard mod from: %s", folder );
		try {
			// Load the mod descriptor
			ProductCard card = ProductCard.card( folder );

			// Obtain the boot module layer
			ModuleLayer bootLayer = ModuleLayer.boot();
			Configuration bootConfiguration = bootLayer.configuration();

			String modVersion = card.getPackagingVersion();
			log.atDebug().log( "Mod version: %s", modVersion );

			ModuleFinder finder = ModuleFinder.of( folder );
			if( "2".equals( modVersion ) ) {
				finder = ModuleFinder.of( Files.list( folder ).filter( f -> f.getFileName().toString().endsWith( ".jar" ) ).distinct().toArray( Path[]::new ) );
			}

			// Create the mod module layer
			Configuration modConfiguration = bootConfiguration.resolveAndBind( ModuleFinder.of(), finder, Set.of() );
			ModuleLayer modLayer = bootLayer.defineModulesWithManyLoaders( modConfiguration, getProgram().getClass().getClassLoader() );
			ServiceLoader<Module> loader = ServiceLoader.load( modLayer, Module.class );

			// Load the mod
			loader.stream().findFirst().ifPresentOrElse(
				m -> {
					loadMod( m.get(), folder );
				}, () -> {
					log.atError().log( "Standard mod expected: %s", folder );
				}
			);
		} catch( Throwable throwable ) {
			log.atError().withCause( throwable ).log( "Error loading standard mods: %s", folder );
		}
	}

	private void loadMod( Module module, Path source ) {
		ProductCard card = module.getCard();
		String message = card.getProductKey() + " from: " + (source == null ? "classpath" : source);
		try {
			log.atDebug().log( "Loading mod: %s", message );

			// Ignore included products
			if( isIncludedProduct( card ) ) return;

			// Check if mod is already loaded
			if( getMod( card.getProductKey() ) != null ) {
				log.atWarn().log( "Mod already loaded: %s", LazyEval.of( card::getProductKey ) );
				return;
			}

			// Configure logging for the mod
			Log.setPackageLogLevel( module.getClass().getPackageName(), getProgram().getProgramParameters().get( LogFlag.LOG_LEVEL ) );

			// Initialize the mod
			module.init( getProgram(), card );

			// This will need to change if nested mods are to be supported
			// Set the parent product
			module.setParent( getProgram() );

			// Set the mod installation folder
			card.setInstallFolder( source );

			// Add the product registration to the manager
			registerMod( module );

			// Notify handlers of install
			getEventBus().dispatch( new ModEvent( this, ModEvent.INSTALLED, module.getCard() ) );

			log.atDebug().log( "Mod loaded: %s", message );
		} catch( Throwable throwable ) {
			log.atError().withCause( throwable ).log( "Error loading mod %s", message );
		}
	}

	private void unloadMod( Module module ) {
		ProductCard card = module.getCard();

		String message = card.getProductKey();
		try {
			log.atDebug().log( "Unloading mod: %s", message );

			// Remove the product registration from the manager
			unregisterMod( module );

			// Notify handlers of remove
			getEventBus().dispatch( new ModEvent( this, ModEvent.REMOVED, module.getCard() ) );

			// TODO Disable logging for a mod that has been removed

			log.atDebug().log( "Mod unloaded: %s", message );
		} catch( Throwable throwable ) {
			log.atError().withCause( throwable ).log( "Error unloading mod %s", message );
		}
	}

	private final class SettingsChangeHandler implements EventHandler<SettingsEvent> {

		@Override
		public void handle( SettingsEvent event ) {
			switch( event.getKey() ) {
				case CHECK -> {
					setCheckOption( CheckOption.valueOf( event.getNewValue().toString().toUpperCase() ) );
					scheduleUpdateCheck( false );
				}
				case FOUND -> setFoundOption( FoundOption.valueOf( event.getNewValue().toString().toUpperCase() ) );
			}
		}

	}

	private static final class UpdateCheckTask extends TimerTask {

		private final ProductManager productManager;

		UpdateCheckTask( ProductManager productManager ) {
			this.productManager = productManager;
		}

		@Override
		public void run() {
			productManager.checkForUpdates();
		}

	}

}
