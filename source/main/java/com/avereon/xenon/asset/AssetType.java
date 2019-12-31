package com.avereon.xenon.asset;

import com.avereon.product.Product;
import com.avereon.util.LogUtil;
import com.avereon.xenon.Program;
import org.slf4j.Logger;

import java.lang.invoke.MethodHandles;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * <p>
 * The AssetType class represents an asset type. An asset must always
 * have an asset type and may be directly specified, or determined by the the
 * URI. Asset types may have one or more associated codecs. {@link Scheme},
 * {@link AssetType} and {@link Codec} work together to save and load assets.
 * <h2>Determining Asset Type</h2>
 * Asset types can usually be determined by using the asset URI. Some
 * asset types can be determined using just the URI scheme. If the asset
 * type cannot be determined by the URI scheme then it is usually a stateful
 * asset with transient connections.
 * <p>The asset type is determined by
 * comparing the asset name to registered codecs. It is possible to match
 * more than one codec. In this case the user might need to choose which codec
 * to use to determine the asset type. If all the possible codecs belong to
 * the same asset type then the user does not have to choose.
 * <p>
 * If the asset type cannot be determined by name, then the first line of the
 * content can be used to match a codec.
 * <p>
 * If the fist line cannot determine the asset type then the content type may
 * be able to be used. This may not be a reliable method since the content type
 * may be specified in a number of ways. No matter how it is specified it should
 * always be considered a best guess.
 * <p>
 * If the asset type still cannot be determined then one of the two default
 * asset types should be used. If, by reading the content, the asset is
 * determined to be text then the text asset type is used. Otherwise, the
 * binary data type is used.
 * <p>
 * When an asset is saved it might also be necessary to update the asset type.
 * <h2>Determining a Asset Tool</h2>
 * Once the asset type is determined an appropriate tool can be created for
 * it. It is possible to have more than one tool registered for the asset
 * type. In this case a default may be specified or the user will need to
 * choose.
 *
 * @author ecco
 */
public abstract class AssetType implements Comparable<AssetType> {

	private static final Logger log = LogUtil.get( MethodHandles.lookup().lookupClass() );

	private final String key = getClass().getName();

	private Product product;

	private String rbKey;

	private Set<Codec> codecs;

	private Codec defaultCodec;

	public AssetType( Product product, String rbKey ) {
		if( product == null ) throw new NullPointerException( "Product cannot be null" );
		if( rbKey == null ) throw new NullPointerException( "Resource bundle key cannot be null" );
		this.product = product;
		this.rbKey = rbKey;
		this.codecs = new CopyOnWriteArraySet<>();
	}

	public Product getProduct() {
		return product;
	}

	public String getKey() {
		return key;
	}

	public String getName() {
		return product.rb().text( "asset", rbKey + "-name" );
	}

	public String getDescription() {
		return product.rb().text( "asset", rbKey + "-description" );
	}

	/**
	 * Is this asset type a user defined asset type. Usually it is a user
	 * defined asset type so this should return true. For program defined
	 * asset types this should return false.
	 *
	 * @return false if this asset type is program defined, true otherwise
	 */
	public boolean isUserType() {
		return true;
	}

	public Codec getDefaultCodec() {
		return defaultCodec;
	}

	/**
	 * Add and set the default codec.
	 *
	 * @param codec
	 */
	public void setDefaultCodec( Codec codec ) {
		addCodec( this.defaultCodec = codec );
	}

	/**
	 * Get the set of codecs for this asset type.
	 *
	 * @return The set of codecs for this asset type
	 */
	public Set<Codec> getCodecs() {
		return Collections.unmodifiableSet( codecs );
	}

	public void addCodec( Codec codec ) {
		if( codec == null ) return;
		synchronized( codec ) {
			codecs.add( codec );
			codec.setAssetType( this );
		}
	}

	public void removeCodec( Codec codec ) {
		if( codec == null ) return;
		synchronized( codec ) {
			codecs.remove( codec );
			codec.setAssetType( null );
			if( getDefaultCodec() == codec ) setDefaultCodec( null );
		}
	}

	/**
	 * Initialize an asset with default state. This method should provide the
	 * specified asset with a default state prior to being used in an editor.
	 * <p>
	 * Unlike the {@link #assetUser(Program, Asset)} method this method is
	 * always called whenever an asset is new, opened or restored. This method
	 * should not be used for user interaction. User interaction should be
	 * implemented in the {@link #assetUser(Program, Asset)} method.
	 * <p>
	 * Note: This method is called using a task thread and is not safe to use
	 * directly on UI components. <br>
	 *
	 * @param program
	 * @param asset
	 * @return True if the asset was initialized, false otherwise. A value of false will keep the asset from being opened and an editor from being created.
	 * @throws AssetException if the asset failed to be initialized.
	 */
	public boolean assetInit( Program program, Asset asset ) throws AssetException {
		return true;
	}

	/**
	 * This method is called just before a new asset is opened to allow for
	 * user interaction. This method is valuable if the asset requires user
	 * interaction when creating new assets.
	 * <p>
	 * Unlike the {@link #assetInit(Program, Asset)} method this method is
	 * only called for new assets when the URI is null. If the URI is not null
	 * this method will not be called as is the case for opening or restoring
	 * existing assets.
	 * <p>
	 * Note: This method is called using a task thread and is not safe to use
	 * directly on UI components.
	 *
	 * @param program
	 * @param asset
	 * @return True if the asset was opened, false otherwise. A value of false will keep the asset from being opened and an editor from being created.
	 * @throws AssetException if the asset failed to be opened.
	 */
	public boolean assetUser( Program program, Asset asset ) throws AssetException {
		return true;
	}

	@Override
	public String toString() {
		return getName();
	}

	@Override
	public int compareTo( AssetType type ) {
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
