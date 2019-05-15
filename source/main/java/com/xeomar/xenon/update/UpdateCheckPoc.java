package com.xeomar.xenon.update;

import com.xeomar.product.ProductCard;
import com.xeomar.util.LogUtil;
import com.xeomar.xenon.Program;
import com.xeomar.xenon.task.Task;
import org.slf4j.Logger;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
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
		program.getTaskManager().submit( new CheckForUpdatesTask() );
	}

	private class CheckForUpdatesTask extends Task<Map<RepoCard, Task<Download>>> {

		@Override
		public Map<RepoCard, Task<Download>> call() throws Exception {
			Map<RepoCard, Task<Download>> downloads = new HashMap<>();

			program.getProductManager().getRepos().forEach( ( r ) -> {
				log.info( "Creating catalog downloads for repo: " + r.getName() );
				URI uri = repoClient.getCatalogUri( r );
				downloads.put( r, (Task<Download>)program.getTaskManager().submit( new DownloadTask( program, uri ) ) );
			} );

			program.getTaskManager().submit( new DownloadCatalogCardCollector( downloads ) );

			return downloads;
		}

	}

	private class DownloadCatalogCardCollector extends Task<Map<RepoCard, CatalogCard>> {

		private Map<RepoCard, Task<Download>> downloads;

		public DownloadCatalogCardCollector( Map<RepoCard, Task<Download>> downloads ) {
			this.downloads = downloads;
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

			program.getTaskManager().submit( new GetProductCardsTask( catalogs ) );

			return catalogs;
		}

	}

	private class GetProductCardsTask extends Task<Void> {

		private Map<RepoCard, CatalogCard> catalogs;

		public GetProductCardsTask( Map<RepoCard, CatalogCard> catalogs ) {
			this.catalogs = catalogs;
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

			program.getTaskManager().submit( new DownloadProductCardCollector( downloads ) );

			return null;
		}

	}

	private class DownloadProductCardCollector extends Task<Void> {

		private Map<RepoCard, Set<Task<Download>>> downloads;

		public DownloadProductCardCollector( Map<RepoCard, Set<Task<Download>>> downloads ) {
			this.downloads = downloads;
		}

		@Override
		public Void call() throws Exception {
			Map<RepoCard, Set<ProductCard>> products = new HashMap<>();

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
					}
				} );
			} );

			return null;
		}

	}

}
