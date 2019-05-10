package com.xeomar.xenon.update;

import com.xeomar.product.ProductCard;
import com.xeomar.util.LogUtil;
import com.xeomar.util.OperatingSystem;
import com.xeomar.util.UriUtil;
import com.xeomar.xenon.Program;
import org.slf4j.Logger;

import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class V2RepoClient implements RepoClient {

	private static final Logger log = LogUtil.get( MethodHandles.lookup().lookupClass() );

	private Program program;

	public V2RepoClient( Program program ) {
		this.program = program;
	}

	@Override
	public Set<CatalogCard> getCatalogCards( Set<RepoCard> repos ) {
		Map<Future<Download>, RepoCard> matchingRepoCards = new HashMap<>();


		// Go through each repo and create a download task for each catalog
		Set<Future<Download>> catalogCardFutures = repos.stream().map( ( repo ) -> {
			DownloadTask task = new DownloadTask( program, UriUtil.addToPath( getRepoApi( repo ), "catalog" ) );
			Future<Download> future = program.getTaskManager().submit( task );
			matchingRepoCards.put( future, repo );
			return future;
		} ).collect( Collectors.toSet() );

		// Go through each future and download the catalog card
		Set<CatalogCard> catalogCards = new HashSet<>();
		for( Future<Download> future : catalogCardFutures ) {
			try {
				Download download = future.get( 10, TimeUnit.SECONDS );
				try( InputStream input = download.getInputStream() ) {
					catalogCards.add( CatalogCard.load( matchingRepoCards.get( future ), input ) );
				} catch( Exception exception ) {
					log.warn( "Error downloading catalog card: " + download.getSource(), exception );
				}
			} catch( Exception exception ) {
				log.warn( "Error downloading catalog card", exception );
			}
		}

		return catalogCards;
	}

	public Set<ProductCard> getProductCards( Set<CatalogCard> catalogs ) {
		String platform = OperatingSystem.getFamily().toString().toLowerCase();

		Set<Future<Download>> futures = new HashSet<>();
		for( CatalogCard catalog : catalogs ) {
			for( String p : catalog.getProducts() ) {
				URI uri = getRepoApi( catalog.getRepo() ).resolve( p ).resolve( platform ).resolve( "product" ).resolve( "card" ).normalize();
				DownloadTask task = new DownloadTask( program, uri );
				futures.add( program.getTaskManager().submit( task ) );
			}
		}

		// Collect all the product cards into a set and return it
		Set<ProductCard> productCards = new HashSet<>();
		for( Future<Download> future : futures ) {
			try {
				Download download = future.get( 10, TimeUnit.SECONDS );
				try( InputStream input = download.getInputStream() ) {
					productCards.add( new ProductCard().load( input ) );
				} catch( Exception exception ) {
					log.warn( "Error downloading product card: " + download.getSource(), exception );
				}
			} catch( Exception exception ) {
				log.warn( "Error downloading product card", exception );
			}
		}

		return productCards;
	}

	private URI getRepoApi( RepoCard repo ) {
		return UriUtil.addToPath( URI.create( repo.getRepo() ), "v2" );
	}

}
