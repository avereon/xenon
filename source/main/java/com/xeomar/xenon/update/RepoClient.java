package com.xeomar.xenon.update;

import com.xeomar.product.ProductCard;

import java.net.URI;
import java.util.Set;

public interface RepoClient {

	URI getCatalogUri( RepoCard repo );

	URI getProductUri( RepoCard repo, String product, String asset, String format );

	Set<CatalogCard> getCatalogCards( Set<RepoCard> repos );

	Set<ProductCard> getProductCards( Set<CatalogCard> catalogs );

}
