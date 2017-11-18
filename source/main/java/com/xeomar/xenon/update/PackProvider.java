package com.xeomar.xenon.update;

import com.xeomar.product.ProductCard;
import com.xeomar.xenon.Program;

import java.util.HashSet;
import java.util.Set;

public class PackProvider implements ProductResourceProvider {

	private Program program;

	private ProductCard card;

	public PackProvider( ProductCard card, Program program ) {
		this.card = card;
		this.program = program;
	}

	@Override
	public Set<ProductResource> getResources() throws Exception {
		// FIXME How am I going to the the resource base URI
		// If all the URIs are absolute then I don't have to worry about it
		// But it's not very convenient to have to know absolute URIs
		// Especially when hosted on the web.

//		URI codebase = card.getSourceUri();
		Set<ProductResource> resources = new HashSet<>();

		// Determine all the resources that need to be downloaded.
		String[] files = card.getResourceUris( "file" );
		String[] packs = card.getResourceUris( "pack" );
		String[] jnlps = card.getResourceUris( "jnlp" );

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
//			Future<XmlDescriptor> future = taskManager.submit( new DescriptorDownloadTask( uri ) );
//			resources.addAll( new JnlpProvider( future.get(), taskManager ).getResources() );
//		}

		return resources;
	}

}
