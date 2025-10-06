package com.avereon.xenon.action;

import com.avereon.xenon.ProgramAction;
import com.avereon.xenon.Xenon;
import com.avereon.xenon.asset.type.ProgramSettingsType;
import javafx.event.ActionEvent;

import java.net.URI;

public class SettingsAction extends ProgramAction {

	private URI uri = ProgramSettingsType.URI;

	public SettingsAction( Xenon program ) {
		this( program, null );
	}

	public SettingsAction( Xenon program, String fragment ) {
		super( program );
		if( fragment != null ) uri = ProgramSettingsType.URI.resolve( "#" + fragment );
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public void handle( ActionEvent event ) {
		getProgram().getResourceManager().openAsset( uri );
	}

}
