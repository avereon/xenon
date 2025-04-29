package com.avereon.xenon.tool.settings;

import com.avereon.data.Node;
import com.avereon.settings.Settings;
import lombok.CustomLog;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@CustomLog
public abstract class SettingDependant extends Node {

	public static final String ID = "id";

	public static final String PATH = "path";

	public static final String DISABLE = "disable";

	public static final String VISIBLE = "visible";

	public static final String COLLAPSIBLE = "collapsible";

	public static final String EXPANDED = "expanded";

	private static final String DEPENDENCIES = "dependencies";

	private static final String FAIL_DEPENDENCY_ACTION = "fail-dependency-action";

	private static final String DEPENDENCY_ACTION_DISABLE = "disable";

	private static final String DEPENDENCY_ACTION_HIDE = "hide";

	protected SettingDependant() {
		setValue( DEPENDENCIES, new CopyOnWriteArrayList<SettingDependency>() );
		addModifyingKeys( DISABLE, VISIBLE );
	}

	public String getId() {
		return getValue( ID );
	}

	public void setId( String id ) {
		setValue( ID, id );
	}

	public String getPath() {
		return getValue( PATH );
	}

	public void setPath( String path ) {
		setValue( PATH, path );
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

	abstract Settings getParentSettings();

	public Settings getSettings() {
		String path = getPath();
		Settings settings = getParentSettings();

		// If the path is set, get the settings from the path
		if( path != null ) settings = settings.getNode( path );

		return settings;
	}

	public String getFailDependencyAction() {
		return getValue( FAIL_DEPENDENCY_ACTION, DEPENDENCY_ACTION_DISABLE );
	}

	public void setFailDependencyAction( String failDependencyAction ) {
		setValue( FAIL_DEPENDENCY_ACTION, failDependencyAction );
	}

	public void updateState() {
		boolean success = SettingDependency.evaluate( getDependencies(), getSettings() );
		if( success ) {
			setDisable( false );
			setVisible( true );
		} else {
			setDisable( true );
			setVisible( !DEPENDENCY_ACTION_HIDE.equals( getFailDependencyAction() ) );
		}
	}

}
