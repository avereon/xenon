package com.parallelsymmetry.essence.product;

import com.parallelsymmetry.essence.util.Utf8Control;

import java.util.Locale;
import java.util.ResourceBundle;

import static java.text.MessageFormat.format;
import static java.util.ResourceBundle.getBundle;

public class ProductBundle {

	private ClassLoader loader;

	public ProductBundle( ClassLoader loader ) throws Exception {
		this.loader = loader;
	}

	public String getString( String bundleKey, String valueKey, String... values ) {
		// TODO Might be able to optimize ProductBundle.getString()
		ResourceBundle bundle = getBundle( "bundles/" + bundleKey, Locale.getDefault(), loader, new Utf8Control() );
		if( !bundle.containsKey( valueKey ) ) return null;
		String string = bundle.getString( valueKey );
		return format( string, values );
	}

}
