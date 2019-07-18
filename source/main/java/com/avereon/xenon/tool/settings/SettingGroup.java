package com.avereon.xenon.tool.settings;

import com.avereon.settings.Settings;
import com.avereon.xenon.node.Node;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class SettingGroup extends Node {

	private static final String ID = "id";

	private static final String DISABLE = "disable";

	private static final String VISIBLE = "visible";

	private static final String SETTINGS = "settings";

	private static final String DEPENDENCIES = "dependencies";

	private Settings settings;

	public SettingGroup( Settings settings ) {
		this.settings = settings;
		definePrimaryKey( ID );
		setValue( SETTINGS, new CopyOnWriteArrayList<Setting>() );
		setValue( DEPENDENCIES, new CopyOnWriteArrayList<SettingDependency>() );
	}

	public String getId() {
		return getValue( ID );
	}

	public void setId( String id ) {
		setValue( ID, id );
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

	public List<Setting> getSettings() {
		return Collections.unmodifiableList( getValue( SETTINGS ) );
	}

	public void addSetting( Setting setting ) {
		List<Setting> settings = getValue( SETTINGS );
		settings.add( setting );
	}

	public List<SettingDependency> getDependencies() {
		return Collections.unmodifiableList( getValue( DEPENDENCIES ) );
	}

	public void addDependency( SettingDependency dependency ) {
		List<SettingDependency> dependencies = getValue( DEPENDENCIES );
		dependencies.add( dependency );
	}

	public void updateState() {
		setVisible( SettingDependency.evaluate( getDependencies(), settings ) );
	}

}
