package com.avereon.xenon.product;

import com.avereon.product.RepoCard;
import com.avereon.util.OperatingSystem;
import com.avereon.util.UriUtil;
import com.avereon.xenon.Xenon;
import lombok.CustomLog;

import java.net.URI;

@CustomLog
public class V2RepoClient implements RepoClient {

	private final Xenon program;

	public V2RepoClient( Xenon program ) {
		this.program = program;
	}

	@Override
	public URI getCatalogUri( RepoCard repo ) {
		return UriUtil.addToPath( getRepoApi( repo ), "catalog" );
	}

	@Override
	public URI getProductUri( RepoCard repo, String product, String asset, String format ) {
		String os = OperatingSystem.getFamily().toString().toLowerCase();

		URI uri = getRepoApi( repo );
		uri = UriUtil.addToPath( uri, product );
		uri = UriUtil.addToPath( uri, os );
		uri = UriUtil.addToPath( uri, asset );
		uri = UriUtil.addToPath( uri, format );
		return uri;
	}

	private URI getRepoApi( RepoCard repo ) {
		return URI.create( repo.getUrl() );
	}

}
