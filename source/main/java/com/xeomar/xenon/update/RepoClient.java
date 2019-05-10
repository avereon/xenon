package com.xeomar.xenon.update;

import com.xeomar.product.ProductCard;

import java.util.Set;

public interface RepoClient {

	Set<CatalogCard> getCatalogCards( Set<RepoCard> repos );

	Set<ProductCard> getProductCards( Set<CatalogCard> catalogs );

	DownloadTask getProductCardDownloadTask( ProductCard card );

}
