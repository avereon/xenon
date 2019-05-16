package com.xeomar.xenon.update;

import com.xeomar.product.ProductCard;
import com.xeomar.product.ProductCardComparator;
import com.xeomar.util.FileUtil;
import com.xeomar.util.LogUtil;
import com.xeomar.xenon.BundleKey;
import com.xeomar.xenon.Program;
import com.xeomar.xenon.notice.Notice;
import com.xeomar.xenon.resource.type.ProgramProductType;
import com.xeomar.xenon.task.Task;
import com.xeomar.xenon.tool.product.ProductTool;
import com.xeomar.xenon.util.DialogUtil;
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
import java.util.concurrent.Future;
import java.util.function.LongConsumer;
import java.util.stream.Collectors;

public class UpdateCheckPoc {

	private static final Logger log = LogUtil.get( MethodHandles.lookup().lookupClass() );

	private Program program;

	private V2RepoClient repoClient;

	public UpdateCheckPoc( Program program ) {
		this.program = program;
		this.repoClient = new V2RepoClient( program );
	}

	public void checkForUpdates( boolean interactive ) {
		program.getTaskManager().submit( new CheckForUpdatesTask( interactive ) );
	}

	public Set<ProductCard> findPostedUpdates( boolean force ) {
		// TODO This method go through the logic as far as DetermineUpdateableVersionTask and return
		return Set.of();
	}

	public void stageAndApplyUpdates( Set<ProductCard> updates, boolean interactive ) {
		// TODO This method directly correlates to updating products from the ProductTool

		Map<ProductCard, RepoCard> cards = new HashMap<>();
		// It will be interesting how to weave this into the existing logic
		program.getTaskManager().submit( new HandleCheckForUpdatesActionTask( cards, interactive, false ) );
	}

	public void createProductUpdate( ProductCard card, Path updatePack ) {
		// TODO This method should go through the logic to create a product update
	}

	// This is a method for testing the update found dialog.
	// It should not be used for production functionality.
	public void showUpdateFoundDialog() {
		new ApplyUpdates( Set.of(), true ).call();
	}

	private class CheckForUpdatesTask extends Task<Map<RepoCard, Task<Download>>> {

		private boolean interactive;

		public CheckForUpdatesTask( boolean interactive ) {
			this.interactive = interactive;
		}

		@Override
		public Map<RepoCard, Task<Download>> call() throws Exception {
			Map<RepoCard, Task<Download>> downloads = new HashMap<>();

			// FIXME Temporarily look in all repos
			//program.getProductManager().getRepos().stream().filter( RepoCard::isEnabled ).forEach( ( r ) -> {
			program.getProductManager().getRepos().stream().forEach( ( r ) -> {
				log.info( "Creating catalog downloads for repo: " + r.getName() );
				URI uri = repoClient.getCatalogUri( r );
				downloads.put( r, (Task<Download>)program.getTaskManager().submit( new DownloadTask( program, uri ) ) );
			} );

			program.getTaskManager().submit( new DownloadCatalogCardCollector( downloads, interactive ) );

			return downloads;
		}

	}

	private class DownloadCatalogCardCollector extends Task<Map<RepoCard, CatalogCard>> {

		private Map<RepoCard, Task<Download>> downloads;

		private boolean interactive;

		public DownloadCatalogCardCollector( Map<RepoCard, Task<Download>> downloads, boolean interactive ) {
			this.downloads = downloads;
			this.interactive = interactive;
		}

		@Override
		public Map<RepoCard, CatalogCard> call() throws Exception {
			Map<RepoCard, CatalogCard> catalogs = new HashMap<>();

			downloads.keySet().forEach( ( r ) -> {
				try {
					catalogs.put( r, CatalogCard.load( r, downloads.get( r ).get().getInputStream() ) );
					log.info( "Catalog loaded for " + r );
				} catch( IOException | ExecutionException | InterruptedException exception ) {
					exception.printStackTrace();
				}
			} );

			program.getTaskManager().submit( new GetProductCardsTask( catalogs, interactive ) );

			return catalogs;
		}

	}

	private class GetProductCardsTask extends Task<Void> {

