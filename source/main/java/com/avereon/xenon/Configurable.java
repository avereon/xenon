package com.avereon.xenon;

import com.avereon.settings.Settings;

/**
 * And interface for classes that want to use the Settings API to store
 * settings.
 * <p/>
 * This class has been deprecated to encourage loose coupling with the Settings
 * class using listeners for changes instead of tightly coupling with the
 * Settings API, particularly UI components.
 *
 * @deprecated The use of this interface is deprecated in favor of decoupling
 * classes that want to store settings with a listener instead of being tightly
 * coupled with the Settings class.
 */
@Deprecated
public interface Configurable {

	@Deprecated
	void setSettings( Settings settings );

	@Deprecated
	Settings getSettings();

}
