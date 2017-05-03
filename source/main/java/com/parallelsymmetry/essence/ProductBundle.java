package com.parallelsymmetry.essence;

import com.parallelsymmetry.essence.util.Utf8Control;

import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;

import static java.text.MessageFormat.format;
import static java.util.ResourceBundle.getBundle;

public class ProductBundle {

	public enum Key {
		ACTION
	}

	private Locale locale;

	private ClassLoader loader;

	private Map<Key, ResourceBundle> bundles;

	public ProductBundle( ClassLoader loader, String localeTag ) throws Exception {
		this.loader = loader;
		this.locale = localeTag == null ? Locale.getDefault() : Locale.forLanguageTag( localeTag );

		bundles = new ConcurrentHashMap<>();
		bundles.put( Key.ACTION, getBundle( "bundles/actions", locale, loader, new Utf8Control() ) );
	}

	public String getActionString( String key, String... values ) {
		return getString( Key.ACTION, key, values );
	}

	public String getString( String bundleKey, String valueKey, String... values ) {
		// TODO Might be able to optimize this
		ResourceBundle bundle = getBundle( "bundles/" + bundleKey, locale, loader, new Utf8Control() );
		if( !bundle.containsKey( valueKey ) ) return null;
		String string = bundle.getString( valueKey );
		return format( string, values );
	}

	private String getString( Key bundleKey, String valueKey, String... values ) {
		ResourceBundle bundle = bundles.get( bundleKey );
		if( !bundle.containsKey( valueKey ) ) return null;
		String string = bundle.getString( valueKey );
		return format( string, values );
	}

}
