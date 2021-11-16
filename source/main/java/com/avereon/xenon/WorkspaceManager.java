package com.avereon.xenon;

import com.avereon.product.Rb;
import com.avereon.settings.SettingsEvent;
import com.avereon.skill.Controllable;
import com.avereon.util.IdGenerator;
import com.avereon.xenon.asset.Asset;
import com.avereon.xenon.util.DialogUtil;
import com.avereon.xenon.workpane.Tool;
import com.avereon.xenon.workpane.Workpane;
import com.avereon.xenon.workspace.Workarea;
import com.avereon.xenon.workspace.Workspace;
import com.avereon.zarra.javafx.Fx;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import lombok.CustomLog;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.stream.Collectors;

@CustomLog
public class WorkspaceManager implements Controllable<WorkspaceManager> {

	private final Program program;

	private final Set<Workspace> workspaces;

	private String currentThemeId;

	private Workspace activeWorkspace;

	private boolean uiReady;

	WorkspaceManager( Program program ) {
		this.program = program;
		this.workspaces = new CopyOnWriteArraySet<>();

		program.getSettings().register( SettingsEvent.CHANGED, e -> {
			if( "workspace-theme-id".equals( e.getKey() ) ) setTheme( (String)e.getNewValue() );
		} );
	}

	public Program getProgram() {
		return program;
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
		setUiReady( false );

		return this;
	}

	//	@Override
	//	public WorkspaceManager awaitStop( long timeout, TimeUnit unit ) {
	//		// This method intentionally does nothing. See explanation in stop() method.
	//		return this;
	//	}

	public boolean isUiReady() {
		return uiReady;
	}

	void setUiReady( boolean uiReady ) {
		this.uiReady = uiReady;
	}

	public String getTheme() {
		return currentThemeId;
	}

	public void setTheme( String id ) {
		ThemeMetadata theme = getProgram().getThemeManager().getMetadata( id );
		if( theme == null ) theme = getProgram().getThemeManager().getMetadata( id = "xenon-dark" );

		this.currentThemeId = id;
		final ThemeMetadata finalTheme = theme;
		workspaces.forEach( w -> w.setTheme( finalTheme.getStylesheet() ) );
	}

	public Set<Workspace> getWorkspaces() {
		return new HashSet<>( workspaces );
	}

	public Workspace newWorkspace() {
		return newWorkspace( IdGenerator.getId() );
	}

	public Workspace newWorkspace( String id ) {
		Workspace workspace = new Workspace( program );
		workspace.setUid( id );
		workspace.updateFromSettings( program.getSettingsManager().getSettings( ProgramSettings.WORKSPACE, id ) );
		workspace.setTheme( getProgram().getThemeManager().getMetadata( currentThemeId ).getStylesheet() );
		workspace.getEventBus().parent( program.getFxEventHub() );

		return workspace;
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
		Fx.run( () -> {
			Workspace workspace = getActiveWorkspace();
			if( workspace == null ) return;
			Stage stage = workspace.getStage();
			if( stage == null ) return;
			stage.show();
			stage.requestFocus();
		} );
	}

	public Workspace getActiveWorkspace() {
		if( activeWorkspace == null && workspaces.size() > 0 ) setActiveWorkspace( workspaces.iterator().next() );
		if( activeWorkspace != null ) return activeWorkspace;
		throw new IllegalStateException( "No workspaces available" );
	}

	public Stage getActiveStage() {
		if( activeWorkspace != null ) return getActiveWorkspace().getStage();
		throw new IllegalStateException( "No workspace stages available" );
	}

	public Set<Tool> getAssetTools( Asset asset ) {
		return workspaces.stream().flatMap( w -> w.getWorkareas().stream() ).flatMap( a -> a.getWorkpane().getTools().stream() ).filter( t -> t.getAsset() == asset ).collect( Collectors.toSet() );
	}

	public Set<Asset> getModifiedAssets() {
		return workspaces
			.stream()
			.flatMap( w -> w.getWorkareas().stream() )
			.flatMap( a -> a.getWorkpane().getTools().stream() )
			.map( Tool::getAsset )
			.filter( Asset::isNewOrModified )
			.collect( Collectors.toSet() );
	}

	public Set<Asset> getModifiedAssets( Workspace workspace ) {
		return workspace.getWorkareas().stream().flatMap( a -> a.getWorkpane().getTools().stream() ).map( Tool::getAsset ).filter( Asset::isNewOrModified ).collect( Collectors.toSet() );
	}

