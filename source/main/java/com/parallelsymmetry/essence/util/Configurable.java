package com.parallelsymmetry.essence.util;

import org.apache.commons.configuration2.Configuration;

public interface Configurable {

	void loadSettings( Configuration configuration );

	void saveSettings( Configuration configuration );

}
