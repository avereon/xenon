package com.xeomar.xenon.tool;

import com.xeomar.xenon.node.Node;
import com.xeomar.xenon.settings.Settings;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Setting extends Node {

	public static final String KEY = "key";

	public static final String SETTING_VALUE = "value";

	public static final String PRESENTATION = "presentation";

	private static final String ENABLED = "enabled";

	private static final String VISIBLE = "visible";

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

	public String getSettingValue() {
		return getValue( SETTING_VALUE );
	}

	public String getPresentation() {
		return getValue( PRESENTATION );
	}

	public void setPresentation( String presentation ) {
		setValue( PRESENTATION, presentation );
	}

	public boolean isEnabled() {
		return getFlag( ENABLED );
	}

	public void setEnabled( boolean enabled ) {
		setFlag( ENABLED, enabled );
	}

	public void updateEnabledFlag() {
		setEnabled( canEnable() );
	}

	public boolean isVisible() {
		return getFlag( VISIBLE );
	}

	public void setVisible( boolean visible ) {
		setFlag( VISIBLE, visible );
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

	public boolean evaluateDependencies( Settings settings ) {
		boolean pass = true;

		for( SettingDependency dependency : getDependencies() ) {
			pass = dependency.evaluate( settings, pass );
		}

		return pass;
	}

	public SettingOption getOption( String value ) {
		for( SettingOption option : getOptions() ) {
			if( option.getOptionValue().equals( value ) ) {
				return option;
			}
		}

		return null;
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
		return !getFlag( "disabled" ) && evaluateDependencies(settings);
	}

}