	/**
	 * Handle modified assets by asking the user what to do with them. The assets
	 * can be provided from any scope (program, workspace, workarea, tool, etc.).
	 *
	 * @param assets The modified assets to handle
	 * @return False if the user chooses to cancel the operation
	 */
	public boolean handleModifiedAssets( ProgramScope scope, Set<Asset> assets ) {
		if( assets.isEmpty() ) return true;

		boolean autoSave = getProgram().getSettings().get( "shutdown-autosave", Boolean.class, false );
		if( autoSave ) {
			getProgram().getAssetManager().saveAssets( assets );
			return true;
		}

		Alert alert = new Alert( Alert.AlertType.CONFIRMATION, "", ButtonType.YES, ButtonType.NO, ButtonType.CANCEL );
		alert.setTitle( Rb.text( RbKey.PROGRAM, "asset-modified" ) );
		alert.setHeaderText( Rb.text( RbKey.PROGRAM, "asset-modified-message" ) );
		alert.setContentText( Rb.text( RbKey.PROGRAM, "asset-modified-prompt" ) );
		alert.initOwner( getActiveWorkspace().getStage() );

		Stage stage = program.getWorkspaceManager().getActiveStage();
		Optional<ButtonType> result = DialogUtil.showAndWait( stage, alert );

		if( result.isPresent() ) {
			if( result.get() == ButtonType.YES ) getProgram().getAssetManager().saveAssets( assets );
			return result.get() == ButtonType.YES || result.get() == ButtonType.NO;
		}

		return false;
	}

	public void requestCloseWorkspace( Workspace workspace ) {
		long visibleWorkspaces = workspaces.stream().filter( w -> w.getStage().isShowing() ).count();
		log.atWarning().log( "Number of visible workspaces: %s", visibleWorkspaces );
		boolean closeProgram = visibleWorkspaces == 1;
		boolean shutdownVerify = getProgram().getSettings().get( "shutdown-verify", Boolean.class, true );

		if( closeProgram ) {
			program.requestExit( false, false );
		} else {
			if( !handleModifiedAssets( ProgramScope.WORKSPACE, getModifiedAssets( workspace ) ) ) return;

			boolean shouldContinue = !shutdownVerify;
			if( shutdownVerify ) {
				Alert alert = new Alert( Alert.AlertType.CONFIRMATION, "", ButtonType.YES, ButtonType.NO );
				alert.setTitle( Rb.text( "workspace", "workspace-close-title" ) );
				alert.setHeaderText( Rb.text( "workspace", "workspace-close-message" ) );
				alert.setContentText( Rb.text( "workspace", "workspace-close-prompt" ) );
				alert.initOwner( workspace.getStage() );

				Stage stage = program.getWorkspaceManager().getActiveStage();
				Optional<ButtonType> result = DialogUtil.showAndWait( stage, alert );

				shouldContinue = result.isPresent() && result.get() == ButtonType.YES;
			}

			if( shouldContinue ) closeWorkspace( workspace );
		}
	}

	public Workpane getActiveWorkpane() {
		Workspace workspace = getActiveWorkspace();
		if( workspace == null ) return null;
		Workarea workarea = workspace.getActiveWorkarea();
		if( workarea == null ) return null;
		return getActiveWorkspace().getActiveWorkarea().getWorkpane();
	}

	public Set<Tool> getActiveWorkpaneTools( Class<? extends Tool> type ) {
		Workpane workpane = getActiveWorkpane();
		if( workpane == null ) return Set.of();
		return workpane.getTools( type );
	}

	public void requestCloseTools( Class<? extends ProgramTool> type ) {
		Fx.run( () -> getActiveWorkpaneTools( type ).forEach( Tool::close ) );
	}

	public Workspace findWorkspace( ProgramTool tool ) {
		for( Workspace workspace : getWorkspaces() ) {
			for( Workarea workarea : workspace.getWorkareas() ) {
				for( Tool check : workarea.getWorkpane().getTools() ) {
					if( check == tool ) return workspace;
				}
			}
		}
		return null;
	}

	void hideWindows() {
		getWorkspaces().forEach( workspace -> workspace.getStage().hide() );
	}

	private void closeWorkspace( Workspace workspace ) {
		removeWorkspace( workspace );
		workspace.close();
	}

}
