package com.avereon.xenon.tool.settings;

import com.avereon.settings.Settings;
import lombok.CustomLog;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@CustomLog
public class SettingData extends SettingDependant {

	public static final String ID = "id";

	public static final String KEY = "key";

	public static final String RBKEY = "rbkey";

	public static final String VALUE = "value";

	public static final String EDITOR = "editor";

	private static final String OPAQUE = "opaque";

	private static final String OPTIONS = "options";

	private static final String OPTION_PROVIDER = "option-provider";

	private final SettingGroup group;

	private SettingOptionProvider optionProvider;

	public SettingData( SettingGroup group ) {
		this.group = group;
		setValue( OPTIONS, new CopyOnWriteArrayList<SettingOption>() );
		setModified( false );
	}

	public SettingGroup getGroup() {
		return group;
	}

	public String getId() {
		return getValue( ID );
	}

	public void setId( String id ) {
		setValue( ID, id );
	}

	public String getKey() {
		return getValue( KEY );
	}

	public void setKey( String key ) {
		setValue( KEY, key );
	}

	public String getSettingValue() {
		return getValue( VALUE );
	}

	public void setSettingValue( String value ) {
		setValue( VALUE, value );
	}

	public String getEditor() {
		return getValue( EDITOR );
	}

	public void setEditor( String editor ) {
		setValue( EDITOR, editor );
	}

	/**
	 * For color settings, if the color is required to be opaque.
	 */
	public boolean isOpaque() {
		return getValue( OPAQUE );
	}

	public void setOpaque( boolean opaque ) {
		setValue( OPAQUE, opaque );
	}

	public String getProvider() {
		return getValue( OPTION_PROVIDER );
	}

	public SettingData setProvider( String provider ) {
		setValue( OPTION_PROVIDER, provider );
		return this;
	}

	public SettingOptionProvider getOptionProvider() {
		return optionProvider;
	}

	public void setOptionProvider( SettingOptionProvider optionProvider ) {
		this.optionProvider = optionProvider;
	}

	public SettingOption getOption( String value ) {
		return getOptions().stream().filter( o -> Objects.equals( o.getOptionValue(), value ) ).findFirst().orElse( null );
	}

	public List<SettingOption> getOptions() {
		if( optionProvider == null ) {
			return Collections.unmodifiableList( getValue( OPTIONS ) );
		} else {
			return optionProvider
				.getKeys()
				.stream()
				.map( k -> new SettingOption().setKey( k ).setName( optionProvider.getName( k ) ).setOptionValue( optionProvider.getValue( k ) ) )
				.collect( Collectors.toList() );
		}
	}

	public void addOption( SettingOption option ) {
		List<SettingOption> options = getValue( OPTIONS );
		options.add( option );
	}

	public Settings getSettings() {
		return getGroup().getSettings();
	}

	public String getBundleKey() {
		return getBundleKey( getId(), getKey() );
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();

		String key = getValue( RBKEY );
		builder.append( key );
		builder.append( ":" );

		String editor = getValue( EDITOR );
		if( editor == null ) editor = "text";
		builder.append( editor );

		return builder.toString();
	}

	private static String getBundleKey( String id, String key ) {
		String localKey = key;
		if( localKey == null ) localKey = id;
		if( localKey == null ) return null;
		if( localKey.startsWith( "/" ) ) localKey = localKey.substring( 1 );
		return localKey.replace( '/', '-' );
	}

}
