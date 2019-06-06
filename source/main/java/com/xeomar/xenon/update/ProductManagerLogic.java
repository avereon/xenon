package com.xeomar.xenon.update;

import com.xeomar.product.CatalogCard;
import com.xeomar.product.ProductCard;
import com.xeomar.product.ProductCardComparator;
import com.xeomar.product.RepoCard;
import com.xeomar.util.FileUtil;
import com.xeomar.util.LogUtil;
import com.xeomar.xenon.BundleKey;
import com.xeomar.xenon.Program;
import com.xeomar.xenon.notice.Notice;
import com.xeomar.xenon.resource.type.ProgramProductType;
import com.xeomar.xenon.task.Task;
import com.xeomar.xenon.task.chain.TaskChain;
import com.xeomar.xenon.tool.product.ProductTool;
import com.xeomar.xenon.util.Asynchronous;
import com.xeomar.xenon.util.DialogUtil;
import com.xeomar.xenon.util.Synchronous;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import org.slf4j.Logger;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.function.LongConsumer;
import java.util.stream.Collectors;

public class ProductManagerLogic {

	private static final Logger log = LogUtil.get( MethodHandles.lookup().lookupClass() );

	private static final ProductCard PRODUCT_CONNECTION_ERROR = new ProductCard();

	private static final RepoCard REPO_CONNECTION_ERROR = new RepoCard();

	private Program program;

	private V2RepoClient repoClient;

	public ProductManagerLogic( Program program ) {
		this.program = program;
		this.repoClient = new V2RepoClient( program );
	}

	// This is a method for testing the update found dialog.
	// It should not be used for production functionality.
	public void showUpdateFoundDialog() {
		notifyUpdatesReadyToApply( true );
	}

	@Asynchronous
	void checkForUpdates( boolean interactive ) {
		createFindPostedUpdatesChain( program.getProductManager().getInstalledProductCards(), interactive )
			.link( ( cards ) -> handlePostedUpdatesResult( cards, interactive ) )
			.run( program );
	}

	@Asynchronous
	Task<Set<ProductCard>> getAvailableProducts( boolean force ) {
		// TODO The force parameter just means to refresh the cache

		return TaskChain
			.init( this::startEnabledCatalogCardDownloads )
			.link( this::collectCatalogCardDownloads )
			.link( this::startAllProductCardDownloadTasks )
			.link( this::collectProductCardDownloads )
			.link( this::determineAvailableProducts )
			.run( program );
	}

	/**
	 * @param products The set of products to find updates for
	 * @param force Request that the cache be flushed before finding updates
	 * @return The map of updateable products and in which repo the update is located
	 */
	@Synchronous
	Task<Set<ProductCard>> findPostedUpdates( Set<ProductCard> products, boolean force ) {
		// TODO The force parameter just means to refresh the cache

		return createFindPostedUpdatesChain( products, force ).run( program );
	}

	@Asynchronous
	void stageUpdates( Set<ProductCard> updates ) {
		try {
			TaskChain
				.init( () -> startResourceDownloads( updates ) )
				.link( this::startProductResourceCollectors )
				.link( this::collectProductUpdates )
				.link( this::stageProductUpdates )
				.run( program );
		} catch( Exception exception ) {
			exception.printStackTrace();
		}
	}

	@Asynchronous
	void stageAndApplyUpdates( Set<ProductCard> updates, boolean interactive ) {
		try {
			TaskChain
				.init( () -> startResourceDownloads( updates ) )
				.link( this::startProductResourceCollectors )
				.link( this::collectProductUpdates )
				.link( this::stageProductUpdates )
				.link( ( productUpdates ) -> handleStagedProductUpdates( productUpdates, interactive ) )
				.run( program );
		} catch( Exception exception ) {
			exception.printStackTrace();
		}
	}

