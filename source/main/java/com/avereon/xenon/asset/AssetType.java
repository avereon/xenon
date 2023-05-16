package com.avereon.xenon.asset;

import com.avereon.product.Product;
import com.avereon.product.Rb;
import com.avereon.xenon.Xenon;
import com.avereon.xenon.ProgramProduct;
import com.avereon.xenon.tool.settings.SettingsPage;
import com.avereon.zarra.javafx.Fx;
import lombok.CustomLog;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

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
@CustomLog
public abstract class AssetType implements Comparable<AssetType> {

	protected static final String BASE_MEDIA_TYPE = "application/vnd.avereon.xenon.program";

	private final String key = getClass().getName();

	private final ProgramProduct product;

	private final String rbKey;

	private final Set<Codec> codecs;

	private Codec defaultCodec;

	private Map<String,SettingsPage> settingsPages;

	public AssetType( ProgramProduct product, String rbKey ) {
		if( product == null ) throw new NullPointerException( "Product cannot be null" );
		if( rbKey == null ) throw new NullPointerException( "Resource bundle key cannot be null" );
		this.product = product;
		this.rbKey = rbKey;
		this.codecs = new CopyOnWriteArraySet<>();
	}

	public Xenon getProgram() {
		return product.getProgram();
	}

	public Product getProduct() {
		return product;
	}

	public String getKey() {
		return key;
	}

	public String getName() {
		return Rb.text( getProduct(), "asset", rbKey + "-name" );
	}

	public String getDescription() {
		return Rb.text( getProduct(), "asset", rbKey + "-description" );
	}

	public String getIcon() {
		return Rb.textOr( getProduct(), "asset", rbKey + "-icon", "asset" );
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

	public Set<Codec.Association> getAssociations() {
		return getCodecs().stream().flatMap( c -> c.getAssociations().stream() ).collect( Collectors.toSet());
	}

	/**
	 * This method is called when a new asset is requested to be opened. This
	 * method is valuable if the asset requires user interaction when creating new
	 * assets.
	 * <p>
	 * Unlike the {@link #assetOpen(Xenon, Asset)} method this method is
	 * only called for new assets. If the asset is not new this method will not
	 * be called unlike the process for opening or restoring existing assets.
	 * <p>
	 * Note: This method is called using a task thread and is not safe to use
	 * directly on UI components.
	 *
	 * @param program
	 * @param asset
	 * @return True if the asset was opened, false otherwise. A value of false will keep the asset from being opened and an editor from being created.
	 * @throws AssetException if the asset failed to be opened.
	 */
	public boolean assetNew( Xenon program, Asset asset ) throws AssetException {
		return true;
	}

	boolean callAssetNew( Xenon program, Asset asset ) throws AssetException {
		Object lock = new Object();
		AtomicBoolean result = new AtomicBoolean();
		AtomicReference<AssetException> resultException = new AtomicReference<>();

		Fx.run( () -> {
			synchronized( lock ) {
				try {
					log.atTrace().log( "Calling assetNew()..." );
					result.set( assetNew( program, asset ) );
				} catch( AssetException exception ) {
					resultException.set( exception );
				} finally {
					lock.notifyAll();
				}
			}
		} );

		synchronized( lock ) {
			try {
				lock.wait( 60000 );
			} catch( InterruptedException exception ) {
				exception.printStackTrace();
			}
		}

		log.atDebug().log( "Done waiting for assetNew()." );

		if( resultException.get() != null ) throw resultException.get();
		return result.get();
	}

	/**
	 * This method is called as an asset is opened just before it is loaded. This
	 * method can provide the specified asset with an initial state prior to being
	 * loaded or used in a tool.
	 * <p>
	 * Unlike the {@link #assetNew(Xenon, Asset)} method this method is
	 * always called whenever an asset is opened, new or otherwise. This method
	 * should not be used for user interaction. User interaction should be
	 * implemented in the {@link #assetNew(Xenon, Asset)} method.
	 * <p>
	 * Note: This method is called using a task thread and is not safe to use
	 * directly on UI components. <br>
	 *
	 * @param program
	 * @param asset
	 * @return True if the asset was initialized, false otherwise. A value of false will keep the asset from being opened and a tool from being created.
	 * @throws AssetException if the asset failed to be initialized.
	 */
	public boolean assetOpen( Xenon program, Asset asset ) throws AssetException {
		return true;
	}

	boolean callAssetOpen( Xenon program, Asset asset ) throws AssetException {
		return assetOpen( program, asset );
	}

	@Override
	public String toString() {
		return getName();
	}

	@Override
	public int compareTo( AssetType type ) {
		return getName().compareTo( type.getName() );
	}

	public Set<Codec> getSupportedCodecs( Codec.Pattern type, String value ) {
		return codecs.stream().filter( c -> c.isSupported( type, value ) ).collect( Collectors.toSet() );
	}

	public Map<String, SettingsPage> getSettingsPages() {
		return settingsPages;
	}

	public void setSettingsPages(Map<String,SettingsPage> pages) {
		this.settingsPages = pages;
	}

}
