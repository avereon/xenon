package com.avereon.xenon.asset;

import com.avereon.xenon.asset.exception.ResourceException;

import java.util.List;

/**
 * The Scheme class represents a URI scheme in the context of a {@link Resource} . See <a href="http://en.wikipedia.org/wiki/URI_scheme">URI_scheme on
 * Wikipedia</a> for more information regarding URI schemes. Scheme, {@link ResourceType}
 * and {@link Codec} are used together by the asset manager to manage assets.
 * <p>
 * The scheme is responsible for defining and implementing how an asset is handled for connection and transport purposes. The scheme is solely responsible for
 * the connection and data transfer of the asset, not for interpreting the
 * content of the asset.
 *
 * @author SoderquistMV
 */
public interface Scheme {

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
	 * @throws ResourceException If an error occurs
	 */
	default List<Resource> getRoots() throws ResourceException {
		return List.of();
	}

	/**
	 * Determines whether the specified asset can be loaded.
	 *
	 * @param resource The asset to check
	 * @return If the asset can be loaded
	 * @throws ResourceException If an error occurs
	 */
	default boolean canLoad( Resource resource ) throws ResourceException {
		return false;
	}

	/**
	 * Determines whether the specified asset can be saved.
	 *
	 * @param resource The asset to check
	 * @return If the asset can be saved
	 * @throws ResourceException If an error occurs
	 */
	default boolean canSave( Resource resource ) throws ResourceException {
		return false;
	}

	/**
	 * Initialize the {@link Resource}. This is called from a {@link Resource} when the {@link Scheme} is set.
	 *
	 * @param resource The asset to init.
	 * @throws ResourceException If an error occurs
	 */
	default void init( Resource resource ) throws ResourceException {}

	/**
	 * Open the {@link Resource}.
	 *
	 * @param resource The asset to open
	 * @throws ResourceException If an error occurs
	 */
	default void open( Resource resource ) throws ResourceException {}

	/**
	 * Load the {@link Resource}.
	 *
	 * @param resource The asset to load
	 * @param codec The codec to use to load the asset
	 * @throws ResourceException If an error occurs
	 */
	default void load( Resource resource, Codec codec ) throws ResourceException {}

	/**
	 * Save the {@link Resource}.
	 *
	 * @param resource The asset to save
	 * @param codec The codec to use to save the asset
	 * @throws ResourceException If an error occurs
	 */
	default void save( Resource resource, Codec codec ) throws ResourceException {}

	/**
	 * Close the {@link Resource}.
	 *
	 * @param resource The asset to close
	 * @throws ResourceException If an error occurs
	 */
	default void close( Resource resource ) throws ResourceException {}

	/**
	 * Determine if the asset exists. If the correct value cannot be determined an exception is thrown.
	 *
	 * @param resource The asset to verify exists
	 * @return true If the asset exists, false otherwise
	 * @throws ResourceException If the correct value cannot be determined
	 */
	default boolean exists( Resource resource ) throws ResourceException {
		return false;
	}

	/**
	 * Create the external asset that the asset represents.
	 *
	 * @param resource The asset to create
	 * @return true If the external source is created, false otherwise
	 * @throws ResourceException If an error occurs during the operation
	 */
	default boolean create( Resource resource ) throws ResourceException {
		return false;
	}

	/**
	 * Create the external folder asset that the asset represents.
	 *
	 * @param resource The asset folder to create
	 * @return true If the external source is created, false otherwise
	 * @throws ResourceException If an error occurs during the operation
	 */
	default boolean createFolder( Resource resource ) throws ResourceException {
		return false;
	}

	/**
	 * Save the asset as a different asset.
	 *
	 * @param resource The asset to save
	 * @param target The destination asset
	 * @throws ResourceException If the asset can not be saved
	 */
	default void saveAs( Resource resource, Resource target ) throws ResourceException {}

	/**
	 * Rename the asset as a different asset.
	 *
	 * @param resource The asset to rename
	 * @param target The destination asset
	 * @throws ResourceException If the asset can not be renamed
	 */
	default boolean rename( Resource resource, Resource target ) throws ResourceException {
		return false;
	}

	/**
	 * Delete the asset.
	 *
	 * @param resource The asset to delete
	 * @return true If and only if the asset is successfully deleted, false otherwise
	 * @throws ResourceException If an error occurred during deletion
	 */
	default boolean delete( Resource resource ) throws ResourceException {
		return false;
	}

	/**
	 * Determine if the asset a folder for other assets.
	 */
	default boolean isFolder( Resource resource ) throws ResourceException {
		return false;
	}

	/**
	 * Determine if the asset is hidden.
	 */
	default boolean isHidden( Resource resource ) throws ResourceException {
		return false;
	}

	/**
	 * Get the child assets if this asset is a folder.
	 */
	default List<Resource> listAssets( Resource resource ) throws ResourceException {
		return List.of();
	}

	/**
	 * Get the size of the asset in bytes.
	 *
	 * @param resource The asset from which to get the size
	 * @return The size of the asset in bytes
	 * @throws ResourceException If the size can not be determined
	 */
	default long getSize( Resource resource ) throws ResourceException {
		return -1;
	}

	/**
	 * Get the modified date.
	 *
	 * @return The last date the asset was modified
	 * @throws ResourceException If the date can not be determined
	 */
	default long getModifiedDate( Resource resource ) throws ResourceException {
		return -1;
	}

	/**
	 * Get the media type for an asset
	 */
	default String getMediaType( Resource resource ) {
		return StandardMediaTypes.DEFAULT;
	}

	/**
	 * Get the first line of content for an asset
	 */
	default String getFirstLine( Resource resource ) {
		return "";
	}

	/**
	 * Check if an asset supported by this scheme.
	 *
	 * @param resource The asset to check
	 * @return True if supported, false otherwise
	 */
	default boolean isSupported( Resource resource ) {
		if( resource == null ) return false;
		return getName().equals( resource.getUri().getScheme() );
	}

	default boolean isPersistentConnection() {
		return false;
	}

}
