package com.xeomar.xenon.update;

import com.xeomar.product.ProductCard;
import com.xeomar.product.RepoCard;
import com.xeomar.xenon.Program;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;

public class PackProvider implements ProductResourceProvider {

	private Program program;

	private RepoCard repo;

	private RepoClient repoClient;

	private ProductCard card;

	public PackProvider( Program program, RepoCard repo, RepoClient client, ProductCard card ) {
		this.program = program;
		this.repo = repo;
		this.repoClient = client;
		this.card = card;
	}

	@Override
	public Set<ProductResource> getResources() throws URISyntaxException {
		Set<ProductResource> resources = new HashSet<>();

		// Add the product pack as a resource
		URI codebase = repoClient.getProductUri( repo, card.getArtifact(), "product", "pack" );
		resources.add( new ProductResource( ProductResource.Type.PACK, codebase ) );

//		// Determine what other resources need to be downloaded
//		String[] files = card.getResourceUris( "file" );
//		String[] packs = card.getResourceUris( "pack" );
//		String[] jnlps = card.getResourceUris( "jnlp" );
//
//		for( String file : files ) {
//			URI uri = codebase.resolve( file );
//			resources.add( new ProductResource( ProductResource.Type.FILE, uri ) );
//		}
//		for( String pack : packs ) {
//			URI uri = codebase.resolve( pack );
//			resources.add( new ProductResource( ProductResource.Type.PACK, uri ) );
//		}
//		for( String jnlp : jnlps ) {
//			URI uri = codebase.resolve( jnlp );
//			Future<XmlDescriptor> future = program.getExecutor().submit( new DescriptorDownloadTask( program, uri ) );
//			resources.addAll( new JnlpProvider( future.get(), program ).getResources( codebase ) );
//		}

		return resources;
	}

}
