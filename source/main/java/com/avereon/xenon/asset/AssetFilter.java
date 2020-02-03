package com.avereon.xenon.asset;

import com.avereon.data.DataFilter;

/**
 * <p>
 * The {@link AssetFilter} is an interface for filtering
 * {@link Asset}) objects.
 * <p>
 * {@link AssetFilter} objects may be used in conjunction with many
 * asset classes to filter assets for display or any other purpose. A
 * typical use is with the <code>AssetTool</code> to filter the assets
 * shown.
 */
public interface AssetFilter extends DataFilter<Asset> {

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
	public String getDescription();

	/**
	 * Test the specified asset.
	 *
	 * @param asset The asset to test.
	 * @return True if the asset should be included, false otherwise.
	 */
	@Override
	public boolean accept( Asset asset );

}
