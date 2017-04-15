package com.parallelsymmetry.essence;

import com.parallelsymmetry.essence.work.Workspace;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class WorkspaceManager {

	private Set<Workspace> workspaces;

	private Workspace activeWorkspace;

	public WorkspaceManager( Program program ) {
		workspaces = new CopyOnWriteArraySet<>();
	}

	public Set<Workspace> getWorkspaces() {
		return new HashSet<>( workspaces );
	}

	public void addWorkspace( Workspace workspace ) {
		workspaces.add( workspace );
	}

	public void removeWorkspace( Workspace workspace ) {
		workspaces.remove( workspace );
	}

	public void setActiveWorkspace( Workspace workspace ) {
		// If the workspace is not already added, add it
		if( !workspaces.contains( workspace ) ) addWorkspace( workspace );

		if( activeWorkspace != null ) {
			activeWorkspace.setActive( false );
		}

		activeWorkspace = workspace;

		if( activeWorkspace != null) {
			activeWorkspace.setActive( true );
		}
	}

	public Workspace getActiveWorkspace() {
		return activeWorkspace;
	}

}
