package com.avereon.xenon.product;

import com.avereon.util.XmlDescriptor;
import com.avereon.xenon.Program;
import org.w3c.dom.Node;

import java.net.URI;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Future;

public class JnlpProvider implements ProductResourceProvider {

	private Program program;

	private XmlDescriptor descriptor;

	private URI codebase;

	public JnlpProvider( XmlDescriptor descriptor, Program program, URI codebase ) {
		this.program = program;
		this.descriptor = descriptor;
	}

	@Override
	public Set<ProductResource> getResources() throws Exception {
		return getResources( codebase, descriptor );
	}

	private Set<ProductResource> getResources( URI codebase, XmlDescriptor descriptor ) throws Exception {
		//URI codebase = new URI( descriptor.getValue( "/jnlp/@codebase" ) );

		Set<ProductResource> resources = new HashSet<>();

		// Resolve all the files to download.
		String[] jars = getResources( descriptor, "jar/@href" );
		String[] libs = getResources( descriptor, "lib/@href" );
		String[] natives = getResources( descriptor, "nativelib/@href" );
		String[] extensions = getResources( descriptor, "extension/@href" );

		for( String jar : jars ) {
			URI uri = codebase.resolve( jar );
			resources.add( new ProductResource( ProductResource.Type.FILE, uri ) );
		}
		for( String lib : libs ) {
			URI uri = codebase.resolve( lib );
			resources.add( new ProductResource( ProductResource.Type.PACK, uri ) );
		}
		for( String lib : natives ) {
			URI uri = codebase.resolve( lib );
			resources.add( new ProductResource( ProductResource.Type.PACK, uri ) );
		}
		for( String extension : extensions ) {
			Future<Download> future = program.getTaskManager().submit( new DownloadTask( program, codebase.resolve( extension ) ) );
			resources.addAll( new JnlpProvider( new XmlDescriptor( future.get().getInputStream() ), program, codebase ).getResources() );
		}

		return resources;
	}

	private String[] getResources( XmlDescriptor descriptor, String path ) {
		String os = System.getProperty( "os.name" );
		String arch = System.getProperty( "os.arch" );

		String[] uris;
		Set<String> resources = new HashSet<>();

		// Determine the resources.
		Node[] nodes = descriptor.getNodes( "/jnlp/resources" );
		for( Node node : nodes ) {
			XmlDescriptor resourcesDescriptor = new XmlDescriptor( node );
			Node osNameNode = node.getAttributes().getNamedItem( "os" );
			Node osArchNode = node.getAttributes().getNamedItem( "arch" );

			String osName = osNameNode == null ? null : osNameNode.getTextContent();
			String osArch = osArchNode == null ? null : osArchNode.getTextContent();

			// Determine what resources should not be included.
			if( osName != null && !os.startsWith( osName ) ) continue;
			if( osArch != null && !arch.equals( osArch ) ) continue;

			uris = resourcesDescriptor.getValues( path );
			if( uris != null ) resources.addAll( Arrays.asList( uris ) );
		}

		return resources.toArray( new String[ resources.size() ] );
	}

}
