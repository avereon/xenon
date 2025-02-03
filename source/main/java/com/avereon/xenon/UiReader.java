package com.avereon.xenon;

import com.avereon.product.Rb;
import com.avereon.settings.Settings;
import com.avereon.xenon.asset.Asset;
import com.avereon.xenon.asset.AssetType;
import com.avereon.xenon.asset.OpenAssetRequest;
import com.avereon.xenon.asset.exception.AssetException;
import com.avereon.xenon.asset.exception.AssetNotFoundException;
import com.avereon.xenon.notice.Notice;
import com.avereon.xenon.workpane.*;
import com.avereon.xenon.workspace.Workarea;
import com.avereon.xenon.workspace.Workspace;
import com.avereon.zarra.javafx.Fx;
import javafx.geometry.Orientation;
import lombok.CustomLog;
import lombok.Getter;
import lombok.Setter;

import java.net.URI;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;

@CustomLog
class UiReader {

	private static final Level logLevel = Level.CONFIG;

	@Getter
	private final Xenon program;

	private final Map<String, Workspace> workspaces = new HashMap<>();

	private final Map<String, Workarea> areas = new HashMap<>();

//	private final Map<String, Workpane> panes = new HashMap<>();

	private final Map<String, WorkpaneEdge> edges = new HashMap<>();

	private final Map<String, WorkpaneView> views = new HashMap<>();

	//private final Map<WorkpaneView, Set<ProgramTool>> viewTools = new HashMap<>();

	private final Map<String, Tool> tools = new HashMap<>();

	private final List<Exception> errors = new ArrayList<>();

	private final Lock restoreLock = new ReentrantLock();

	private final Condition restoredCondition = restoreLock.newCondition();

	private boolean restored;

	@Getter
	@Setter
	private boolean modifying;

	public UiReader( Xenon program ) {
		this.program = program;
	}

	public void load() {
		Fx.affirmOnFxThread();
		restoreLock.lock();

		try {
			// Load the entity ids
			// Note that areas and panes have the exact same ids
			List<String> workspaceIds = getUiSettingsIds( ProgramSettings.WORKSPACE );
			List<String> areaIds = getUiSettingsIds( ProgramSettings.AREA );
//			List<String> paneIds = getUiSettingsIds( ProgramSettings.PANE );
			List<String> viewIds = getUiSettingsIds( ProgramSettings.VIEW );
			List<String> edgeIds = getUiSettingsIds( ProgramSettings.EDGE );
			List<String> toolIds = getUiSettingsIds( ProgramSettings.TOOL );
			log.at( logLevel ).log( "Number of items to restore: s=%s a=%s v=%s e=%s t=%s", workspaceIds.size(), areaIds.size(), viewIds.size(), edgeIds.size(), toolIds.size() );

			// Load all the parts from settings in order: space, area/pane, view, edge, tool
			workspaceIds.forEach( this::loadWorkspace );
			areaIds.forEach( this::loadArea );
			viewIds.forEach( this::loadView );
			edgeIds.forEach( this::loadEdge );
			toolIds.forEach( this::loadTool );

			// If there are exceptions restoring the UI notify the user
			if( !errors.isEmpty() ) notifyUserOfErrors( errors );
		} finally {
			restored = true;
			restoredCondition.signalAll();
			restoreLock.unlock();
		}
	}

	private void loadWorkspace( String id ) {
		try {
			Settings settings = getProgram().getSettingsManager().getSettings( ProgramSettings.WORKSPACE, id );

			Workspace workspace = new Workspace( program );
			workspace.setUid( id );
			workspace.updateFromSettings( settings );
			workspaces.put( id, workspace );
		} catch( Exception exception ) {
			log.atError( exception ).log( "Error restoring workspace" );
		}
	}

	private void loadArea( String id ) {
		try {
			Settings settings = getProgram().getSettingsManager().getSettings( ProgramSettings.AREA, id );
			Workspace workspace = workspaces.get( settings.get( UiFactory.PARENT_WORKSPACE_ID ) );

			// If the workspace is not found, then the workarea is orphaned...delete the settings
			if( workspace == null ) {
				if( isModifying() ) {
					getProgram().getSettingsManager().getSettings( ProgramSettings.PANE, id ).delete();
					settings.delete();
				}
				throw new UiException( "Removed orphaned workarea pane id=" + id );
			}

			Workarea workarea = new Workarea();
			workarea.setUid( id );
			areas.put( id, workarea );
		} catch( Exception exception ) {
			errors.add( exception );
		}
	}

