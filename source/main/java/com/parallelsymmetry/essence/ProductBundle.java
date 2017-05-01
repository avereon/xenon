package com.parallelsymmetry.essence;

import com.parallelsymmetry.essence.util.Utf8Control;

import java.util.Locale;
import java.util.ResourceBundle;

public class ProductBundle {

	private ResourceBundle bundle;

	public ProductBundle( ClassLoader loader, String locale ) throws Exception {
		Locale localeObject = Locale.getDefault();
		if( locale != null ) localeObject = Locale.forLanguageTag( locale );
		bundle = ResourceBundle.getBundle( "bundles/actions", localeObject, loader, new Utf8Control() );
	}

	public String getActionString( String key ) {
		return bundle.containsKey( key ) ? bundle.getString( key ) : null;
	}

}