		private Map<RepoCard, CatalogCard> catalogs;

		private boolean interactive;

		public GetProductCardsTask( Map<RepoCard, CatalogCard> catalogs, boolean interactive ) {
			this.catalogs = catalogs;
			this.interactive = interactive;
		}

		@Override
		public Void call() throws Exception {
			Map<RepoCard, Set<Task<Download>>> downloads = new HashMap<>();

			catalogs.keySet().forEach( ( repo ) -> {
				CatalogCard catalog = catalogs.get( repo );

				Set<Task<Download>> repoDownloads = downloads.computeIfAbsent( repo, ( k ) -> new HashSet<>() );

				catalog.getProducts().forEach( ( product ) -> {
					URI uri = repoClient.getProductUri( repo, product, "product", "card" );
					repoDownloads.add( (Task<Download>)program.getTaskManager().submit( new DownloadTask( program, uri ) ) );
				} );
			} );

			program.getTaskManager().submit( new DownloadProductCardCollector( downloads, interactive ) );

			return null;
		}

	}

	private class DownloadProductCardCollector extends Task<Void> {

		private Map<RepoCard, Set<Task<Download>>> downloads;

		private boolean interactive;

		public DownloadProductCardCollector( Map<RepoCard, Set<Task<Download>>> downloads, boolean interactive ) {
			this.downloads = downloads;
			this.interactive = interactive;
		}

		@Override
		public Void call() throws Exception {
			Map<RepoCard, Set<ProductCard>> products = new HashMap<>();

			boolean connectionErrors = false;
			downloads.keySet().forEach( ( r ) -> {
				Set<Task<Download>> repoDownloads = downloads.get( r );
				repoDownloads.forEach( ( t ) -> {
					Set<ProductCard> productSet = products.computeIfAbsent( r, ( k ) -> new HashSet<>() );
					try {
						ProductCard product = new ProductCard().load( t.get().getInputStream(), t.get().getSource() );
						productSet.add( product );
						log.info( "Catalog loaded for " + product );
					} catch( IOException | ExecutionException | InterruptedException exception ) {
						exception.printStackTrace();
						// FIXME Need to set connectionErrors = true;
					}
				} );
			} );

			program.getTaskManager().submit( new DetermineUpdateableVersionsTask( products, interactive, connectionErrors ) );

			return null;
		}

	}

	private class DetermineUpdateableVersionsTask extends Task<Map<ProductCard, RepoCard>> {

		private Map<RepoCard, Set<ProductCard>> products;

		private boolean interactive;

		private boolean connectionErrors;

		public DetermineUpdateableVersionsTask( Map<RepoCard, Set<ProductCard>> products, boolean interactive, boolean connectionErrors ) {
			this.products = products;
			this.interactive = interactive;
		}

		@Override
		public Map<ProductCard, RepoCard> call() throws Exception {
			// If the installed versions were added to the incoming map then the
			// sorting logic would find them properly and any version that is already
			// installed can simply be ignored/removed.
			RepoCard programInstalledRepo = new RepoCard( "installed" );
			products.put( programInstalledRepo, program.getProductManager().getInstalledProductCards() );

			// Need to determine the latest version from the installed products and
			// those versions available from the repositories. Luckily the versions
			// from the repositories are in sets that can easily be sorted to find
			// latest one. A map from product back to repo will need to be maintained
			// to know what repo it came from.

			// Create the product repo map and the product versions map
			Map<ProductCard, RepoCard> productRepos = new HashMap<>();
			Map<String, List<ProductCard>> productVersions = new HashMap<>();
			products.keySet().forEach( ( r ) -> products.get( r ).forEach( ( p ) -> {
				productRepos.put( p, r );
				productVersions.computeIfAbsent( p.getProductKey(), ( k ) -> new ArrayList<>() ).add( p );
			} ) );

			// The key for the map is a ProductCard key
			Map<ProductCard, RepoCard> cards = new HashMap<>();

			// Sort all the latest product versions to the top of each list
			Comparator<ProductCard> comparator = new ProductCardComparator( ProductCardComparator.Field.RELEASE ).reversed();
			productVersions.keySet().forEach( ( k ) -> {
				productVersions.get( k ).sort( comparator );
				ProductCard version = productVersions.get( k ).get( 0 );
				RepoCard repo = productRepos.get( version );
				if( repo != programInstalledRepo ) cards.put( version, repo );

				ProductCard current = program.getProductManager().getInstalledProductCard( version );
				if( current != null ) log.debug( "Installed: " + current.getProductKey() + " " + current.getRelease() );
				log.debug( "Available: " + version.getProductKey() + " " + version.getRelease() );
				log.info( "Latest version: " + version + " found in: " + repo );
			} );

			program.getTaskManager().submit( new HandleCheckForUpdatesActionTask( cards, interactive, connectionErrors ) );

			return cards;
		}

	}

