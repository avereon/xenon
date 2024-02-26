package com.avereon.xenon.product;

import com.avereon.product.CatalogCard;
import com.avereon.product.ProductCard;
import com.avereon.product.RepoCard;

import java.net.URI;
import java.util.Set;

public interface RepoClient {

	URI getCatalogUri( RepoCard repo );

	URI getProductUri( RepoCard repo, String product, boolean osSpecific, String asset, String format );

	Set<CatalogCard> getCatalogCards( Set<RepoCard> repos );

	Set<ProductCard> getProductCards( Set<CatalogCard> catalogs );

}
