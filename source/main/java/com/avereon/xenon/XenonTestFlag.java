package com.avereon.xenon;

public interface XenonTestFlag {

	/**
	 * This flag indicates that the default workspace should be left empty. This
	 * is the normal operation of most UI tests to avoid unnecessary work at the
	 * beginning of the test. If a test does not want the default workspace left
	 * empty, it should remove this flag from the test configuration.
	 */
	String EMPTY_WORKSPACE = "--empty-workspace";

}
