package com.xeomar.xenon.util;

import com.xeomar.xenon.settings.Settings;

public interface Configurable {

	void loadSettings( Settings settings );

	void saveSettings( Settings settings );

}
