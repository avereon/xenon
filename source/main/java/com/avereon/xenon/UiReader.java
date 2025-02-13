package com.avereon.xenon;

import com.avereon.log.LazyEval;
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
import java.util.function.Consumer;
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

	private final Map<String, Tool> tools = new HashMap<>();

	private final Map<WorkpaneView, Set<Tool>> viewToolMap = new HashMap<>();

	private final Map<Workspace, Workarea> spaceActiveAreas = new HashMap<>();

	private final Map<WorkpaneView, Tool> viewActiveTools = new HashMap<>();

	private final List<Exception> errors = new ArrayList<>();

	private final Lock restoreLock = new ReentrantLock();

	private final Condition restoredCondition = restoreLock.newCondition();

	private Workspace activeSpace;

	private Workarea activeArea;

	private WorkpaneView activeView;

	private Tool activeTool;

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
			getUiSettings( ProgramSettings.WORKSPACE ).forEach( this::loadSpace );
			getUiSettings( ProgramSettings.AREA ).forEach( this::loadArea );
			getUiSettings( ProgramSettings.VIEW ).forEach( this::loadView );
			getUiSettings( ProgramSettings.EDGE ).forEach( this::loadEdge );
			getUiSettings( ProgramSettings.TOOL ).forEach( this::loadTool );

			// Reassemble the UI
			linkTools();
			linkEdgesAndViews();
			linkAreas();
			addSpaces();

			// NEXT Continue work on UI reader
			// - Reassemble the UI
			//   - Link views and edges to areas
			//   - Link tools to views
			//   - Load the workspaces into the manager

			// NEXT Set all the active UI components
			// - Set active tool
			// - Set active view
			// - Set default view
			// - Set active workarea
			log.atWarn().log( "activeSpace: %s", activeSpace );
			log.atWarn().log( "activeArea: %s", activeArea );
			log.atWarn().log( "activeView: %s", activeView );
			log.atWarn().log( "activeTool: %s", activeTool );
			// if( activeTool != null ) activeTool.setActiveWhenReady();

			// If there are exceptions restoring the UI notify the user
			if( !errors.isEmpty() ) notifyUserOfErrors( errors );
		} finally {
			restored = true;
			restoredCondition.signalAll();
			restoreLock.unlock();
		}
	}

	Workspace loadSpace( Settings settings ) {
		try {
			Workspace workspace = loadSpaceFromSettings( settings );
			spaces.put( workspace.getUid(), workspace );
			return workspace;
		} catch( Exception exception ) {
			log.atError( exception ).log( "Error restoring workspace" );
			return null;
		}
	}

	Workspace loadSpaceFromSettings( Settings settings ) {
		Workspace workspace = new Workspace( program );
		workspace.setUid( settings.getName() );
		workspace.updateFromSettings( settings );
		if( isActive( settings ) ) activeSpace = workspace;
		return workspace;
	}

	Workarea loadArea( Settings settings ) {
		try {
			String id = settings.getName();
			Workspace workspace = spaces.get( settings.get( UiFactory.PARENT_WORKSPACE_ID ) );

			// If the workspace is not found, then the workarea is orphaned...delete the settings
			if( workspace == null ) {
				if( isModifying() ) {
					getProgram().getSettingsManager().getSettings( ProgramSettings.PANE, id ).delete();
					settings.delete();
				}
				throw new UiException( "Removed orphaned area id=" + id );
			}

			Workarea workarea = loadAreaFromSettings( settings );
			areas.put( id, workarea );
			return workarea;
		} catch( Exception exception ) {
			errors.add( exception );
			return null;
		}
	}

	Workarea loadAreaFromSettings( Settings settings ) {
		Workarea area = new Workarea();
		area.setUid( settings.getName() );
		if( isActive( settings ) ) activeArea = area;
		return area;
	}

	WorkpaneView loadView( Settings settings ) {
		try {
			String id = settings.getName();
			Workpane workpane = areas.get( settings.get( UiFactory.PARENT_WORKPANE_ID ) );

			// If the workpane is not found, then the view is orphaned...delete the settings
			if( workpane == null ) {
				if( isModifying() ) settings.delete();
				throw new UiException( "Removed orphaned view id=" + id );
			}

			WorkpaneView view = loadViewFromSettings( settings );
			views.put( id, view );
			return view;
		} catch( Exception exception ) {
			errors.add( exception );
			return null;
		}
	}

	WorkpaneView loadViewFromSettings( Settings settings ) {
		WorkpaneView view = new WorkpaneView();
		view.setUid( settings.getName() );
		//if( settings.exists( "placement" ) ) view.setPlacement( Workpane.Placement.valueOf( settings.get( "placement" ).toUpperCase() ) );
		if( settings.exists( "placement" ) ) view.setPlacement( settings.get( "placement", Workpane.Placement.class ) );
		if( isActive( settings ) ) activeView = view;
		return view;
	}

	WorkpaneEdge loadEdge( Settings settings ) {
		try {
			String id = settings.getName();
			Workpane workpane = areas.get( settings.get( UiFactory.PARENT_WORKPANE_ID ) );

			// If the workpane is not found, then the edge is orphaned...delete the settings
			if( workpane == null ) {
				if( isModifying() ) settings.delete();
				throw new UiException( "Removed orphaned edge id=" + id );
			}

			WorkpaneEdge edge = loadEdgeFromSettings( settings );
			edges.put( id, edge );
			return edge;
		} catch( Exception exception ) {
			errors.add( exception );
			return null;
		}
	}

	WorkpaneEdge loadEdgeFromSettings( Settings settings ) {
		WorkpaneEdge edge = new WorkpaneEdge();
		edge.setUid( settings.getName() );
		if( settings.exists( "orientation" ) ) edge.setOrientation( Orientation.valueOf( settings.get( "orientation" ).toUpperCase() ) );
		if( settings.exists( "position" ) ) edge.setPosition( settings.get( "position", Double.class ) );
		return edge;
	}

	void loadTool( Settings settings ) {
		try {
			String id = settings.getName();
			URI uri = settings.get( Asset.SETTINGS_URI_KEY, URI.class );
			WorkpaneView view = views.get( settings.get( UiFactory.PARENT_WORKPANEVIEW_ID ) );

			// If the view is not found, then the tool is orphaned...delete the settings
			if( view == null || uri == null ) {
				if( isModifying() ) settings.delete();
				throw new UiException( "Removed orphaned tool id=" + id );
			}

			Tool tool = loadToolFromSettings( settings );
			viewToolMap.computeIfAbsent( view, k -> new HashSet<>() ).add( tool );
			tools.put( id, tool );
		} catch( Exception exception ) {
			errors.add( exception );
		}
	}

	ProgramTool loadToolFromSettings( Settings settings ) throws AssetException, ToolInstantiationException {
		String toolClassName = settings.get( Tool.SETTINGS_TYPE_KEY );
		URI uri = settings.get( Asset.SETTINGS_URI_KEY, URI.class );
		String assetTypeKey = settings.get( Asset.SETTINGS_TYPE_KEY );
		Integer order = settings.get( Tool.ORDER, Integer.class, -1 );

		// Create the asset
		Asset asset;
		AssetType assetType = getProgram().getAssetManager().getAssetType( assetTypeKey );
		try {
			asset = getProgram().getAssetManager().createAsset( assetType, uri );
		} catch( AssetException exception ) {
			throw new AssetNotFoundException( new Asset( assetType, uri ), exception );
		}

		// Create the open asset request
		OpenAssetRequest openAssetRequest = new OpenAssetRequest();
		openAssetRequest.setToolId( settings.getName() );
		openAssetRequest.setAsset( asset );
		openAssetRequest.setToolClassName( toolClassName );

		// Restore the tool
		ProgramTool tool = getProgram().getToolManager().restoreTool( openAssetRequest );
		if( tool == null ) {
			if( isModifying() ) settings.delete();
			throw new ToolInstantiationException( settings.getName(), toolClassName );
		}

		tool.setOrder(order);

		//if( isActive( settings ) ) activeTool = tool;

		return tool;
	}

	private boolean isActive( Settings settings ) {
		return settings.get( "active", Boolean.class, false );
	}

	private void linkAreas() {
		// Sort the areas by order
		List<Workarea> areaList = new ArrayList<>( areas.values() );
		areaList.sort( Comparator.comparing( Workarea::getOrder ) );

		// Link the workareas to the workspaces
		for( Workarea workarea : areaList ) {
			try {
				Settings settings = getProgram().getSettingsManager().getSettings( ProgramSettings.AREA, workarea.getUid() );
				Workspace workspace = spaces.get( settings.get( UiFactory.PARENT_WORKSPACE_ID ) );
				workspace.addWorkarea( workarea );

				// Save the active area for later
				if( workarea.isActive() ) spaceActiveAreas.put( workspace, workarea );
			} catch( Exception exception ) {
				errors.add( exception );
			}
		}
	}

	private void linkEdgesAndViews() {
		Map<Workpane, Set<WorkpaneEdge>> workpaneEdges = new HashMap<>();
		Map<Workpane, Set<WorkpaneView>> workpaneViews = new HashMap<>();

		// Link the edges
		for( WorkpaneEdge edge : edges.values() ) {
			Settings settings = getProgram().getSettingsManager().getSettings( ProgramSettings.EDGE, edge.getUid() );
			Workpane workpane = areas.get( settings.get( UiFactory.PARENT_WORKPANE_ID ) );
			try {
				if( linkEdge( edge ) ) {
					workpaneEdges.computeIfAbsent( workpane, k -> new HashSet<>() ).add( edge );
				} else {
					log.atDebug().log( "Removing invalid workpane edge settings: %s", LazyEval.of( settings::getName ) );
					settings.delete();
				}
			} catch( Exception exception ) {
				log.atWarn( exception ).log( "Error linking edge: %s", LazyEval.of( edge::getUid ) );
				return;
			}
		}

		// Link the views
		for( WorkpaneView view : views.values() ) {
			Settings settings = getProgram().getSettingsManager().getSettings( ProgramSettings.VIEW, view.getUid() );
			Workpane workpane = areas.get( settings.get( UiFactory.PARENT_WORKPANE_ID ) );
			try {
				if( linkView( view ) ) {
					workpaneViews.computeIfAbsent( workpane, k -> new HashSet<>() ).add( view );
				} else {
					log.atDebug().log( "Removing invalid workpane edge settings: %s", LazyEval.of( settings::getName ) );
					settings.delete();
				}
			} catch( Exception exception ) {
				log.atWarn( exception ).log( "Error linking view: %s", LazyEval.of( view::getUid ), exception );
				return;
			}
		}

		// Restore edges and views to workpane
		for( Workpane pane : areas.values() ) {
			Set<WorkpaneEdge> localEdges = workpaneEdges.computeIfAbsent( pane, k -> new HashSet<>() );
			Set<WorkpaneView> localViews = workpaneViews.computeIfAbsent( pane, k -> new HashSet<>() );
			pane.restoreNodes( localEdges, localViews );

			//			// UIFactory gets sent a flag to not set the defaults when restoring the UI.
			//			// This works around the problem of the default view being overwritten.
			//
			//			// Active, default and maximized views
			//			Settings settings = getProgram().getSettingsManager().getSettings( ProgramSettings.PANE, pane.getUid() );
			//			setView( settings, "view-active", pane::setActiveView );
			//			setView( settings, "view-default", pane::setDefaultView );
			//			setView( settings, "view-maximized", pane::setMaximizedView );
		}
	}

	boolean linkEdge( WorkpaneEdge edge ) {
		return false;
	}

	boolean linkView( WorkpaneView view ) {
		return false;
	}

	void setView( Settings settings, String key, Consumer<WorkpaneView> handler ) {

	}

	void linkTools() {
		for( Map.Entry<WorkpaneView, Set<Tool>> entry : viewToolMap.entrySet() ) {
			WorkpaneView view = entry.getKey();
			Workpane pane = view.getWorkpane();
			if( pane == null ) continue;

			List<Tool> localTools = new ArrayList<>( entry.getValue() );

			// Sort the tools
			localTools.sort( Comparator.comparing( Tool::getOrder ) );

			// Add the tools to the view
			for( Tool tool : localTools ) {
				pane.addTool( tool, view, false );

				Settings settings = getProgram().getSettingsManager().getSettings( ProgramSettings.TOOL, tool.getUid() );
				boolean isActive = settings.get( "active", Boolean.class, false );
				if( isActive ) viewActiveTools.put( view, tool );

				log.atDebug().log( "Tool linked: %s: %s", LazyEval.of( tool::getClass ), LazyEval.of( () -> tool.getAsset().getUri() ) );
			}
		}
	}

	void addSpaces() {
		List<Workspace> spacesList = new ArrayList<>( spaces.values() );
		spacesList.sort( Comparator.comparing( Workspace::getOrder ) );

		for( Workspace workspace : spacesList ) {
			if( modifying ) getProgram().getWorkspaceManager().addWorkspace( workspace );
		}
	}

	private WorkpaneEdge lookupEdge( Workarea area, String id ) {
		if( area == null ) throw new NullPointerException( "Workpane cannot be null" );
		if( id == null ) throw new NullPointerException( "Edge id cannot be null" );

		WorkpaneEdge edge = edges.get( id );
		if( edge == null ) edge = area.getWallEdge( id.charAt( 0 ) );

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