	void createProductUpdates( Set<ProductCard> products ) {
		// Start with StageUpdates and end with ProductUpdateCollector

		//Future<ProductUpdate> productUpdateFutures = stageUpdates( cardsAndRepos, false );
		//Set<ProductResource> productResources = downloadProductResources( productUpdateFutures );
		//ProductUpdate productUpdate = collectProductResources( productResources );
		//Collection<ProductUpdate> productUpdates = collectProductUpdates( productUpdate );

		//		new TaskChain<Collection<ProductUpdate>>( program )
		//			.add( () -> stageUpdates( products, false ) )
		//			// TODO This gets passed to install logic that isn't implemented in this class yet
		//			// It's in the ProductManager.InstallProducts class
		//			.run();
	}

	private TaskChain<Set<ProductCard>> createFindPostedUpdatesChain( Set<ProductCard> products, boolean force ) {
		Map<String, ProductCard> installedProducts = program.getProductManager().getInstalledProductCardsMap();

		return TaskChain
			.init( this::startEnabledCatalogCardDownloads )
			.link( this::collectCatalogCardDownloads )
			.link( ( catalogs ) -> startSelectedProductCardDownloadTasks( catalogs, products ) )
			.link( this::collectProductCardDownloads )
			.link( ( availableProducts ) -> determineUpdateableProducts( availableProducts, installedProducts ) );
	}

	private Map<RepoCard, Task<Download>> startEnabledCatalogCardDownloads() {
		Set<RepoCard> repos = program
			.getProductManager()
			.getRepos()
			.stream()
			.filter( RepoCard::isEnabled )
			.collect( Collectors.toSet() );
		return startCatalogCardDownloads( repos );
	}

	private Map<RepoCard, Task<Download>> startCatalogCardDownloads( Set<RepoCard> repos ) {
		Map<RepoCard, Task<Download>> downloads = new HashMap<>();

		repos.forEach( ( r ) -> {
			log.info( "Creating catalog downloads for repo: " + r.getName() );
			URI uri = repoClient.getCatalogUri( r );
			downloads.put( r, program.getTaskManager().submit( new DownloadTask( program, uri ) ) );
		} );
		return downloads;
	}

	private Map<RepoCard, CatalogCard> collectCatalogCardDownloads( Map<RepoCard, Task<Download>> downloads ) {
		Map<RepoCard, CatalogCard> catalogs = new HashMap<>();

		downloads.keySet().forEach( ( r ) -> {
			try {
				catalogs.put( r, CatalogCard.load( r, downloads.get( r ).get().getInputStream() ) );
				log.info( "Catalog card loaded for " + r );
			} catch( IOException | ExecutionException | InterruptedException exception ) {
				// FIXME Need to indicate there was an IO exception
				exception.printStackTrace();
			}
		} );
		return catalogs;
	}

	private Map<RepoCard, Set<Task<Download>>> startSelectedProductCardDownloadTasks( Map<RepoCard, CatalogCard> catalogs, Set<ProductCard> products ) {
		Map<RepoCard, Set<Task<Download>>> downloads = new HashMap<>();
		Set<String> artifacts = products.stream().map( ProductCard::getArtifact ).collect( Collectors.toSet() );

		catalogs.keySet().forEach( ( repo ) -> {
			CatalogCard catalog = catalogs.get( repo );
			Set<Task<Download>> repoDownloads = downloads.computeIfAbsent( repo, ( k ) -> new HashSet<>() );
			catalog
				.getProducts()
				.stream()
				.filter( artifacts::contains )
				.forEach( ( product ) -> repoDownloads.add( program
					.getTaskManager()
					.submit( new DownloadTask( program, repoClient.getProductUri( repo, product, "product", "card" ) ) ) ) );
		} );

		return downloads;
	}

