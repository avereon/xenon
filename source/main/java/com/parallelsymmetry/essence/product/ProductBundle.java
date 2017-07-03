package com.parallelsymmetry.essence.product;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;
import java.util.ResourceBundle;

import static java.text.MessageFormat.format;
import static java.util.ResourceBundle.getBundle;

public class ProductBundle {

	private static final Logger log = LoggerFactory.getLogger( ProductBundle.class );

	private ClassLoader loader;

	public ProductBundle( ClassLoader loader ) throws Exception {
		this.loader = loader;
	}

	public String getString( String bundleKey, String valueKey, String... values ) {
		String string = null;

		ResourceBundle bundle = getBundle( "bundles/" + bundleKey, Locale.getDefault(), loader );
		if( bundle.containsKey( valueKey ) ) string = format( bundle.getString( valueKey ), (Object[])values );
		if( string == null ) log.trace( "Missing RB key: " + bundleKey + ":" + valueKey );

		return string;
	}

}
