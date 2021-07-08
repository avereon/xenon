package com.avereon.xenon.product;

import com.avereon.log.LazyEval;
import com.avereon.product.CatalogCard;
import com.avereon.product.ProductCard;
import com.avereon.product.RepoCard;
import com.avereon.util.OperatingSystem;
import com.avereon.util.UriUtil;
import com.avereon.xenon.Program;
import lombok.CustomLog;

import java.io.InputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@CustomLog
public class V2RepoClient implements RepoClient {

	private final Program program;

	public V2RepoClient( Program program ) {
		this.program = program;
	}

	@Override
	public URI getCatalogUri( RepoCard repo ) {
		return UriUtil.addToPath( getRepoApi( repo ), "catalog" );
	}

	@Override
	public URI getProductUri( RepoCard repo, String product, String asset, String format ) {
		URI uri = getRepoApi( repo );
		uri = UriUtil.addToPath( uri, product );
		uri = UriUtil.addToPath( uri, OperatingSystem.getFamily().toString().toLowerCase() );
		uri = UriUtil.addToPath( uri, asset );
		uri = UriUtil.addToPath( uri, format );
		return uri;
	}

	@Deprecated
	@Override
	public Set<CatalogCard> getCatalogCards( Set<RepoCard> repos ) {
		// WORKAROUND Repo cards need to be associated to download tasks
		Map<Future<Download>, RepoCard> matchingRepoCards = new HashMap<>();

		// Go through each repo and create a download task for each catalog
		Set<Future<Download>> catalogCardFutures = repos.stream().map( ( repo ) -> {
			DownloadTask task = new DownloadTask( program, getCatalogUri( repo ) );
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
					catalogCards.add( CatalogCard.fromJson( matchingRepoCards.get( future ), input ) );
				} catch( Exception exception ) {
					log.atWarn( exception ).log( "Error downloading catalog card: %s", LazyEval.of( download::getSource ) );
				}
			} catch( Exception exception ) {
				log.atWarn( exception).log( "Error downloading catalog card" );
			}
		}

		return catalogCards;
	}

	@Deprecated
	public Set<ProductCard> getProductCards( Set<CatalogCard> catalogs ) {
		Set<Future<Download>> futures = new HashSet<>();
		for( CatalogCard catalog : catalogs ) {
			for( String p : catalog.getProducts() ) {
				URI uri = getProductUri( catalog.getRepo(), p, "product", "card" );
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
					productCards.add( new ProductCard().fromJson( input, download.getSource() ) );
				} catch( Exception exception ) {
					log.atWarn( exception ).log( "Error downloading product card: %s", LazyEval.of( download::getSource ) );
				}
			} catch( Exception exception ) {
				log.atWarn( exception).log( "Error downloading product card" );
			}
		}

		return productCards;
	}

	private URI getRepoApi( RepoCard repo ) {
		return URI.create( repo.getUrl() );
	}

}