	private Map<RepoCard, Set<Task<Download>>> startAllProductCardDownloadTasks( Map<RepoCard, CatalogCard> catalogs ) {
		Map<RepoCard, Set<Task<Download>>> downloads = new HashMap<>();

		catalogs.keySet().forEach( ( repo ) -> {
			CatalogCard catalog = catalogs.get( repo );
			Set<Task<Download>> repoDownloads = downloads.computeIfAbsent( repo, ( k ) -> new HashSet<>() );
			catalog
				.getProducts()
				.forEach( ( product ) -> repoDownloads.add( program
					.getTaskManager()
					.submit( new DownloadTask( program, repoClient.getProductUri( repo, product, "product", "card" ) ) ) ) );
		} );

		return downloads;
	}

	private Map<RepoCard, Set<ProductCard>> collectProductCardDownloads( Map<RepoCard, Set<Task<Download>>> downloads ) {
		Map<RepoCard, Set<ProductCard>> products = new HashMap<>();

		downloads.keySet().forEach( ( repo ) -> {
			Set<Task<Download>> repoDownloads = downloads.get( repo );
			repoDownloads.forEach( ( task ) -> {
				Set<ProductCard> productSet = products.computeIfAbsent( repo, ( k ) -> new HashSet<>() );
				try {
					ProductCard product = new ProductCard().load( task.get().getInputStream(), task.get().getSource() );
					productSet.add( product );
					log.info( "Product card loaded for " + product );
				} catch( IOException | ExecutionException | InterruptedException exception ) {
					productSet.add( PRODUCT_CONNECTION_ERROR );
					exception.printStackTrace();
				}
			} );
		} );

		return products;
	}

	Set<ProductCard> determineAvailableProducts( Map<RepoCard, Set<ProductCard>> products ) {
		return determineUpdateableProducts( products, Map.of() );
	}

	Set<ProductCard> determineUpdateableProducts( Map<RepoCard, Set<ProductCard>> products, Map<String, ProductCard> installedProducts ) {
		if( products == null ) throw new NullPointerException( "Product map cannot be null" );

		boolean determineAvailable = installedProducts.size() == 0;
		boolean determineUpdates = !determineAvailable;

		// Create a product/version map
		Map<String, List<ProductCard>> productVersions = new HashMap<>();
		products
			.keySet()
			.stream()
			.filter( RepoCard::isEnabled )
			.forEach( ( repo ) -> products.get( repo ).forEach( ( product ) -> {
				productVersions.computeIfAbsent( product.getProductKey(), ( k ) -> new ArrayList<>() ).add( product );
				product.setRepo( repo );
			} ) );

		Set<ProductCard> cards = new HashSet<>();

		Comparator<ProductCard> comparator = new ProductCardComparator( ProductCardComparator.Field.RELEASE ).reversed();
		productVersions.values().forEach( ( productVersionList ) -> {
			// Sort all the latest product versions to the top of each list
			productVersionList.sort( comparator );

			ProductCard latest = productVersionList.get( 0 );
			RepoCard repo = latest.getRepo();
			ProductCard installed = installedProducts.get( latest.getProductKey() );
			boolean latestIsInstalled = installed != null && latest.getRelease().equals( installed.getRelease() );
			boolean updateAvailable = determineUpdates && installed != null && !latestIsInstalled;

			if( determineAvailable || updateAvailable ) cards.add( new ProductCard().copyFrom( latest ).setRepo( repo ) );

			if( installed != null ) log.debug( "Installed: " + installed.getProductKey() + " " + installed.getRelease() );
			log.debug( "Available: " + latest.getProductKey() + " " + latest.getRelease() );
			log.info( "Latest version: " + latest + " " + latest.getRelease() + " found in: " + repo );
		} );

		return cards;
	}