	private class HandleCheckForUpdatesActionTask extends Task<Void> {

		private Map<ProductCard, RepoCard> cards;

		private boolean interactive;

		private boolean connectionErrors;

		public HandleCheckForUpdatesActionTask( Map<ProductCard, RepoCard> cards, boolean interactive, boolean connectionErrors ) {
			this.cards = cards;
			this.interactive = interactive;
			this.connectionErrors = connectionErrors;
		}

		@Override
		public Void call() throws Exception {
			boolean available = cards.size() > 0;

			if( interactive ) {
				if( available ) {
					openProductTool();
				} else {
					notifyUserOfNoUpdates();
				}
			} else {
				switch( program.getProductManager().getFoundOption() ) {
					case SELECT: {
						notifyUserOfUpdates();
						break;
					}
					case STORE:
					case APPLY: {
						program.getTaskManager().submit( new StageUpdates( cards, interactive ) );
						break;
					}
				}
			}

			return null;
		}

		private void notifyUserOfNoUpdates() {
			String title = program.getResourceBundle().getString( BundleKey.UPDATE, "updates" );
			String updatesNotAvailable = program.getResourceBundle().getString( BundleKey.UPDATE, "updates-not-available" );
			String updatesCannotConnect = program.getResourceBundle().getString( BundleKey.UPDATE, "updates-source-cannot-connect" );
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

	}

	private class StageUpdates extends Task<Set<Future<ProductUpdate>>> {

		private Map<ProductCard, RepoCard> cardsAndRepos;

		private boolean interactive;

		public StageUpdates( Map<ProductCard, RepoCard> cardsAndRepos, boolean interactive ) {
			super( program.getResourceBundle().getString( BundleKey.UPDATE, "task-updates-stage-selected" ) );
			this.cardsAndRepos = cardsAndRepos;
			this.interactive = interactive;
		}

		@Override
		public Set<Future<ProductUpdate>> call() throws Exception {
			Path stageFolder = program.getDataFolder().resolve( ProductManager.UPDATE_FOLDER_NAME );

			log.debug( "Number of packs to stage: " + cardsAndRepos.size() );
			log.trace( "Pack stage folder: " + stageFolder );

			try {
				Files.createDirectories( stageFolder );
			} catch( IOException exception ) {
				log.warn( "Error creating update stage folder: " + stageFolder, exception );
				return Set.of();
			}

			// The remaining code in this method is not particularly obvious but is
			// done this way for a better user experience. The idea is to submit the
			// product download resource tasks first then the tasks to build product
			// updates from those resources next and finally submits the task that
			// collects all the product update futures.

			Set<StageProductResources> updateTasks = cardsAndRepos.keySet().stream().map( ( card ) -> {
				try {
					RepoCard repo = cardsAndRepos.get( card );
					Path updatePack = stageFolder.resolve( getStagedUpdateFileName( card ) );

					// The returned ProductResource objects contain the product resource download futures
					Set<ProductResource> resources = startProductResourceDownloads( repo, card, updatePack );

					// Return the task that will produce the ProductUpdate but don't submit it here
					return new StageProductResources( repo, card, resources, updatePack );
				} catch( Exception exception ) {
					return null;
				}
			} ).filter( Objects::nonNull ).collect( Collectors.toSet() );

			// Submit the task that to produce the ProductUpdates here
			Set<Future<ProductUpdate>> updateFutures = updateTasks.stream().map( ( task ) -> program.getTaskManager().submit( task ) ).collect( Collectors.toSet() );

			program.getTaskManager().submit( new CollectProductUpdateFutures( updateFutures, interactive ) );

			return updateFutures;
		}

