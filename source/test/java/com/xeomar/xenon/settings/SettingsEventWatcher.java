package com.xeomar.xenon.settings;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

class SettingsEventWatcher implements SettingsListener {

	private List<SettingsEvent> events = new CopyOnWriteArrayList<>();

	@Override
	public void settingsEvent( SettingsEvent event ) {
		events.add( event );
	}

	public List<SettingsEvent> getEvents() {
		return Collections.unmodifiableList( events );
	}

	public void reset() {
		events.clear();
	}

}
