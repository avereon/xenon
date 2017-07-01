package com.parallelsymmetry.essence;

import com.parallelsymmetry.essence.util.Controllable;
import com.parallelsymmetry.essence.workarea.Workspace;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import org.slf4j.Logger;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class WorkspaceManager implements Controllable<WorkspaceManager> {

	private static Logger log = LogUtil.get( WorkspaceManager.class );

	private Program program;

	private Set<Workspace> workspaces;

	private Workspace activeWorkspace;

	private CountDownLatch stopLatch;

	public WorkspaceManager( Program program ) {
		this.program = program;
		workspaces = new CopyOnWriteArraySet<>();
	}

	@Override
	public boolean isRunning() {
		return workspaces.size() > 0;
	}

	@Override
	public WorkspaceManager start() {
		return this;
	}

	@Override
	public WorkspaceManager awaitStart( long timeout, TimeUnit unit ) throws InterruptedException {
		return this;
	}

	@Override
	public WorkspaceManager restart() {
		return this;
	}

	@Override
	public WorkspaceManager awaitRestart( long timeout, TimeUnit unit ) throws InterruptedException {
		return this;
	}

	public WorkspaceManager stop() {
		// Hide all the workspace stages
		stopLatch = new CountDownLatch( workspaces.size() );
		for( Workspace workspace : workspaces ) {
			Platform.runLater( () -> workspace.getStage().close() );
			workspace.getStage().onHiddenProperty().addListener( ( event ) -> stopLatch.countDown() );
			System.out.println( "Closed: " + workspace.getId() );
		}

		return this;
	}

	@Override
	public WorkspaceManager awaitStop( long timeout, TimeUnit unit ) throws InterruptedException {
		if( stopLatch != null ) {
			try {
				stopLatch.await( 10, TimeUnit.SECONDS );
			} catch( InterruptedException exception ) {
				log.error( "Timeout waiting for windows to close", exception );
			}
		}

		return this;
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

		if( activeWorkspace != null ) {
			activeWorkspace.setActive( true );
		}
	}

	public Workspace getActiveWorkspace() {
		return activeWorkspace;
	}

	public void requestCloseWorkspace( Workspace workspace ) {
		boolean closeProgram = workspaces.size() == 1;
		if( closeProgram ) {
			program.requestExit();
		} else {
			Alert alert = new Alert( Alert.AlertType.CONFIRMATION, "", ButtonType.YES, ButtonType.NO );
			alert.setTitle( program.getResourceBundle().getString( "workspace", "workspace.close.title" ) );
			alert.setHeaderText( program.getResourceBundle().getString( "workspace", "workspace.close.message" ) );
			alert.setContentText( program.getResourceBundle().getString( "workspace", "workspace.close.prompt" ) );
			alert.initOwner( workspace.getStage() );

			Optional<ButtonType> result = alert.showAndWait();

			if( result.isPresent() && result.get() == ButtonType.YES ) closeWorkspace( workspace );
		}
	}

	public void closeWorkspace( Workspace workspace ) {
		// This will close the program if it's the last window and
		// Platform.setImplicitExit( false ) has not been called
		workspace.getStage().close();

		// TODO Remove the workspace, workpane, workpane components, tool settings, etc.
		// TODO Remove the workspace from the workspace collection
	}

}
