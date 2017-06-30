package com.parallelsymmetry.essence.resource;

import com.parallelsymmetry.essence.node.DataFilter;

/**
 * <p>
 * The <code>ResourceFilter</code> is an interface for filtering
 * <code>Resource</code> objects.
 * <p>
 * <code>ResourceFilter</code> objects may be used in conjunction with many
 * resource classes to filter resources for display or any other purpose. A
 * typical use is with the <code>ResourceTool</code> to filter the resources
 * shown.
 */
public interface ResourceFilter extends DataFilter<Resource> {

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
	 * Test the specified resource.
	 *
	 * @param resource The resource to test.
	 * @return True if the resource should be included, false otherwise.
	 */
	@Override
	public boolean accept( Resource resource );

}
