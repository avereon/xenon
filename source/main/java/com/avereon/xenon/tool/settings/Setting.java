package com.avereon.xenon.tool.settings;

import com.avereon.data.Node;
import com.avereon.settings.Settings;
import com.avereon.util.Log;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

public class Setting extends Node {

	public static final String KEY = "key";

	public static final String VALUE = "value";

	public static final String EDITOR = "editor";

	public static final String DISABLE = "disable";

	public static final String VISIBLE = "visible";

	private static final String EDITABLE = "editable";

	private static final String OPAQUE = "opaque";

	private static final String OPTIONS = "options";

	private static final String OPTION_PROVIDER = "option-provider";

	private static final String DEPENDENCIES = "dependencies";

	private static final System.Logger log = Log.get();

	private final Settings settings;

	private SettingOptionProvider optionProvider;

	public Setting( Settings settings ) {
		this.settings = settings;
		setValue( OPTIONS, new CopyOnWriteArrayList<SettingOption>() );
		setValue( DEPENDENCIES, new CopyOnWriteArrayList<SettingDependency>() );
		setModified( false );
		addModifyingKeys( DISABLE, VISIBLE );
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

	public boolean isDisable() {
		return getValue( DISABLE, false );
	}

	public void setDisable( boolean disable ) {
		setValue( DISABLE, disable );
	}

	public boolean isVisible() {
		return getValue( VISIBLE, true );
	}

	public void setVisible( boolean visible ) {
		setValue( VISIBLE, visible );
	}

	//	public boolean isEditable() {
	//		return getValue( EDITABLE );
	//	}
	//
	//	public void setEditable( boolean editable ) {
	//		setValue( EDITABLE, editable );
	//	}

	public void updateState() {
		setDisable( !SettingDependency.evaluate( getDependencies(), settings ) );
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

	public Setting setProvider( String provider ) {
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

	public List<SettingDependency> getDependencies() {
		return Collections.unmodifiableList( getValue( DEPENDENCIES ) );
	}

	public void addDependency( SettingDependency dependency ) {
		List<SettingDependency> dependencies = getValue( DEPENDENCIES );
		dependencies.add( dependency );
	}

	public Settings getSettings() {
		return settings;
	}

	public String getBundleKey() {
		return getBundleKey( getKey() );
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();

		String key = getValue( KEY );
		builder.append( key );
		builder.append( ":" );

		String editor = getValue( EDITOR );
		if( editor == null ) editor = "text";
		builder.append( editor );

		return builder.toString();
	}

	private static String getBundleKey( String key ) {
		if( key == null ) return null;
		if( key.startsWith( "/" ) ) key = key.substring( 1 );
		return key.replace( '/', '-' );
	}

}
