package com.avereon.xenon;

import com.avereon.util.Controllable;
import com.avereon.util.LogUtil;
import com.avereon.xenon.tool.ProgramTool;
import com.avereon.xenon.util.DialogUtil;
import com.avereon.xenon.workarea.Tool;
import com.avereon.xenon.workarea.Workpane;
import com.avereon.xenon.workspace.Workspace;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import org.slf4j.Logger;

import java.lang.invoke.MethodHandles;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class WorkspaceManager implements Controllable<WorkspaceManager> {

	private static final Logger log = LogUtil.get( MethodHandles.lookup().lookupClass() );

	private Program program;

	private Set<Workspace> workspaces;

	private Workspace activeWorkspace;

	WorkspaceManager( Program program ) {
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

//	@Override
//	public WorkspaceManager awaitStart( long timeout, TimeUnit unit ) {
//		return this;
//	}
//
//	@Override
//	public WorkspaceManager restart() {
//		return this;
//	}
//
//	@Override
//	public WorkspaceManager awaitRestart( long timeout, TimeUnit unit ) {
//		return this;
//	}

	public WorkspaceManager stop() {
		// If this is called after Platform.exit(), which is usually the case
		// then the result of closing the stages is unpredictable. Not trying to
		// close the stages seems to work fine and the program exits normally.
		//
		// ... But during unit testing, Platform.exit() cannot be called or
		// it hangs the tests. Furthermore, the tests will need to call
		// Program.stop() which, in turn, calls WorkspaceManager.stop(), which
		// should close the stages or they stay open during the duration of the
		// testing process.
		//
		// RESULT Do not close the stages in this method. The unit tests will just
		// have to close the stages as part of the cleanup.

		activeWorkspace = null;
		workspaces.clear();

		return this;
	}

//	@Override
//	public WorkspaceManager awaitStop( long timeout, TimeUnit unit ) {
//		// This method intentionally does nothing. See explanation in stop() method.
//		return this;
//	}

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

	public void showActiveWorkspace() {
		Platform.runLater( () -> {
			Workspace workspace = getActiveWorkspace();
			if( workspace == null ) return;
			Stage stage = workspace.getStage();
			if( stage == null ) return;
			stage.show();
			stage.requestFocus();
		} );
	}

	public Workspace getActiveWorkspace() {
		if( activeWorkspace != null ) return activeWorkspace;
		if( workspaces.size() > 0 ) return workspaces.iterator().next();
		throw new IllegalStateException( "No workspaces exist" );
	}

	public Stage getActiveStage() {
		if( activeWorkspace != null ) return activeWorkspace.getStage();
		if( workspaces.size() > 0 ) return workspaces.iterator().next().getStage();
		throw new IllegalStateException( "No available stage" );
	}

	public void requestCloseWorkspace( Workspace workspace ) {
		boolean closeProgram = workspaces.size() == 1;
		if( closeProgram ) {
			program.requestExit( false );
		} else {
			Alert alert = new Alert( Alert.AlertType.CONFIRMATION, "", ButtonType.YES, ButtonType.NO );
			alert.setTitle( program.getResourceBundle().getString( "workspace", "workspace.close.title" ) );
			alert.setHeaderText( program.getResourceBundle().getString( "workspace", "workspace.close.message" ) );
			alert.setContentText( program.getResourceBundle().getString( "workspace", "workspace.close.prompt" ) );
			alert.initOwner( workspace.getStage() );

			Stage stage = program.getWorkspaceManager().getActiveStage();
			Optional<ButtonType> result = DialogUtil.showAndWait( stage, alert );

			if( result.isPresent() && result.get() == ButtonType.YES ) closeWorkspace( workspace );
		}
	}

	private void closeWorkspace( Workspace workspace ) {
		removeWorkspace( workspace );
		workspace.close();
	}

	public Workpane getActiveWorkpane() {
		return getActiveWorkspace().getActiveWorkarea().getWorkpane();
	}

	public Set<Tool> getActiveTools( Class<? extends Tool> type ) {
		return getActiveWorkpane().getTools( type );
	}

	public void requestCloseTools( Class<? extends ProgramTool> type ) {
		Platform.runLater( () -> {
			Set<Tool> tools = getActiveTools( type );
			tools.forEach( Tool::close );
		} );
	}

	void hideWindows() {
		for( Workspace workspace : getWorkspaces() ) {
			workspace.getStage().hide();
		}
	}

}
