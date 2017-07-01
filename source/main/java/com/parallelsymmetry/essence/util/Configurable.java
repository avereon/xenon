package com.parallelsymmetry.essence.util;

import com.parallelsymmetry.essence.settings.Settings;

public interface Configurable {

	void loadSettings( Settings settings );

	void saveSettings( Settings settings );

}
