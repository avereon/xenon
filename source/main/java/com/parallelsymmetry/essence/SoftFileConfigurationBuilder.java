package com.parallelsymmetry.essence;

import org.apache.commons.configuration2.FileBasedConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.ex.ConfigurationException;

import java.util.Map;

public class SoftFileConfigurationBuilder<T extends FileBasedConfiguration> extends FileBasedConfigurationBuilder<T> {

	public SoftFileConfigurationBuilder( Class<? extends T> resCls ) {
		super( resCls );
	}

	public SoftFileConfigurationBuilder( Class<? extends T> resCls, Map params ) {
		super( resCls, params );
	}

	public SoftFileConfigurationBuilder( Class<? extends T> resCls, Map params, boolean allowFailOnInit ) {
		super( resCls, params, allowFailOnInit );
	}

	@Override
	public void save() throws ConfigurationException {
		// TODO Change this to save on a timer
		super.save();
	}

}