	private Void handlePostedUpdatesResult( Set<ProductCard> cards, boolean interactive ) {
		long connectionErrors = cards.stream().filter( ( source ) -> source.getRepo() == REPO_CONNECTION_ERROR ).count();
		boolean available = cards.size() > 0;

		if( interactive ) {
			if( available ) {
				openProductTool();
			} else {
				notifyUserOfNoUpdates( connectionErrors > 0 );
			}
		} else {
			switch( program.getProductManager().getFoundOption() ) {
				case SELECT: {
					notifyUserOfUpdates();
					break;
				}
				case STORE: {
					stageUpdates( cards );
					break;
				}
				case APPLY: {
					stageAndApplyUpdates( cards, false );
					break;
				}
			}
		}

		return null;
	}

	private Set<ProductResourceCollector> startResourceDownloads( Set<ProductCard> cards ) {
		Path stageFolder = program.getDataFolder().resolve( ProductManager.UPDATE_FOLDER_NAME );

		log.debug( "Number of packs to stage: " + cards.size() );
		log.trace( "Pack stage folder: " + stageFolder );

		try {
			Files.createDirectories( stageFolder );
		} catch( IOException exception ) {
			log.warn( "Error creating update stage folder: " + stageFolder, exception );
			return Set.of();
		}

		// The remaining code in this method is not particularly obvious but is
		// done this way for a better user experience. The idea is to submit the
		// product download resource tasks and return the tasks that build product
		// updates from those resources.
		return cards.stream().map( ( card ) -> {
			try {
				RepoCard repo = card.getRepo();

				// The returned ProductResource objects contain the product resource download futures
				Set<ProductResource> resources = startProductResourceDownloads( repo, card );

				// Create the task that will produce the ProductUpdate but don't submit it here
				Path updatePack = stageFolder.resolve( getStagedUpdateFileName( card ) );
				return new ProductResourceCollector( repo, card, resources, updatePack );
			} catch( Exception exception ) {
				return null;
			}
		} ).filter( Objects::nonNull ).collect( Collectors.toSet() );
	}

	private class DownloadProductResourceTask extends Task<Set<ProductResource>> {

		private RepoCard repo;

		private ProductCard card;

		DownloadProductResourceTask( RepoCard repo, ProductCard card ) {
			setName( program
				.getResourceBundle()
				.getString( BundleKey.UPDATE, "task-updates-cache-update", card.getName(), card.getVersion() ) );
			this.repo = repo;
			this.card = card;
		}

		@Override
		public Set<ProductResource> call() throws Exception {
			// Determine all the resources to download.
			PackProvider provider = new PackProvider( program, repo, repoClient, card );
			Set<ProductResource> resources = provider.getResources();
			log.debug( "Product resource count: " + resources.size() );

			resources.forEach( ( resource ) -> resource.setFuture( program
				.getTaskManager()
				.submit( new DownloadTask( program, getSchemeResolvedUri( resource.getUri() ) ) ) ) );

			return resources;
		}

		private URI getSchemeResolvedUri( URI uri ) {
			if( uri == null ) return null;
			return uri.getScheme() == null ? Paths.get( uri.getPath() ).toUri() : uri;
		}

	}

	private class ProductResourceCollector extends Task<ProductUpdate> {

		private RepoCard repo;

		private ProductCard product;

		private Set<ProductResource> resources;

		private Path localPackPath;

		ProductResourceCollector( RepoCard repo, ProductCard product, Set<ProductResource> resources, Path localPackPath ) {
			this.repo = repo;
			this.product = product;
			this.resources = resources;
			this.localPackPath = localPackPath;
		}