		private String getStagedUpdateFileName( ProductCard card ) {
			return card.getGroup() + "." + card.getArtifact() + ".pack";
		}

		private Set<ProductResource> startProductResourceDownloads( RepoCard repo, ProductCard card, Path updatePack ) throws InterruptedException, ExecutionException {
			return program.getTaskManager().submit( new DownloadProductResources( repo, card, updatePack ) ).get();
		}

	}

	private class DownloadProductResources extends Task<Set<ProductResource>> {

		private RepoCard repo;

		private ProductCard card;

		private Path localPackPath;

		public DownloadProductResources( RepoCard repo, ProductCard card, Path localPackPath ) {
			// FIXME Localize this task name
			super( "Stage update: " + card.getName() + " " + card.getVersion() );
			this.repo = repo;
			this.card = card;
			this.localPackPath = localPackPath;
		}

		@Override
		public Set<ProductResource> call() throws Exception {
			// Determine all the resources to download.
			PackProvider provider = new PackProvider( program, repo, repoClient, card );
			Set<ProductResource> resources = provider.getResources();
			log.debug( "Product resource count: " + resources.size() );

			resources.forEach( ( resource ) -> resource.setFuture( program.getTaskManager().submit( new DownloadTask( program, getSchemeResolvedUri( resource.getUri() ) ) ) ) );

			return resources;
		}

		private URI getSchemeResolvedUri( URI uri ) {
			if( uri == null ) return null;
			return uri.getScheme() == null ? Paths.get( uri.getPath() ).toUri() : uri;
		}

	}

	private class StageProductResources extends Task<ProductUpdate> {

		private RepoCard repo;

		private ProductCard product;

		private Set<ProductResource> resources;

		private Path localPackPath;

		public StageProductResources( RepoCard repo, ProductCard product, Set<ProductResource> resources, Path localPackPath ) {
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
			if( manager.isInstalled( product ) ) installFolder = manager.getInstalledProductCard( product ).getInstallFolder();

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

	private class CollectProductUpdateFutures extends Task<Set<ProductUpdate>> {

		private Set<Future<ProductUpdate>> updateFutures;

		private boolean interactive;

		public CollectProductUpdateFutures( Set<Future<ProductUpdate>> updateFutures, boolean interactive ) {
			this.updateFutures = updateFutures;
			this.interactive = interactive;
		}

		@Override
		public Set<ProductUpdate> call() throws Exception {
			Set<ProductUpdate> updates = updateFutures.stream().map( ( future ) -> {
				try {
					return future.get();
				} catch( Exception exception ) {
					return null;
				}
			} ).filter( Objects::nonNull ).collect( Collectors.toSet() );

			if( program.getProductManager().getFoundOption() == ProductManager.FoundOption.APPLY ) {
				program.getTaskManager().submit( new ApplyUpdates( updates, interactive ) );
			}

			return updates;
		}

	}

	private class ApplyUpdates extends Task<Void> {

		private Set<ProductUpdate> updates;

		private boolean interactive;

		public ApplyUpdates( Set<ProductUpdate> updates, boolean interactive ) {
			this.updates = updates;
			this.interactive = interactive;
		}

		@Override
		public Void call() {
			if( interactive ) {
				Platform.runLater( this::showAlert );
			} else {
				Platform.runLater( this::showNotice );
			}

			return null;
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
				program.getProductManager().userApplyStagedUpdates();
			}
		}

		private void showNotice() {
			String header = program.getResourceBundle().getString( BundleKey.UPDATE, "restart-required" );
			String message = program.getResourceBundle().getString( BundleKey.UPDATE, "restart-recommended" );

			Notice notice = new Notice( header, message, () -> Platform.runLater( this::showAlert ) );
			program.getNoticeManager().addNotice( notice );
		}

	}

}
