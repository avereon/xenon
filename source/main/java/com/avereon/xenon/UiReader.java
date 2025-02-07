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

	private final Map<String, Workspace> spaces = new HashMap<>();

	private final Map<String, Workarea> areas = new HashMap<>();

	private final Map<String, WorkpaneEdge> edges = new HashMap<>();

	private final Map<String, WorkpaneView> views = new HashMap<>();

	//private final Map<WorkpaneView, Set<ProgramTool>> viewTools = new HashMap<>();

	private final Map<String, Tool> tools = new HashMap<>();

	private final Map<Workspace, Workarea> activeAreas = new HashMap<>();

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
		doLoad();
	}

	public void waitForLoad( long duration, TimeUnit unit ) throws InterruptedException, TimeoutException {
		doWaitForLoad( duration, unit );
	}

	private void doLoad() {
		Fx.affirmOnFxThread();
		restoreLock.lock();

		try {
			// Load the entity ids
			// Note that areas and panes have the exact same ids
			List<String> spaceIds = getUiSettingsIds( ProgramSettings.WORKSPACE );
			List<String> areaIds = getUiSettingsIds( ProgramSettings.AREA );
			List<String> viewIds = getUiSettingsIds( ProgramSettings.VIEW );
			List<String> edgeIds = getUiSettingsIds( ProgramSettings.EDGE );
			List<String> toolIds = getUiSettingsIds( ProgramSettings.TOOL );
			log.at( logLevel ).log( "Number of items to restore: s=%s a=%s v=%s e=%s t=%s", spaceIds.size(), areaIds.size(), viewIds.size(), edgeIds.size(), toolIds.size() );

			// Load all the parts from settings in order: space, area/pane, view, edge, tool
			spaceIds.forEach( this::loadSpace );
			areaIds.forEach( this::loadArea );
			viewIds.forEach( this::loadView );
			edgeIds.forEach( this::loadEdge );
			toolIds.forEach( this::loadTool );

			// Reassemble the UI
			linkAreas();
			// NEXT Continue work on UI reader
			// - Reassemble the UI
			//   - Link views and edges to areas
			//   - Link tools to views

			// NEXT Set all the active UI components
			// - Set active tool
			// - Set active view
			// - Set default view
			// - Set active workarea

			// If there are exceptions restoring the UI notify the user
			if( !errors.isEmpty() ) notifyUserOfErrors( errors );
		} finally {
			restored = true;
			restoredCondition.signalAll();
			restoreLock.unlock();
		}
	}

	private void loadSpace( String id ) {
		try {
			Settings settings = getProgram().getSettingsManager().getSettings( ProgramSettings.WORKSPACE, id );
			Workspace workspace = loadSpaceFromSettings( settings );
			spaces.put( id, workspace );
		} catch( Exception exception ) {
			log.atError( exception ).log( "Error restoring workspace" );
		}
	}

	Workspace loadSpaceFromSettings( Settings settings ) {
		Workspace workspace = new Workspace( program );
		workspace.setUid( settings.getName() );
		workspace.updateFromSettings( settings );
		return workspace;
	}

	private void loadArea( String id ) {
		try {
			Settings settings = getProgram().getSettingsManager().getSettings( ProgramSettings.AREA, id );
			Workspace workspace = spaces.get( settings.get( UiFactory.PARENT_WORKSPACE_ID ) );

			// If the workspace is not found, then the workarea is orphaned...delete the settings
			if( workspace == null ) {
				if( isModifying() ) {
					getProgram().getSettingsManager().getSettings( ProgramSettings.PANE, id ).delete();
					settings.delete();
				}
				throw new UiException( "Removed orphaned workarea pane id=" + id );
			}

			Workarea workarea = loadAreaFromSettings( settings );
			areas.put( id, workarea );
		} catch( Exception exception ) {
			errors.add( exception );
		}
	}

	Workarea loadAreaFromSettings( Settings settings ) {
		Workarea area = new Workarea();
		area.setUid( settings.getName() );
		return area;
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

	private void linkAreas() {
		// Link the workareas to the workspaces
		for( Workarea workarea : areas.values() ) {
			try {
				Settings settings = getProgram().getSettingsManager().getSettings( ProgramSettings.AREA, workarea.getUid() );
				Workspace workspace = spaces.get( settings.get( UiFactory.PARENT_WORKSPACE_ID ) );
				workspace.addWorkarea( workarea );

				// Save the active area for later
				if( workarea.isActive() ) activeAreas.put( workspace, workarea );
			} catch( Exception exception ) {
				errors.add( exception );
			}
		}
	}

	//	private void linkEdgesAndViews() {
	//		Map<Workpane, Set<WorkpaneEdge>> workpaneEdges = new HashMap<>();
	//		Map<Workpane, Set<WorkpaneView>> workpaneViews = new HashMap<>();
	//
	//		// Link the edges
	//		for( WorkpaneEdge edge : edges.values() ) {
	//			Settings settings = getProgram().getSettingsManager().getSettings( ProgramSettings.EDGE, edge.getUid() );
	//			Workpane workpane = areas.get( settings.get( UiFactory.PARENT_WORKPANE_ID ) );
	//			try {
	//				if( linkEdge( edge ) ) {
	//					workpaneEdges.computeIfAbsent( workpane, k -> new HashSet<>() ).add( edge );
	//				} else {
	//					log.atDebug().log( "Removing invalid workpane edge settings: %s", LazyEval.of( settings::getName ) );
	//					settings.delete();
	//				}
	//			} catch( Exception exception ) {
	//				log.atWarn( exception ).log( "Error linking edge: %s", LazyEval.of( edge::getUid ) );
	//				return;
	//			}
	//		}
	//
	//		// Link the views
	//		for( WorkpaneView view : views.values() ) {
	//			Settings settings = getProgram().getSettingsManager().getSettings( ProgramSettings.VIEW, view.getUid() );
	//			Workpane workpane = areas.get( settings.get( UiFactory.PARENT_WORKPANE_ID ) );
	//			try {
	//				if( linkView( view ) ) {
	//					workpaneViews.computeIfAbsent( workpane, k -> new HashSet<>() ).add( view );
	//				} else {
	//					log.atDebug().log( "Removing invalid workpane edge settings: %s", LazyEval.of( settings::getName ) );
	//					settings.delete();
	//				}
	//			} catch( Exception exception ) {
	//				log.atWarn( exception ).log( "Error linking view: %s", LazyEval.of( view::getUid ), exception );
	//				return;
	//			}
	//		}
	//
	//		// Restore edges and views to workpane
	//		for( Workpane pane : areas.values() ) {
	//			Set<WorkpaneEdge> localEdges = workpaneEdges.computeIfAbsent( pane, k -> new HashSet<>() );
	//			Set<WorkpaneView> localViews = workpaneViews.computeIfAbsent( pane, k -> new HashSet<>() );
	//			pane.restoreNodes( localEdges, localViews );
	//

	/// /			// UIFactory gets sent a flag to not set the defaults when restoring the UI.
	/// /			// This works around the problem of the default view being overwritten.
	/// /
	/// /			// Active, default and maximized views
	/// /			Settings settings = getProgram().getSettingsManager().getSettings( ProgramSettings.PANE, pane.getUid() );
	/// /			setView( settings, "view-active", pane::setActiveView );
	/// /			setView( settings, "view-default", pane::setDefaultView );
	/// /			setView( settings, "view-maximized", pane::setMaximizedView );
	//		}
	//	}
	private WorkpaneEdge lookupEdge( Workarea area, String id ) {
		if( area == null ) throw new NullPointerException( "Workpane cannot be null" );
		if( id == null ) throw new NullPointerException( "Edge id cannot be null" );

		WorkpaneEdge edge = edges.get( id );

		// FIXME I can do better than this MVS 02 Feb 2025
		if( edge == null ) {
			try {
				edge = area.getWallEdge( id.charAt( 0 ) );
			} catch( IllegalArgumentException exception ) {
				// Intentionally ignore exception
			}
		}

		return edge;
	}

	private void doWaitForLoad( long duration, TimeUnit unit ) throws InterruptedException, TimeoutException {
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

	private List<Settings> getUiSettings( String path ) {
		return getUiSettingsIds( path ).stream().map( id -> getProgram().getSettingsManager().getSettings( path, id ) ).toList();
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