		@Override
		public ProductUpdate call() throws Exception {
			// Wait for all resources to be downloaded
			resources.forEach( ( resource ) -> {
				try {
					resource.waitFor();
					log.debug( "Product resource target: " + resource.getLocalFile() );
				} catch( Exception exception ) {
					resource.setThrowable( exception );
					log.error( "Error downloading resource: " + resource, exception );
				}
			} );

			// Verify the resources have all been staged successfully
			if( !areAllResourcesValid( resources ) ) {
				log.warn( "Update missing resources: " + product );
				return null;
			}

			stageResources( localPackPath, this::setProgress );

			ProductManager manager = program.getProductManager();
			Path installFolder = manager.getProductInstallFolder( product );
			if( manager.isInstalled( product ) ) {
				installFolder = manager.getInstalledProductCard( product ).getInstallFolder();
			}

			log.debug( "Update staged: " + product.getProductKey() + " " + product.getRelease() );
			log.debug( "           to: " + localPackPath );

			// Notify listeners the update is staged
			new ProductManagerEvent( manager, ProductManagerEvent.Type.PRODUCT_STAGED, product ).fire( manager.getProductManagerListeners() );

			return new ProductUpdate( repo, product, localPackPath, installFolder );
		}

		private boolean areAllResourcesValid( Set<ProductResource> resources ) {
			return resources.stream().filter( ( r ) -> !r.isValid() ).collect( Collectors.toSet() ).size() == 0;
		}

