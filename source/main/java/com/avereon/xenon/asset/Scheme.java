package com.avereon.xenon.asset;

import java.net.URLConnection;
import java.util.List;

/**
 * The Scheme class represents a URI scheme in the context of a {@link Asset} . See <a href="http://en.wikipedia.org/wiki/URI_scheme">URI_scheme on Wikipedia</a> for more information regarding URI schemes. Scheme, {@link AssetType}
 * and {@link Codec} are used together by the asset manager to manage assets.
 * <p>
 * The scheme is responsible for defining and implementing how an asset is handled for connection and transport purposes. The scheme is solely responsible for the connection and data transfer of the asset, not for interpreting the
 * content of the asset.
 *
 * @author SoderquistMV
 */
public interface Scheme {

	String ASSET_LAST_SAVED_KEY = "asset.last.saved";

	/**
	 * Get the scheme name. The scheme name is equivalent to the URI scheme
	 * defined in <a href="http://tools.ietf.org/html/rfc3986">RFC-3986</a>.
	 *
	 * @return The scheme name
	 */
	String getName();

	/**
	 * Get the root assets associated with this scheme.
	 *
	 * @return The list of root assets for this scheme.
	 * @throws AssetException If an error occurs
	 */
	List<Asset> getRoots() throws AssetException;

	/**
	 * Determines whether the specified asset can be loaded.
	 *
	 * @param asset The asset to check
	 * @return If the asset can be loaded
	 * @throws AssetException If an error occurs
	 */
	boolean canLoad( Asset asset ) throws AssetException;

	/**
	 * Determines whether the specified asset can be saved.
	 *
	 * @param asset The asset to check
	 * @return If the asset can be saved
	 * @throws AssetException If an error occurs
	 */
	boolean canSave( Asset asset ) throws AssetException;

	/**
	 * Initialize the {@link Asset}. This is called from a {@link Asset} when the {@link Scheme} is set.
	 *
	 * @param asset The asset to init.
	 * @throws AssetException If an error occurs
	 */
	void init( Asset asset ) throws AssetException;

	/**
	 * Open the {@link Asset}.
	 *
	 * @param asset The asset to open
	 * @throws AssetException If an error occurs
	 */
	void open( Asset asset ) throws AssetException;

	/**
	 * Load the {@link Asset}.
	 *
	 * @param asset The asset to load
	 * @param codec The codec to use to load the asset
	 * @throws AssetException If an error occurs
	 */
	void load( Asset asset, Codec codec ) throws AssetException;

	/**
	 * Save the {@link Asset}.
	 *
	 * @param asset The asset to save
	 * @param codec The codec to use to save the asset
	 * @throws AssetException If an error occurs
	 */
	void save( Asset asset, Codec codec ) throws AssetException;

	/**
	 * Close the {@link Asset}.
	 *
	 * @param asset The asset to close
	 * @throws AssetException If an error occurs
	 */
	void close( Asset asset ) throws AssetException;

	/**
	 * Determine if the asset exists. If the correct value cannot be determined an exception is thrown.
	 *
	 * @param asset The asset to verify exists
	 * @return true If the asset exists, false otherwise
	 * @throws AssetException If the correct value cannot be determined
	 */
	boolean exists( Asset asset ) throws AssetException;

	/**
	 * Create the external asset that the asset represents.
	 *
	 * @param asset The asset to create
	 * @return true If the external source is created, false otherwise
	 * @throws AssetException If an error occurs during the operation
	 */
	boolean create( Asset asset ) throws AssetException;

	/**
	 * Save the asset as a different asset.
	 *
	 * @param asset The asset to save
	 * @param target The destination asset
	 * @throws AssetException If the asset can not be saved
	 */
	void saveAs( Asset asset, Asset target ) throws AssetException;

	/**
	 * Rename the asset as a different asset.
	 *
	 * @param asset The asset to rename
	 * @param target The destination asset
	 * @throws AssetException If the asset can not be renamed
	 */
	boolean rename( Asset asset, Asset target ) throws AssetException;

	/**
	 * Delete the asset.
	 *
	 * @param asset The asset to delete
	 * @return true If and only if the asset is successfully deleted, false otherwise
	 * @throws AssetException If an error occurred during deletion
	 */
	boolean delete( Asset asset ) throws AssetException;

	/**
	 * Determine if the asset a folder for other assets.
	 */
	boolean isFolder( Asset asset ) throws AssetException;

	/**
	 * Determine if the asset is hidden.
	 */
	boolean isHidden( Asset asset ) throws AssetException;

	/**
	 * Get the child assets if this asset is a folder.
	 */
	List<Asset> listAssets( Asset asset ) throws AssetException;

	/**
	 * Get the size of the asset in bytes.
	 *
	 * @param asset The asset from which to get the size
	 * @return The size of the asset in bytes
	 * @throws AssetException If the size can not be determined
	 */
	long getSize( Asset asset ) throws AssetException;

	/**
	 * Get the modified date.
	 *
	 * @return The last date the asset was modified
	 * @throws AssetException If the date can not be determined
	 */
	long getModifiedDate( Asset asset ) throws AssetException;

	/**
	 * Get a connection for an asset
	 */
	URLConnection getConnection( Asset asset );

}
