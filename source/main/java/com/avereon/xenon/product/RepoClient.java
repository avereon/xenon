package com.avereon.xenon.product;

import com.avereon.product.RepoCard;

import java.net.URI;

public interface RepoClient {

	URI getCatalogUri( RepoCard repo );

	URI getProductUri( RepoCard repo, String product, String asset, String format );

}
