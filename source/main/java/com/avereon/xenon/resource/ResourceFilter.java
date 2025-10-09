package com.avereon.xenon.resource;

import com.avereon.data.DataFilter;

import java.util.function.Predicate;

/**
 * <p>
 * The {@link ResourceFilter} is an interface for filtering
 * {@link Resource}) objects.
 * <p>
 * {@link ResourceFilter} objects may be used in conjunction with many
 * asset classes to filter assets for display or any other purpose. A
 * typical use is with the <code>AssetTool</code> to filter the assets
 * shown.
 */
public interface ResourceFilter extends DataFilter<Resource>, Predicate<Resource>, Comparable<ResourceFilter> {

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
	 * @param resource The asset to test.
	 * @return True if the asset should be included, false otherwise.
	 */
	@Override
	boolean accept( Resource resource );

	@Override
	default boolean test( Resource resource ) {
		return accept( resource );
	}

	@Override
	default int compareTo( ResourceFilter that ) {
		return this.getDescription().compareTo( that.getDescription() );
	}
}
