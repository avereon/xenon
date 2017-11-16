package com.xeomar.xenon;

import com.xeomar.product.Product;
import com.xeomar.product.ProductCard;
import com.xeomar.xenon.update.ProductUpdate;
import com.xeomar.settings.Settings;
import com.xeomar.util.Configurable;
import com.xeomar.util.Controllable;

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
		MANUAL, STARTUP, INTERVAL, SCHEDULE
	}

	public enum CheckInterval {
		MONTH, WEEK, DAY, HOUR
	}

	public enum CheckWhen {
		DAILY, SUNDAY, MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY
	}

	public enum FoundOption {
		SELECT, STORE, STAGE
	}

	public enum ApplyOption {
		VERIFY, IGNORE, RESTART
	}

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

	//private Set<ProductCatalog> catalogs;

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

//		catalogs = new CopyOnWriteArraySet<ProductCatalog>();
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

	public void checkForUpdates() {
		// NEXT Work on implementing update manager
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

	@Override
	public void setSettings( Settings settings ) {
		this.settings = settings;
	}

	@Override
	public Settings getSettings() {
		return settings;
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
