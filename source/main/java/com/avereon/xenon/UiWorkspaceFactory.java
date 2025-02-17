package com.avereon.xenon;

import com.avereon.settings.Settings;
import com.avereon.util.IdGenerator;
import com.avereon.xenon.workspace.Workspace;

public class UiWorkspaceFactory {

	public static final double DEFAULT_WIDTH = 960;

	public static final double DEFAULT_HEIGHT = 600;

	private final Xenon program;

	public UiWorkspaceFactory( Xenon program ) {
		this.program = program;
	}

	public Workspace create() {
		Workspace space = new Workspace( program );
		space.setUid( IdGenerator.getId() );
		return space;
	}

	public Workspace applyWorkspaceSettings( Workspace workspace, Settings settings ) {
		return workspace;
	}

	public Workspace applyWorkspaceSettingsListeners( Workspace workspace, Settings settings ) {
		return workspace;
	}

}
