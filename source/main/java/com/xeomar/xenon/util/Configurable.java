package com.xeomar.xenon.util;

import com.xeomar.xenon.settings.Settings;

public interface Configurable {

	void setSettings( Settings settings );

	Settings getSettings();

}
