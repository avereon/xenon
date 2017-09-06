package com.xeomar.xenon.tool.settings;

import com.xeomar.xenon.node.Node;
import com.xeomar.xenon.settings.Settings;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class SettingGroup extends Node {

	private static final String ID = "id";

	private static final String ENABLED = "enabled";

	private static final String VISIBLE = "visible";

	private static final String SETTINGS = "settings";

	private static final String DEPENDENCIES = "dependencies";

	private Settings settings;

	public SettingGroup( Settings settings ) {
		this.settings = settings;
		definePrimaryKey( ID );
		setValue( SETTINGS, new CopyOnWriteArrayList<Setting>() );
		setValue( DEPENDENCIES, new CopyOnWriteArrayList<SettingDependency>() );
		setEnabled( true );
		updateFlags();
		setModified( false );
	}

	public String getId() {
		return getValue( ID );
	}

	public void setId( String id ) {
		setValue( ID, id );
	}

	public boolean isEnabled() {
		return getValue( ENABLED, false );
	}

	public void setEnabled( boolean enabled ) {
		setValue( ENABLED, enabled );
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

	public void updateFlags() {
		setVisible( SettingDependency.evaluate( getDependencies(), settings ) );
	}

}
