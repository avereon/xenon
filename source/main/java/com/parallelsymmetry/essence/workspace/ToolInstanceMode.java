package com.parallelsymmetry.essence.workspace;

/**
 * The various modes that tools instances can be handled.
 */
public enum ToolInstanceMode {

	/**
	 * Allow only one instance of the tool per workarea.
	 */
	SINGLETON,
	/**
	 * An unlimited number of tools can be created and added to any workarea.
	 */
	UNLIMITED

}
