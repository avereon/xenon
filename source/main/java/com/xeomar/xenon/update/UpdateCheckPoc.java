package com.xeomar.xenon.update;

import com.xeomar.product.ProductCard;
import com.xeomar.product.ProductCardComparator;
import com.xeomar.util.LogUtil;
import com.xeomar.xenon.BundleKey;
import com.xeomar.xenon.Program;
import com.xeomar.xenon.notice.Notice;
import com.xeomar.xenon.resource.type.ProgramProductType;
import com.xeomar.xenon.task.Task;
import javafx.application.Platform;
import org.slf4j.Logger;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.net.URI;
import java.util.*;
import java.util.concurrent.ExecutionException;

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

	private class CheckForUpdatesTask extends Task<Map<RepoCard, Task<Download>>> {

		private boolean interactive;

		public CheckForUpdatesTask( boolean interactive ) {
			this.interactive = interactive;
		}

		@Override
		public Map<RepoCard, Task<Download>> call() throws Exception {
			Map<RepoCard, Task<Download>> downloads = new HashMap<>();

			program.getProductManager().getRepos().stream().filter( RepoCard::isEnabled ).forEach( ( r ) -> {
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
				CatalogCard c = catalogs.get( repo );

				Set<Task<Download>> repoDownloads = downloads.computeIfAbsent( repo, ( k ) -> new HashSet<>() );

				c.getProducts().forEach( ( product ) -> {
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
					String title = program.getResourceBundle().getString( BundleKey.UPDATE, "updates" );
					String updatesNotAvailable = program.getResourceBundle().getString( BundleKey.UPDATE, "updates-not-available" );
					String updatesCannotConnect = program.getResourceBundle().getString( BundleKey.UPDATE, "updates-source-cannot-connect" );
					final String message = connectionErrors ? updatesCannotConnect : updatesNotAvailable;
					Platform.runLater( () -> program.getNoticeManager().addNotice( new Notice( title, message ) ) );
				}
			} else {
				// TODO There is more work to do
			}

			return null;
		}

		private void openProductTool() {
			URI uri = URI.create( ProgramProductType.URI + "#" + ProgramProductType.UPDATES );
			Platform.runLater( () -> program.getResourceManager().open( uri ) );
		}

	}

}
