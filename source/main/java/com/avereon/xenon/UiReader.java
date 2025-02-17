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
import com.avereon.zarra.color.Colors;
import com.avereon.zarra.color.Paints;
import com.avereon.zarra.javafx.Fx;
import javafx.geometry.Orientation;
import javafx.geometry.Side;
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

	private final UiWorkspaceFactory spaceFactory;

	private final UiWorkareaFactory areaFactory;

	private final Map<String, Workspace> spaces = new HashMap<>();

	private final Map<String, Workarea> areas = new HashMap<>();

	private final Map<String, WorkpaneEdge> edges = new HashMap<>();

	private final Map<String, WorkpaneView> views = new HashMap<>();

	private final Map<String, Tool> tools = new HashMap<>();

	private final Map<WorkpaneView, Set<Tool>> viewToolMap = new HashMap<>();

	private Workspace activeSpace;

	private final Set<Workspace> maximizedSpaces = new HashSet<>();

	private final Map<Workspace, Workarea> spaceActiveAreas = new HashMap<>();

	private final Map<Workarea, WorkpaneView> areaActiveViews = new HashMap<>();

	private final Map<Workarea, WorkpaneView> areaDefaultViews = new HashMap<>();

	private final Map<Workarea, WorkpaneView> areaMaximizedViews = new HashMap<>();

	private final Map<WorkpaneView, Tool> viewActiveTools = new HashMap<>();

	private final List<Exception> errors = new ArrayList<>();

	private final Lock restoreLock = new ReentrantLock();

	private final Condition restoredCondition = restoreLock.newCondition();

	private boolean restored;

	@Getter
	@Setter
	@Deprecated( forRemoval = true )
	private boolean modifying;

	public UiReader( Xenon program ) {
		this.program = program;
		this.spaceFactory = new UiWorkspaceFactory( program );
		this.areaFactory = new UiWorkareaFactory( program );
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
			getUiSettings( ProgramSettings.WORKSPACE ).forEach( this::loadSpaceForLinking );
			getUiSettings( ProgramSettings.AREA ).forEach( this::loadAreaForLinking );
			getUiSettings( ProgramSettings.VIEW ).forEach( this::loadViewForLinking );
			getUiSettings( ProgramSettings.EDGE ).forEach( this::loadEdgeForLinking );
			getUiSettings( ProgramSettings.TOOL ).forEach( this::loadToolForLinking );

			// Reassemble the UI
			linkToolsToViews();
			linkEdgesAndViewsToAreas();
			linkAreasToSpaces();
			linkSpaces();

//			log.atWarn().log( "activeSpace: %s", activeSpace );
//			log.atWarn().log( "maximizedSpaces: %s", maximizedSpaces.size() );
//			log.atWarn().log( "spaceActiveAreas: %s", spaceActiveAreas.size() );
//			log.atWarn().log( "areaActiveViews: %s", areaActiveViews.size() );
//			log.atWarn().log( "areaDefaultViews: %s", areaDefaultViews.size() );
//			log.atWarn().log( "areaMaximizedViews: %s", areaMaximizedViews.size() );
//			log.atWarn().log( "viewActiveTools: %s", viewActiveTools.size() );

			/*
			Now that everything is linked, time to restore the flags. This should be
			done after the listeners are added to allow the events to be handled.

			The UiFactory methods add listeners to the UI components. UiReader
			intentionally avoids doing this to avoid triggering events during
			restoration. So the listeners need to be added here.
			 */

			// NEXT Set all the active, default and maximized UI components
			// - Set active tool
			// - Set active view
			// - Set default view
			// - Set active workarea
			// if( activeTool != null ) activeTool.setActiveWhenReady();
			// setAreaViews();

			// If there are exceptions restoring the UI notify the user
			if( !errors.isEmpty() ) notifyUserOfErrors( errors );
		} finally {
			restored = true;
			restoredCondition.signalAll();
			restoreLock.unlock();
		}
	}

	private void setAreaViews() {
		//		for( Workarea area : areas.values() ) {
		//			Settings settings = getProgram().getSettingsManager().getSettings( ProgramSettings.AREA, area.getUid() );
		//
		//			// Get the active, default and maximized views for the area
		//			//setView( settings, "view-active", area::setActiveView );
		//			//setView( settings, "view-default", area::setDefaultView );
		//			//setView( settings, "view-maximized", area::setMaximizedView );
		//		}
	}

	Workspace loadSpaceForLinking( Settings settings ) {
		try {
			Workspace workspace = loadSpace( settings );
			spaces.put( workspace.getUid(), workspace );
			return workspace;
		} catch( Exception exception ) {
			log.atError( exception ).log( "Error restoring workspace" );
			return null;
		}
	}

	Workspace loadSpace( Settings settings ) {
		Workspace workspace = spaceFactory.create();
		workspace.setUid( settings.getName() );
		workspace.updateFromSettings( settings );
		if( isActive( settings ) ) activeSpace = workspace;
		if( isMaximized( settings ) ) maximizedSpaces.add( workspace );
		return workspace;
	}

	Workarea loadAreaForLinking( Settings settings ) {
		try {
			copyPaneSettings( settings );

			String id = settings.getName();
			Workspace space = spaces.get( settings.get( UiFactory.PARENT_WORKSPACE_ID ) );

			// If the workspace is not found, then the workarea is orphaned...delete the settings
			if( space == null ) {
				if( isModifying() ) {
					getProgram().getSettingsManager().getSettings( ProgramSettings.AREA, id ).delete();
					settings.delete();
				}
				throw new UiException( "Removed orphaned area id=" + id );
			}

			Workarea area = loadArea( settings );
			if( isActive( settings ) ) spaceActiveAreas.put( space, area );

			areas.put( id, area );
			return area;
		} catch( Exception exception ) {
			errors.add( exception );
			return null;
		}
	}

	Workarea loadArea( Settings settings ) {
		Workarea area = areaFactory.create();
		area.setUid( settings.getName() );
		area.setOrder( settings.get( "order", Integer.class, area.getOrder() ) );
		area.setPaint( Paints.parse( settings.get( UiFactory.PAINT, Paints.toString( area.getPaint() ) ) ) );
		area.setColor( Colors.parse( settings.get( UiFactory.COLOR, Colors.toString( area.getColor() ) ) ) );
		area.setName( settings.get( UiFactory.NAME, area.getName() ) );
		return area;
	}

	WorkpaneView loadViewForLinking( Settings settings ) {
		try {
			String id = settings.getName();
			Workarea area = areas.get( settings.get( UiFactory.PARENT_WORKPANE_ID ) );

			// If the workpane is not found, then the view is orphaned...delete the settings
			if( area == null ) {
				if( isModifying() ) settings.delete();
				throw new UiException( "Removed orphaned view id=" + id );
			}

			WorkpaneView view = loadView( settings );
			views.put( id, view );
			return view;
		} catch( Exception exception ) {
			errors.add( exception );
			return null;
		}
	}

	WorkpaneView loadView( Settings settings ) {
		WorkpaneView view = new WorkpaneView();
		view.setUid( settings.getName() );
		if( settings.exists( "placement" ) ) view.setPlacement( settings.get( "placement", Workpane.Placement.class ) );
		return view;
	}

	WorkpaneEdge loadEdgeForLinking( Settings settings ) {
		try {
			String id = settings.getName();
			Workarea area = areas.get( settings.get( UiFactory.PARENT_WORKPANE_ID ) );

			// If the workpane is not found, then the edge is orphaned...delete the settings
			if( area == null ) {
				if( isModifying() ) settings.delete();
				throw new UiException( "Removed orphaned edge id=" + id );
			}

			WorkpaneEdge edge = loadEdge( settings );
			edges.put( id, edge );
			return edge;
		} catch( Exception exception ) {
			errors.add( exception );
			return null;
		}
	}

	WorkpaneEdge loadEdge( Settings settings ) {
		WorkpaneEdge edge = new WorkpaneEdge();
		edge.setUid( settings.getName() );
		if( settings.exists( "orientation" ) ) edge.setOrientation( Orientation.valueOf( settings.get( "orientation" ).toUpperCase() ) );
		if( settings.exists( "position" ) ) edge.setPosition( settings.get( "position", Double.class ) );
		return edge;
	}

	Tool loadToolForLinking( Settings settings ) {
		try {
			String id = settings.getName();
			URI uri = settings.get( Asset.SETTINGS_URI_KEY, URI.class );
			WorkpaneView view = views.get( settings.get( UiFactory.PARENT_WORKPANEVIEW_ID ) );

			// If the view is not found, then the tool is orphaned...delete the settings
			if( view == null || uri == null ) {
				if( isModifying() ) settings.delete();
				throw new UiException( "Removed orphaned tool id=" + id );
			}

			Tool tool = loadTool( settings );
			viewToolMap.computeIfAbsent( view, k -> new HashSet<>() ).add( tool );
			if( isActive( settings ) ) viewActiveTools.put( view, tool );
			tools.put( id, tool );
			return tool;
		} catch( Exception exception ) {
			exception.printStackTrace( System.out );
			errors.add( exception );
			return null;
		}
	}

	ProgramTool loadTool( Settings settings ) throws AssetException, ToolInstantiationException {
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

		tool.setOrder( order );

		return tool;
	}

	private boolean isActive( Settings settings ) {
		return settings.get( UiFactory.ACTIVE, Boolean.class, false );
	}

	private boolean isMaximized( Settings settings ) {
		return settings.get( UiFactory.MAXIMIZED, Boolean.class, false );
	}

	private boolean isViewActive( Settings settings ) {
		return settings.exists( UiWorkareaFactory.VIEW_ACTIVE );
	}

	private boolean isViewDefault( Settings settings ) {
		return settings.exists( UiWorkareaFactory.VIEW_DEFAULT );
	}

	private boolean isViewMaximized( Settings settings ) {
		return settings.exists( UiWorkareaFactory.VIEW_MAXIMIZED );
	}

	private void linkAreasToSpaces() {
		// Sort the areas by order
		List<Workarea> areaList = new ArrayList<>( areas.values() );
		areaList.sort( Comparator.comparing( Workarea::getOrder ) );

		// Link the workareas to the workspaces
		for( Workarea area : areaList ) {
			try {
				Settings settings = getProgram().getSettingsManager().getSettings( ProgramSettings.AREA, area.getUid() );
				Workspace space = spaces.get( settings.get( UiFactory.PARENT_WORKSPACE_ID ) );
				space.addWorkarea( area );

				// Save the active area for later
				if( area.isActive() ) spaceActiveAreas.put( space, area );

				if( isViewActive( settings ) ) areaActiveViews.put( area, views.get( settings.get( UiWorkareaFactory.VIEW_ACTIVE ) ) );
				if( isViewDefault( settings ) ) areaDefaultViews.put( area, views.get( settings.get( UiWorkareaFactory.VIEW_DEFAULT ) ) );
				if( isViewMaximized( settings ) ) areaMaximizedViews.put( area, views.get( settings.get( UiWorkareaFactory.VIEW_MAXIMIZED ) ) );
			} catch( Exception exception ) {
				errors.add( exception );
			}
		}
	}

	void linkEdgesAndViewsToAreas() {
		Map<Workpane, Set<WorkpaneEdge>> areaEdges = new HashMap<>();
		Map<Workpane, Set<WorkpaneView>> areaViews = new HashMap<>();

		// Link the edges
		for( WorkpaneEdge edge : edges.values() ) {
			Settings settings = getProgram().getSettingsManager().getSettings( ProgramSettings.EDGE, edge.getUid() );
			Workarea area = areas.get( settings.get( UiFactory.PARENT_WORKPANE_ID ) );
			try {
				if( linkEdge( area, edge, settings ) ) {
					areaEdges.computeIfAbsent( area, k -> new HashSet<>() ).add( edge );
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
			Workarea area = areas.get( settings.get( UiFactory.PARENT_WORKPANE_ID ) );
			try {
				if( linkView( area, view, settings ) ) {
					areaViews.computeIfAbsent( area, k -> new HashSet<>() ).add( view );
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
		for( Workarea area : areas.values() ) {
			Set<WorkpaneEdge> localAreaEdges = areaEdges.computeIfAbsent( area, k -> new HashSet<>() );
			Set<WorkpaneView> localAreaViews = areaViews.computeIfAbsent( area, k -> new HashSet<>() );
			linkArea( area, localAreaEdges, localAreaViews );
		}
	}

	boolean linkEdge( Workarea area, WorkpaneEdge edge, Settings settings ) {
		Orientation orientation = Objects.requireNonNull( edge.getOrientation() );

		if( orientation == Orientation.VERTICAL ) {
			WorkpaneEdge t = lookupEdge( area, settings.get( "t" ) );
			WorkpaneEdge b = lookupEdge( area, settings.get( "b" ) );
			if( t == null || b == null ) return false;
			edge.setEdge( Side.TOP, t );
			edge.setEdge( Side.BOTTOM, b );
		} else if( orientation == Orientation.HORIZONTAL ) {
			WorkpaneEdge l = lookupEdge( area, settings.get( "l" ) );
			WorkpaneEdge r = lookupEdge( area, settings.get( "r" ) );
			if( l == null || r == null ) return false;
			edge.setEdge( Side.LEFT, l );
			edge.setEdge( Side.RIGHT, r );
		}

		return true;
	}

	boolean linkView( Workarea area, WorkpaneView view, Settings settings ) {
		WorkpaneEdge t = lookupEdge( area, settings.get( "t" ) );
		WorkpaneEdge l = lookupEdge( area, settings.get( "l" ) );
		WorkpaneEdge r = lookupEdge( area, settings.get( "r" ) );
		WorkpaneEdge b = lookupEdge( area, settings.get( "b" ) );

		if( t == null || l == null || r == null || b == null ) return false;

		view.setEdge( Side.TOP, t );
		view.setEdge( Side.LEFT, l );
		view.setEdge( Side.RIGHT, r );
		view.setEdge( Side.BOTTOM, b );

		return true;
	}

	void linkArea( Workarea area, Set<WorkpaneEdge> edges, Set<WorkpaneView> views ) {
		area.restoreNodes( edges, views );
	}

	void linkToolsToViews() {
		for( Map.Entry<WorkpaneView, Set<Tool>> entry : viewToolMap.entrySet() ) {
			WorkpaneView view = entry.getKey();
			Workpane pane = view.getWorkpane();
			if( pane == null ) continue;

			// Sort the tools
			List<Tool> localTools = new ArrayList<>( entry.getValue() );
			localTools.sort( Comparator.comparing( Tool::getOrder ) );

			// Add the tools to the view
			for( Tool tool : localTools ) {
				pane.addTool( tool, view, false );
				log.atDebug().log( "Tool linked: %s: %s", LazyEval.of( tool::getClass ), LazyEval.of( () -> tool.getAsset().getUri() ) );
			}
		}
	}

	void linkSpaces() {
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

	private void copyPaneSettings(Settings settings ) {
		String id = settings.getName();
		Settings rootSettings = getProgram().getSettingsManager().getSettings( ProgramSettings.BASE );
		if( rootSettings.nodeExists( ProgramSettings.PANE ) ) {
			Settings paneSetting = getProgram().getSettingsManager().getSettings( ProgramSettings.PANE );
			if( paneSetting.nodeExists( id ) ) {
				Settings paneSettings = paneSetting.getNode( id );
				settings.set( UiWorkareaFactory.VIEW_ACTIVE, paneSettings.get( UiWorkareaFactory.VIEW_ACTIVE ) );
				settings.set( UiWorkareaFactory.VIEW_DEFAULT, paneSettings.get( UiWorkareaFactory.VIEW_DEFAULT) );
				settings.set( UiWorkareaFactory.VIEW_MAXIMIZED, paneSettings.get( UiWorkareaFactory.VIEW_MAXIMIZED ) );
			}
		}
	}

}
