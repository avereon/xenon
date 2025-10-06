package com.avereon.xenon.asset;

import com.avereon.data.DataFilter;

import java.util.function.Predicate;

/**
 * <p>
 * The {@link ResourceFilter} is an interface for filtering
 * {@link Asset}) objects.
 * <p>
 * {@link ResourceFilter} objects may be used in conjunction with many
 * asset classes to filter assets for display or any other purpose. A
 * typical use is with the <code>AssetTool</code> to filter the assets
 * shown.
 */
public interface ResourceFilter extends DataFilter<Asset>, Predicate<Asset>, Comparable<ResourceFilter> {

	/**
	 * <p>
	 * Get a short description for the filter. Examples:
	 * </p>
	 * <ul>
	 * <li>Directories Only</li>
	 * <li>Java Source Files (*.java)</li>
	 * <li>Web Files (*.html *.htm *.xml)</li>
	 * </ul>
	 *
	 * @return A short description for the filter.
	 */
	String getDescription();

	/**
	 * Test the specified asset.
	 *
	 * @param asset The asset to test.
	 * @return True if the asset should be included, false otherwise.
	 */
	@Override
	boolean accept( Asset asset );

	@Override
	default boolean test( Asset asset ) {
		return accept( asset );
	}

	@Override
	default int compareTo( ResourceFilter that ) {
		return this.getDescription().compareTo( that.getDescription() );
	}
}
