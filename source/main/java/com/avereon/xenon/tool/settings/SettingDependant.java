package com.avereon.xenon.tool.settings;

import com.avereon.data.Node;
import com.avereon.settings.Settings;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public abstract class SettingDependant extends Node {

	public static final String DISABLE = "disable";

	public static final String VISIBLE = "visible";

	private static final String DEPENDENCIES = "dependencies";

	private static final String FAIL_DEPENDENCY_ACTION = "fail-dependency-action";

	protected SettingDependant() {
		setValue( DEPENDENCIES, new CopyOnWriteArrayList<SettingDependency>() );
		addModifyingKeys( DISABLE, VISIBLE );
	}

	public boolean isDisable() {
		return getValue( DISABLE, false );
	}

	public void setDisable( boolean enabled ) {
		setValue( DISABLE, enabled );
	}

	public boolean isVisible() {
		return getValue( VISIBLE, false );
	}

	public void setVisible( boolean visible ) {
		setValue( VISIBLE, visible );
	}

	public List<SettingDependency> getDependencies() {
		return Collections.unmodifiableList( getValue( DEPENDENCIES ) );
	}

	public void addDependency( SettingDependency dependency ) {
		List<SettingDependency> dependencies = getValue( DEPENDENCIES );
		dependencies.add( dependency );
	}

	public abstract Settings getSettings();

	public String getFailDependencyAction() {
		return getValue( FAIL_DEPENDENCY_ACTION, "disable" );
	}

	public void setFailDependencyAction( String failDependencyAction ) {
		setValue( FAIL_DEPENDENCY_ACTION, failDependencyAction );
	}

	public void updateState() {
		boolean dependenciesValid = SettingDependency.evaluate( getDependencies(), getSettings() );

		if( !dependenciesValid ) {
			switch( getFailDependencyAction() ) {
				case "hide" -> {
					setDisable( true );
					setVisible( false );
				}
				default -> {
					setDisable( true );
					setVisible( true );
				}
			}
		} else {
			setDisable( false );
			setVisible( true );
		}
	}

}
