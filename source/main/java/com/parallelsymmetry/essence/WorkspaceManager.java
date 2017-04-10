package com.parallelsymmetry.essence;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class WorkspaceManager {

	private Set<Workspace> workspaces;

	public WorkspaceManager() {
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
}
