package com.xeomar.xenon.tool.settings;

import com.xeomar.xenon.node.Node;
import com.xeomar.xenon.settings.Settings;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Setting extends Node {

	public static final String KEY = "key";

	public static final String VALUE = "value";

	public static final String PRESENTATION = "presentation";

	private static final String ENABLED = "enabled";

	private static final String VISIBLE = "visible";

	private static final String OPAQUE = "opaque";

	private static final String OPTIONS = "options";

	private static final String DEPENDENCIES = "dependencies";

	private Settings settings;

	public Setting( Settings settings ) {
		this.settings = settings;
		setValue( OPTIONS, new CopyOnWriteArrayList<SettingOption>() );
		setValue( DEPENDENCIES, new CopyOnWriteArrayList<SettingDependency>() );
		setModified( false );
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

	public String getPresentation() {
		return getValue( PRESENTATION );
	}

	public void setPresentation( String presentation ) {
		setValue( PRESENTATION, presentation );
	}

	public boolean isEnabled() {
		return getValue( ENABLED );
	}

	public void setEnabled( boolean enabled ) {
		setValue( ENABLED, enabled );
	}

	public void updateEnabled() {
		setEnabled( canEnable() );
	}

	public boolean isVisible() {
		return getValue( VISIBLE );
	}

	public void setVisible( boolean visible ) {
		setValue( VISIBLE, visible );
	}

	public boolean isOpaque() {
		return getValue( OPAQUE );
	}

	public void setOpaque( boolean opaque ) {
		setValue( OPAQUE, opaque );
	}

	public SettingOption getOption( String value ) {
		for( SettingOption option : getOptions() ) {
			if( option.getOptionValue().equals( value ) ) {
				return option;
			}
		}

		return null;
	}

	public List<SettingOption> getOptions() {
		return Collections.unmodifiableList( getValue( OPTIONS ) );
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

		String presentation = getValue( PRESENTATION );
		if( presentation == null ) presentation = "text";
		builder.append( presentation );

		return builder.toString();
	}

	private boolean canEnable() {
		return !getValue( "disabled", false ) && SettingDependency.evaluate( getDependencies(), settings );
	}

	private static String getBundleKey( String key ) {
		if( key == null ) return null;
		if( key.startsWith( "/" ) ) key = key.substring( 1 );
		return key.replace( '/', '-' );
	}

}