	private void loadView( String id ) {
		try {
			Settings settings = getProgram().getSettingsManager().getSettings( ProgramSettings.VIEW, id );
			Workpane workpane = areas.get( settings.get( UiFactory.PARENT_WORKPANE_ID ) );

			// If the workpane is not found, then the view is orphaned...delete the settings
			if( workpane == null ) {
				if( isModifying() ) settings.delete();
				throw new UiException( "Removed orphaned workpane view id=" + id );
			}

			WorkpaneView view = new WorkpaneView();
			view.setUid( id );
			if( settings.exists( "placement" ) ) view.setPlacement( Workpane.Placement.valueOf( settings.get( "placement" ).toUpperCase() ) );

			views.put( view.getUid(), view );
		} catch( Exception exception ) {
			errors.add( exception );
		}
	}

	private void loadEdge( String id ) {
		try {
			Settings settings = getProgram().getSettingsManager().getSettings( ProgramSettings.EDGE, id );
			Workpane workpane = areas.get( settings.get( UiFactory.PARENT_WORKPANE_ID ) );

			// If the workpane is not found, then the edge is orphaned...delete the settings
			if( workpane == null ) {
				if( isModifying() ) settings.delete();
				throw new UiException( "Removed orphaned workpane edge id=" + id );
			}

			WorkpaneEdge edge = new WorkpaneEdge();
			edge.setUid( id );
			if( settings.exists( "orientation" ) ) edge.setOrientation( Orientation.valueOf( settings.get( "orientation" ).toUpperCase() ) );
			if( settings.exists( "position" ) ) edge.setPosition( settings.get( "position", Double.class ) );

			edges.put( id, edge );
		} catch( Exception exception ) {
			errors.add( exception );
		}
	}

	private void loadTool( String id ) {
		try {
			Settings settings = getProgram().getSettingsManager().getSettings( ProgramSettings.TOOL, id );
			String toolClassName = settings.get( Tool.SETTINGS_TYPE_KEY );
			URI uri = settings.get( Asset.SETTINGS_URI_KEY, URI.class );
			String assetTypeKey = settings.get( Asset.SETTINGS_TYPE_KEY );
			AssetType assetType = getProgram().getAssetManager().getAssetType( assetTypeKey );
			WorkpaneView view = views.get( settings.get( UiFactory.PARENT_WORKPANEVIEW_ID ) );

			// If the view is not found, then the tool is orphaned...delete the settings
			if( view == null || uri == null ) {
				if( isModifying() ) settings.delete();
				throw new UiException( "Removed orphaned tool id=" + id );
			}

			// Create the asset
			Asset asset;
			try {
				asset = getProgram().getAssetManager().createAsset( assetType, uri );
			} catch( AssetException exception ) {
				throw new AssetNotFoundException( new Asset( assetType, uri ), exception );
			}

			// Create the open asset request
			OpenAssetRequest openAssetRequest = new OpenAssetRequest();
			openAssetRequest.setToolId( id );
			openAssetRequest.setAsset( asset );
			openAssetRequest.setToolClassName( toolClassName );

			// Restore the tool
			ProgramTool tool = getProgram().getToolManager().restoreTool( openAssetRequest );
			if( tool == null ) {
				if( isModifying() ) settings.delete();
				throw new ToolInstantiationException( id, toolClassName );
			}

			tools.put( tool.getUid(), tool );
			//viewTools.computeIfAbsent( view, k -> new HashSet<>() ).add( tool );
		} catch( Exception exception ) {
			errors.add( exception );
		}
	}

	public void waitForLoad( long duration, TimeUnit unit ) throws InterruptedException, TimeoutException {
		restoreLock.lock();
		try {
			while( !restored ) {
				if( !restoredCondition.await( duration, unit ) ) throw new TimeoutException( "Timeout waiting for UI restore" );
			}
		} finally {
			restoreLock.unlock();
		}
	}

	private List<String> getUiSettingsIds( String path ) {
		return getProgram().getSettingsManager().getSettings( path ).getNodes();
	}

	private void notifyUserOfErrors( List<Exception> exceptions ) {
		Set<String> messages = new HashSet<>();
		for( Exception exception : exceptions ) {
			log.atWarn().log( exception.getMessage() );

			if( exception instanceof ToolInstantiationException toolException ) {
				messages.add( Rb.text( RbKey.PROGRAM, "tool-missing", toolException.getToolClass() ) );
			} else if( exception instanceof AssetNotFoundException assetException ) {
				messages.add( Rb.text( RbKey.PROGRAM, "asset-missing", assetException.getAsset().getUri() ) );
			} else {
				messages.add( exception.getMessage() );
			}
		}

		//		List<String> sortedMessages = new ArrayList<>( messages );
		//		Collections.sort( sortedMessages );
		StringBuilder builder = new StringBuilder();
		for( String message : messages ) builder.append( "\n" ).append( message );

		Notice notice = new Notice( Rb.text( RbKey.PROGRAM, "ui-restore-error-title" ) );
		notice.setMessage( builder.toString().trim() );
		getProgram().getNoticeManager().addNotice( notice );
	}

}
