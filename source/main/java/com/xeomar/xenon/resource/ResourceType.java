package com.xeomar.xenon.resource;

import com.xeomar.xenon.Program;
import com.xeomar.product.Product;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * <p>
 * The ResourceType class represents a resource type. A resource must always
 * have a resource type and may be directly specified, or determined by the the
 * URI. Resource types may have one or more associated codecs. {@link Scheme},
 * ResourceType and {@link Codec} work together to store and restore resources.
 * <h2>Determining Resource Type</h2>
 * Resource types can usually be determined by using the resource URI. Some
 * resource types can be determined using just the URI scheme. If the resource
 * type cannot be determined by the URI scheme then it is usually a stateful
 * resource with transient connections.
 * <p>The resource type is determined by
 * comparing the resource name to registered codecs. It is possible to match
 * more than one codec. In this case the user might need to choose which codec
 * to use to determine the resource type. If all the possible codecs belong to
 * the same resource type then the user does not have to choose.
 * <p>
 * If the resource type cannot be determined by name, then the first line of the
 * content can be used to match a codec.
 * <p>
 * If the fist line cannot determine the resource type then the content type may
 * be able to be used. This may not be a reliable method since the content type
 * may be specified in a number of ways. No matter how it is specified it should
 * always be considered a best guess.
 * <p>
 * If the resource type still cannot be determined then one of the two default
 * resource types should be used. If, by reading the content, the resource is
 * determined to be text then the text resource type is used. Otherwise, the
 * binary data type is used.
 * <p>
 * When a resource is saved it might also be necessary to update the resource
 * type.
 * <h2>Determining a Resource Editor</h2>
 * Once the resource type is determined an appropriate editor can be created for
 * it. It is possible to have more than one editor registered for the resource
 * type. In this case a default may be specified or the user will need to
 * choose.
 *
 * @author ecco
 */
public abstract class ResourceType implements Comparable<ResourceType> {

	private final String key = getClass().getName();

	private Product product;

	private String rbKey;

	private Set<Codec> codecs;

	private Codec defaultCodec;

	public ResourceType( Product product, String resourceBundleKey ) {
		if( product == null ) throw new NullPointerException( "Product cannot be null" );
		if( resourceBundleKey == null ) throw new NullPointerException( "Resource bundle key cannot be null" );
		this.product = product;
		this.rbKey = resourceBundleKey;
		this.codecs = new CopyOnWriteArraySet<Codec>();
	}

	public Product getProduct() {
		return product;
	}

	public String getKey() {
		return key;
	}

	public String getName() {
		return product.getResourceBundle().getString( "resource", rbKey + "-name" );
	}

	public String getDescription() {
		return product.getResourceBundle().getString( "resource", rbKey + "-description" );
	}

	public boolean isUserType() {
		return true;
	}

	public Codec getDefaultCodec() {
		return defaultCodec;
	}

	public void setDefaultCodec( Codec codec ) {
		addCodec( this.defaultCodec = codec );
	}

	public Set<Codec> getCodecs() {
		return Collections.unmodifiableSet( codecs );
	}

	public void addCodec( Codec codec ) {
		if( codec == null ) return;
		synchronized( codec ) {
			codecs.add( codec );
			codec.setResourceType( this );
		}
	}

	public void removeCodec( Codec codec ) {
		if( codec == null ) return;
		synchronized( codec ) {
			codecs.remove( codec );
			codec.setResourceType( null );
			if( getDefaultCodec() == codec ) setDefaultCodec( null );
		}
	}

	/**
	 * Initialize a resource with default state. This method should provide the
	 * specified resource with a default state prior to being used in an editor.
	 * <p>
	 * Unlike the {@link #resourceDialog(Program, Resource)} method this method is
	 * always called whenever a resource is new, opened or restored. This method
	 * should not be used for user interaction. User interaction should be
	 * implemented in the {@link #resourceDialog(Program, Resource)} method.
	 * <p>
	 * Note: This method is called using a task thread and is not safe to use
	 * directly on UI components. <br>
	 *
	 * @param program
	 * @param resource
	 * @return True if the resource was initialized, false otherwise. A value of false will keep the resource from being opened and an editor from being created.
	 * @throws ResourceException if the resource failed to be initialized.
	 */
	public boolean resourceDefault( Program program, Resource resource ) throws ResourceException {
		return true;
	}

	/**
	 * This method is called just before a new resource is opened to allow for
	 * user interaction. This method is valuable if the resource requires user
	 * interaction when creating new resources.
	 * <p>
	 * Unlike the {@link #resourceDefault(Program, Resource)} method this method is
	 * only called for new resources when the URI is null. If the URI is not null
	 * this method will not be called as is the case for opening or restoring
	 * existing resources.
	 * <p>
	 * Note: This method is called using a task thread and is not safe to use
	 * directly on UI components.
	 *
	 * @param program
	 * @param resource
	 * @return True if the resource was opened, false otherwise. A value of false will keep the resource from being opened and an editor from being created.
	 * @throws ResourceException if the resource failed to be opened.
	 */
	public boolean resourceDialog( Program program, Resource resource ) throws ResourceException {
		return true;
	}

	@Override
	public String toString() {
		return getName();
	}

	@Override
	public int compareTo( ResourceType type ) {
		return getName().compareTo( type.getName() );
	}

	public Codec getCodecByMediaType( String type ) {
		if( type == null ) return null;

		for( Codec codec : codecs ) {
			if( codec.isSupportedMediaType( type )) return codec;
		}

		return null;
	}

	public Codec getCodecByFileName( String name ) {
		if( name == null ) return null;

		for( Codec codec : codecs ) {
			if( codec.isSupportedFileName( name ) ) return codec;
		}

		return null;
	}

	public Codec getCodecByFirstLine( String line ) {
		if( line == null ) return null;

		for( Codec codec : codecs ) {
			if( codec.isSupportedFirstLine( line )) return codec;
		}

		return null;
	}

}