		private void stageResources( Path updatePack, LongConsumer progressCallback ) throws IOException {
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
				program.getProductManager().copyProductResources( resources, updateFolder );
				setTotal( FileUtil.getDeepSize( updateFolder ) );
				FileUtil.zip( updateFolder, updatePack, progressCallback );
				FileUtil.deleteOnExit( updateFolder );
			}
		}

	}

	// Minimized task
	//	private class ProductUpdateCollector extends Task<Collection<ProductUpdate>> {
	//
	//		private Set<Future<ProductUpdate>> updateFutures;
	//
	//		private boolean interactive;
	//
	//		public ProductUpdateCollector( Set<Future<ProductUpdate>> updateFutures, boolean interactive ) {
	//			this.updateFutures = updateFutures;
	//			this.interactive = interactive;
	//		}
	//
	//		@Override
	//		public Collection<ProductUpdate> call() throws Exception {
	//			Collection<ProductUpdate> updates = collectProductUpdates( updateFutures, interactive );
	//
	//			if( program.getProductManager().getFoundOption() == ProductManager.FoundOption.APPLY ) {
	//				//program.getTaskManager().submit( new UpdatesReadyToApply( interactive ) );
	//
	//				// Because this is the last task and it returns void...this one is easy
	//				program.getTaskManager().submit( Lambda.task( "", () -> updatesReadyToApply( interactive ) ) );
	//			}
	//
	//			return updates;
	//		}
	//
	//	}

	private Set<Task<ProductUpdate>> startProductResourceCollectors( Set<ProductResourceCollector> collectors ) {
		return collectors.stream().map( ( task ) -> program.getTaskManager().submit( task ) ).collect( Collectors.toSet() );
	}

	private Collection<ProductUpdate> collectProductUpdates( Set<Task<ProductUpdate>> updateFutures ) {
		return updateFutures.stream().map( ( future ) -> {
			try {
				return future.get();
			} catch( Exception exception ) {
				return null;
			}
		} ).filter( Objects::nonNull ).collect( Collectors.toSet() );
	}

	private Collection<ProductUpdate> stageProductUpdates( Collection<ProductUpdate> productUpdates ) {
		if( productUpdates.size() == 0 ) return Set.of();

		Collection<ProductUpdate> stagedUpdates = new HashSet<>();
		for( ProductUpdate update : productUpdates ) {
			ProductCard updateCard = update.getCard();

			// Verify the product is registered
			if( !program.getProductManager().isInstalled( updateCard ) ) {
				log.warn( "Product not registered: " + updateCard );
				continue;
			}

			// Verify the product is installed
			Path installFolder = program.getProductManager().getInstalledProductCard( updateCard ).getInstallFolder();
			boolean installFolderValid = installFolder != null && Files.exists( installFolder );
			if( !installFolderValid ) {
				log.warn( "Missing install folder: " + installFolder );
				log.warn( "Product not installed:  " + updateCard );
				continue;
			}

			// Add the update to the set of staged updates
			stagedUpdates.add( update );
		}

		program
			.getTaskManager()
			.submit( Task.of( "Store staged update settings", () -> program
				.getProductManager()
				.setStagedUpdates( stagedUpdates ) ) );

		log.debug( "Product update count: " + stagedUpdates.size() );

		return stagedUpdates;
	}

	private Collection<ProductUpdate> handleStagedProductUpdates( Collection<ProductUpdate> productUpdates, boolean interactive ) {
		if( productUpdates.size() == 0 ) return productUpdates;

		if( program.getProductManager().getFoundOption() == ProductManager.FoundOption.APPLY ) {
			notifyUpdatesReadyToApply( interactive );
		}

		return productUpdates;
	}

	// Utility methods -----------------------------------------------------------

	private void notifyUserOfNoUpdates( boolean connectionErrors ) {
		String title = program.getResourceBundle().getString( BundleKey.UPDATE, "updates" );
		String updatesNotAvailable = program.getResourceBundle().getString( BundleKey.UPDATE, "updates-not-available" );
		String updatesCannotConnect = program
			.getResourceBundle()
			.getString( BundleKey.UPDATE, "updates-source-cannot-connect" );
		final String message = connectionErrors ? updatesCannotConnect : updatesNotAvailable;
		Platform.runLater( () -> program.getNoticeManager().addNotice( new Notice( title, message ) ) );
	}

	private void openProductTool() {
		URI uri = URI.create( ProgramProductType.URI + "#" + ProgramProductType.UPDATES );
		Platform.runLater( () -> program.getResourceManager().open( uri ) );
	}

	private void notifyUserOfUpdates() {
		String title = program.getResourceBundle().getString( BundleKey.UPDATE, "updates" );
		String message = program.getResourceBundle().getString( BundleKey.UPDATE, "updates-found-review" );
		URI uri = URI.create( ProgramProductType.URI + "#" + ProgramProductType.UPDATES );

		Notice notice = new Notice( title, message, () -> program.getResourceManager().open( uri ) );
		Platform.runLater( () -> program.getNoticeManager().addNotice( notice ) );
	}

	private String getStagedUpdateFileName( ProductCard card ) {
		return card.getGroup() + "." + card.getArtifact() + ".pack";
	}

	private Set<ProductResource> startProductResourceDownloads( RepoCard repo, ProductCard card ) throws InterruptedException, ExecutionException {
		return program.getTaskManager().submit( new DownloadProductResourceTask( repo, card ) ).get();
	}

	private void notifyUpdatesReadyToApply( boolean interactive ) {
		if( interactive ) {
			Platform.runLater( this::showAlert );
		} else {
			Platform.runLater( this::showNotice );
		}
	}

	private void showNotice() {
		String header = program.getResourceBundle().getString( BundleKey.UPDATE, "restart-required" );
		String message = program.getResourceBundle().getString( BundleKey.UPDATE, "restart-recommended" );

		Notice notice = new Notice( header, message, () -> Platform.runLater( this::showAlert ) );
		program.getNoticeManager().addNotice( notice );
	}

	private void showAlert() {
		String title = program.getResourceBundle().getString( BundleKey.UPDATE, "updates" );
		String header = program.getResourceBundle().getString( BundleKey.UPDATE, "restart-required" );
		String message = program.getResourceBundle().getString( BundleKey.UPDATE, "restart-recommended" );

		Alert alert = new Alert( Alert.AlertType.CONFIRMATION, "", ButtonType.YES, ButtonType.NO );
		alert.setTitle( title );
		alert.setHeaderText( header );
		alert.setContentText( message );

		Stage stage = program.getWorkspaceManager().getActiveStage();
		Optional<ButtonType> result = DialogUtil.showAndWait( stage, alert );

		if( result.isPresent() && result.get() == ButtonType.YES ) {
			program.getWorkspaceManager().requestCloseTools( ProductTool.class );
			program.getProductManager().applyStagedUpdates();
		}
	}

}
