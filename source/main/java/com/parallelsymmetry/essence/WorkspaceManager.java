package com.parallelsymmetry.essence;

import com.parallelsymmetry.essence.workarea.Workspace;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class WorkspaceManager {

	private Program program;

	private Set<Workspace> workspaces;

	private Workspace activeWorkspace;

	public WorkspaceManager( Program program ) {
		this.program = program;
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

	public void requestCloseWorkspace( Workspace workspace ) {
		if( workspaces.size() > 1 ) {
			Alert alert = new Alert( Alert.AlertType.CONFIRMATION, "", ButtonType.YES, ButtonType.NO );
			alert.setTitle( program.getResourceBundle().getString( "workspace", "workspace.close.title" ) );
			alert.setHeaderText( program.getResourceBundle().getString( "workspace", "workspace.close.message" ) );
			alert.setContentText( program.getResourceBundle().getString( "workspace", "workspace.close.prompt" ) );
			alert.initOwner( workspace.getStage() );

			Optional<ButtonType> result = alert.showAndWait();

			if( result.isPresent() && result.get() == ButtonType.YES ) closeWorkspace( workspace );
		} else {
			program.requestExit();
		}
	}

	public void closeWorkspace( Workspace workspace ) {
		System.out.println( "Close workspace: " + workspace.getStage().getTitle() );
		workspace.getStage().close();
//		if( workspaces.size() == 1 ) {
//			program.requestExit();
//		} else {
//		}
	}

	public void shutdown() {
		for( Workspace workspace : workspaces ) {
			Stage stage = workspace.getStage();
			System.out.println( "Closing window: " + stage.getTitle() );
			workspace.getStage().hide();
		}
	}

}
