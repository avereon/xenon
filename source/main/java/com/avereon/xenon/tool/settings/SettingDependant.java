package com.avereon.xenon.tool.settings;

import com.avereon.data.Node;
import com.avereon.settings.Settings;
import lombok.CustomLog;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@CustomLog
public abstract class SettingDependant extends Node {

	public static final String DISABLE = "disable";

	public static final String VISIBLE = "visible";

	public static final String COLLAPSIBLE = "collapsible";

	public static final String EXPANDED = "expanded";

	private static final String DEPENDENCIES = "dependencies";

	private static final String FAIL_DEPENDENCY_ACTION = "fail-dependency-action";

	private static final String DEPEDENCY_ACTION_DISABLE = "disable";

	private static final String DEPEDENCY_ACTION_HIDE = "hide";

	protected SettingDependant() {
		setValue( DEPENDENCIES, new CopyOnWriteArrayList<SettingDependency>() );
		addModifyingKeys( DISABLE, VISIBLE );
	}

	public boolean isDisable() {
		return getValue( DISABLE, false );
	}

	public void setDisable( Boolean enabled ) {
		setValue( DISABLE, enabled );
	}

	public boolean isVisible() {
		return getValue( VISIBLE, false );
	}

	public void setVisible( Boolean visible ) {
		setValue( VISIBLE, visible );
	}

	public boolean isCollapsible() {
		return getValue( COLLAPSIBLE, false );
	}

	public void setCollapsible( Boolean collapsible ) {
		setValue( COLLAPSIBLE, collapsible );
	}

	public boolean isExpanded() {
		return getValue( EXPANDED, true );
	}

	public void setExpanded( Boolean expanded ) {
		setValue( EXPANDED, expanded );
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
		return getValue( FAIL_DEPENDENCY_ACTION, DEPEDENCY_ACTION_DISABLE );
	}

	public void setFailDependencyAction( String failDependencyAction ) {
		setValue( FAIL_DEPENDENCY_ACTION, failDependencyAction );
	}

	public void updateState() {
		if( SettingDependency.evaluate( getDependencies(), getSettings() ) ) {
			setDisable( false );
			setVisible( true );
		} else {
			setDisable( true );
			setVisible( !DEPEDENCY_ACTION_HIDE.equals( getFailDependencyAction() ) );
		}
	}

}
