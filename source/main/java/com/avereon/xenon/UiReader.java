package com.avereon.xenon;

import com.avereon.log.LazyEval;
import com.avereon.log.LogLevel;
import com.avereon.product.Rb;
import com.avereon.settings.Settings;
import com.avereon.util.IdGenerator;
import com.avereon.xenon.asset.Resource;
import com.avereon.xenon.asset.ResourceType;
import com.avereon.xenon.asset.OpenAssetRequest;
import com.avereon.xenon.asset.exception.ResourceException;
import com.avereon.xenon.asset.exception.ResourceNotFoundException;
import com.avereon.xenon.asset.exception.AssetTypeNotFoundException;
import com.avereon.xenon.asset.type.ProgramWelcomeType;
import com.avereon.xenon.notice.Notice;
import com.avereon.xenon.scheme.XenonScheme;
import com.avereon.xenon.workpane.*;
import com.avereon.xenon.workspace.Workarea;
import com.avereon.xenon.workspace.Workspace;
import com.avereon.zerra.javafx.Fx;
import javafx.geometry.Orientation;
import javafx.geometry.Side;
import lombok.CustomLog;
import lombok.Getter;

import java.net.URI;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@CustomLog
class UiReader {

	private static final LogLevel logLevel = LogLevel.DEBUG;

	@Getter
	private final Xenon program;

	private final UiWorkspaceFactory spaceFactory;

	private final UiWorkareaFactory areaFactory;

	private final Map<String, Workspace> spaces = new HashMap<>();

	private final Map<String, Workarea> areas = new HashMap<>();

	private final Map<String, WorkpaneEdge> edges = new HashMap<>();

	private final Map<String, WorkpaneView> views = new HashMap<>();

	private final Map<String, Tool> tools = new HashMap<>();

	private final Set<Resource> resources = new HashSet<>();

	private Workspace activeSpace;

	private final Set<Workspace> maximizedSpaces = new HashSet<>();

	private final Map<Workspace, Workarea> spaceActiveAreas = new HashMap<>();

	private final Map<Workarea, WorkpaneView> areaActiveViews = new HashMap<>();

	private final Map<Workarea, WorkpaneView> areaDefaultViews = new HashMap<>();

	private final Map<Workarea, WorkpaneView> areaMaximizedViews = new HashMap<>();

	private final Map<WorkpaneView, Tool> viewActiveTools = new HashMap<>();

	private final List<Exception> errors = new ArrayList<>();

	private final Lock spaceRestoreLock = new ReentrantLock();

	private final Condition spacesRestoredCondition = spaceRestoreLock.newCondition();

	private boolean spacesRestored;

	private Future<Collection<Resource>> assetLoadFuture;

	public UiReader( Xenon program ) {
		this.program = program;
		this.spaceFactory = new UiWorkspaceFactory( program );
		this.areaFactory = new UiWorkareaFactory( program );
	}

	public void loadWorkspaces() {
		Fx.run( this::doWorkspaceLoad );
	}

	public void awaitLoadWorkspaces( long duration, TimeUnit unit ) throws InterruptedException, TimeoutException {
		doAwaitForSpaceLoad( duration, unit );
	}

	public void loadAssets() {
		doStartAssetLoading();
	}

	public void awaitLoadAssets( long duration, TimeUnit unit ) throws InterruptedException, TimeoutException {
		doAwaitForAssetLoad( duration, unit );
	}

	private void doWorkspaceLoad() {
		Fx.affirmOnFxThread();
		spaceRestoreLock.lock();

		try {
			if( getWorkspaceCount() == 0 ) {
				createDefaultWorkspace();
				log.at( logLevel ).log( "Created default workspace" );
			} else {
				restoreWorkspaces();
				log.at( logLevel ).log( "Restored known workspaces: count=%s", spaces.size() );
			}

			// Ensure there is an active workarea
			Workspace space = getProgram().getWorkspaceManager().getActiveWorkspace();
			//			if( space != null && !space.getWorkareas().isEmpty() && getProgram().getWorkspaceManager().getActiveWorkpane() == null ) {
			//				space.setActiveWorkarea( space.getWorkareas().iterator().next() );
			//			}

			// Check the restored state
			if( getProgram().getWorkspaceManager().getWorkspaces().isEmpty() ) log.atError().log( "No workspaces restored" );
			if( space == null ) log.atError().log( "Missing active workspace" );
			if( space != null && space.getWorkareas().isEmpty() ) log.atError().log( "No workareas restored" );
			if( space != null && space.getActiveWorkarea() == null ) log.atError().log( "Missing active workarea" );
			for( Workarea area : areas.values() ) {
				if( area.getActiveView() == null ) log.atError().log( "Missing active view for workarea: %s", area );
				if( area.getDefaultView() == null ) log.atError().log( "Missing default view for workarea: %s", area );
			}

			// If there are exceptions, restoring the UI notify the user
			if( !errors.isEmpty() ) notifyUserOfErrors( errors );
		} finally {
			spacesRestored = true;
			spacesRestoredCondition.signalAll();
			spaceRestoreLock.unlock();
		}
	}

	private int getWorkspaceCount() {
		return getProgram().getSettingsManager().getSettings( ProgramSettings.WORKSPACE ).getNodes().size();
	}

	private void createDefaultWorkspace() {
		// Create the default workarea
		Workarea workarea = areaFactory.create();
		workarea.setUid( IdGenerator.getId() );
		workarea.setIcon( "workarea" );
		workarea.setName( Rb.text( RbKey.WORKAREA, "workarea-new-title", "New Workarea" ) );
		Settings areaSettings = program.getSettingsManager().getSettings( ProgramSettings.AREA, workarea.getUid() );
		areaFactory.applyWorkareaSettings( workarea, areaSettings );
		areaFactory.linkWorkareaSettingsListeners( workarea, areaSettings );

		// Create the default workspace
		Workspace space = new Workspace( program );
		space.setUid( IdGenerator.getId() );
		space.initializeScene( UiWorkspaceFactory.DEFAULT_WIDTH, UiWorkspaceFactory.DEFAULT_HEIGHT );

		Settings spaceSettings = program.getSettingsManager().getSettings( ProgramSettings.WORKSPACE, space.getUid() );
		spaceFactory.applyWorkspaceSettings( space, spaceSettings );
		spaceFactory.linkWorkspaceSettingsListeners( space, spaceSettings );

		String themeId = getProgram().getWorkspaceManager().getThemeId();
		space.setTheme( getProgram().getThemeManager().getMetadata( themeId ).getUrl() );

		// Add the workarea to the workspace
		space.addWorkarea( workarea );

		// Activate the new workarea and workspace
		space.setActiveWorkarea( workarea );
		getProgram().getWorkspaceManager().setActiveWorkspace( space );

		// Add the welcome tool to the default workarea
		if( !getProgram().getProgramParameters().isSet( XenonTestFlag.EMPTY_WORKSPACE ) ) getProgram().getResourceManager().openAsset( ProgramWelcomeType.URI );

		spaces.put( space.getUid(), space );
	}

	private void restoreWorkspaces() {
		getUiSettings( ProgramSettings.WORKSPACE ).forEach( this::loadSpaceForLinking );
		getUiSettings( ProgramSettings.AREA ).forEach( this::loadAreaForLinking );
		getUiSettings( ProgramSettings.VIEW ).forEach( this::loadViewForLinking );
		getUiSettings( ProgramSettings.EDGE ).forEach( this::loadEdgeForLinking );
		getUiSettings( ProgramSettings.TOOL ).forEach( this::loadToolForLinking );

		// Reassemble the UI
		linkSpaces();
		linkAreasToSpaces();
		linkEdgesAndViewsToAreas();
		linkToolsToViews();

		// Now that everything is linked, time to restore the flags. This should be
		// done before the listeners are added to avoid unintended modifications.
		restoreFlags();

		// Last, but not least, register the listeners. This should be done last to
		// avoid unintended modifications while the UI is being restored.
		registerListeners();
	}

	private void restoreFlags() {
		/* TOOLS */
		// For each view there is an active tool
		for( WorkpaneView view : viewActiveTools.keySet() ) {
			Tool tool = viewActiveTools.get( view );
			if( tool instanceof ProgramTool programTool ) {
				programTool.setActiveWhenReady();
			} else {
				view.setActiveTool( tool );
			}
		}

		/* WORKAREAS */
		// For each area there is an active view
		for( Workarea area : areaActiveViews.keySet() ) {
			WorkpaneView view = areaActiveViews.get( area );
			area.setActiveView( view );
		}
		// For each area there is a default view
		for( Workarea area : areaDefaultViews.keySet() ) {
			WorkpaneView view = areaDefaultViews.get( area );
			area.setDefaultView( view );
		}
		// For each area there might be a maximized view
		for( Workarea area : areaMaximizedViews.keySet() ) {
			WorkpaneView view = areaMaximizedViews.get( area );
			area.setMaximizedView( view );
		}

		/* WORKSPACES */
		// For each space there is an active area
		for( Workspace space : spaceActiveAreas.keySet() ) {
			Workarea area = spaceActiveAreas.get( space );
			space.setActiveWorkarea( area );
		}
		// For each space there might be a maximized area
		for( Workspace space : maximizedSpaces ) {
			space.setMaximized( true );
		}
		// Set the active space
		if( activeSpace != null ) {
			program.getWorkspaceManager().setActiveWorkspace( activeSpace );
		}
	}

	private void registerListeners() {
		// Register the workarea listeners
		for( Workarea area : areas.values() ) {
			Settings settings = program.getSettingsManager().getSettings( ProgramSettings.AREA, area.getUid() );
			areaFactory.linkWorkareaSettingsListeners( area, settings );
		}

		// Register the workspace listeners
		for( Workspace space : spaces.values() ) {
			Settings settings = program.getSettingsManager().getSettings( ProgramSettings.WORKSPACE, space.getUid() );
			spaceFactory.linkWorkspaceSettingsListeners( space, settings );
		}
	}

	private void doStartAssetLoading() {
		try {
			assetLoadFuture = getProgram().getResourceManager().loadAssets( resources );
		} catch( Exception exception ) {
			log.atWarn( exception ).log();
		}
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
		spaceFactory.applyWorkspaceSettings( workspace, settings );
		if( isActive( settings ) ) activeSpace = workspace;
		if( isMaximized( settings ) ) maximizedSpaces.add( workspace );
		return workspace;
	}

	Workarea loadAreaForLinking( Settings settings ) {
		try {
			copyPaneSettings( settings );

			String id = settings.getName();
			Workspace space = spaces.get( settings.get( UiFactory.PARENT_SPACE_ID ) );
			// TODO Remove in 1.9-SNAPSHOT
			if( space == null ) space = spaces.get( settings.get( UiFactory.PARENT_WORKSPACE_ID ) );

			// If the workspace is not found, then the workarea is orphaned...delete the settings
			if( space == null ) {
				settings.delete();
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
		return areaFactory.applyWorkareaSettings( area, settings );
	}

	WorkpaneView loadViewForLinking( Settings settings ) {
		try {
			String id = settings.getName();
			Workarea area = areas.get( settings.get( UiFactory.PARENT_AREA_ID ) );
			// TODO Remove in 1.9-SNAPSHOT
			if( area == null ) area = areas.get( settings.get( UiFactory.PARENT_WORKPANE_ID ) );

			// If the workpane is not found, then the view is orphaned...delete the settings
			if( area == null ) {
				settings.delete();
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
			Workarea area = areas.get( settings.get( UiFactory.PARENT_AREA_ID ) );
			// TODO Remove in 1.9-SNAPSHOT
			if( area == null ) area = areas.get( settings.get( UiFactory.PARENT_WORKPANE_ID ) );

			// If the workpane is not found, then the edge is orphaned...delete the settings
			if( area == null ) {
				settings.delete();
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
			URI uri = settings.get( Resource.SETTINGS_URI_KEY, URI.class );
			WorkpaneView view = views.get( settings.get( UiFactory.PARENT_VIEW_ID ) );
			// TODO Remove in 1.9-SNAPSHOT
			if( view == null ) view = views.get( settings.get( UiFactory.PARENT_WORKPANEVIEW_ID ) );

			// If the view is not found, then the tool is orphaned...delete the settings
			if( view == null || uri == null ) {
				settings.delete();
				throw new UiException( "Removed orphaned tool id=" + id );
			}

			Tool tool = loadTool( settings );
			if( isActive( settings ) ) viewActiveTools.put( view, tool );
			resources.add( tool.getResource() );
			tools.put( id, tool );
			return tool;
		} catch( Exception exception ) {
			errors.add( exception );
			return null;
		}
	}

	ProgramTool loadTool( Settings settings ) throws ResourceException, ToolInstantiationException {
		String toolClassName = settings.get( Tool.SETTINGS_TYPE_KEY );
		URI uri = settings.get( Resource.SETTINGS_URI_KEY, URI.class );
		String assetTypeKey = settings.get( Resource.SETTINGS_TYPE_KEY );
		Integer order = settings.get( Tool.ORDER, Integer.class, -1 );

		if( "program:/guide".equals( assetTypeKey ) ) assetTypeKey = XenonScheme.ID + ":/guide";

		// Create the asset
		Resource resource;
		ResourceType resourceType = getProgram().getResourceManager().getAssetType( assetTypeKey );
		if( resourceType == null ) throw new AssetTypeNotFoundException( assetTypeKey );
		try {
			resource = getProgram().getResourceManager().createAsset( resourceType, uri );
		} catch( ResourceException exception ) {
			throw new ResourceNotFoundException( new Resource( resourceType, uri ), exception );
		}

		// Create the open asset request
		OpenAssetRequest openAssetRequest = new OpenAssetRequest();
		openAssetRequest.setToolId( settings.getName() );
		openAssetRequest.setResource( resource );
		openAssetRequest.setToolClassName( toolClassName );

		// Restore the tool
		ProgramTool tool = getProgram().getToolManager().restoreTool( openAssetRequest );
		if( tool == null ) {
			settings.delete();
			throw new ToolInstantiationException( settings.getName(), toolClassName );
		}

		tool.setOrder( order );

		return tool;
	}

	void linkAreasToSpaces() {
		// Sort the areas by order
		List<Workarea> areaList = new ArrayList<>( areas.values() );
		areaList.sort( Comparator.comparing( Workarea::getOrder ) );

		// Link the workareas to the workspaces
		for( Workarea area : areaList ) {
			try {
				Settings settings = getProgram().getSettingsManager().getSettings( ProgramSettings.AREA, area.getUid() );
				Workspace space = spaces.get( settings.get( UiFactory.PARENT_SPACE_ID ) );
				// TODO Remove in 1.9-SNAPSHOT
				if( space == null ) space = spaces.get( settings.get( UiFactory.PARENT_WORKSPACE_ID ) );
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
			Workarea area = areas.get( settings.get( UiFactory.PARENT_AREA_ID ) );
			// TODO Remove in 1.9-SNAPSHOT
			if( area == null ) area = areas.get( settings.get( UiFactory.PARENT_WORKPANE_ID ) );
			try {
				if( linkEdge( area, edge, settings ) ) {
					areaEdges.computeIfAbsent( area, k -> new HashSet<>() ).add( edge );
				} else {
					log.atDebug().log( "Removing invalid workpane edge settings: %s", LazyEval.of( settings::getName ) );
					settings.delete();
				}
			} catch( Exception exception ) {
				log.atWarn( exception ).log( "Error linking edge: %s", LazyEval.of( edge::getUid ) );
			}
		}

		// Link the views
		for( WorkpaneView view : views.values() ) {
			Settings settings = getProgram().getSettingsManager().getSettings( ProgramSettings.VIEW, view.getUid() );
			Workarea area = areas.get( settings.get( UiFactory.PARENT_AREA_ID ) );
			// TODO Remove in 1.9-SNAPSHOT
			if( area == null ) area = areas.get( settings.get( UiFactory.PARENT_WORKPANE_ID ) );
			try {
				if( linkView( area, view, settings ) ) {
					areaViews.computeIfAbsent( area, k -> new HashSet<>() ).add( view );
				} else {
					log.atDebug().log( "Removing invalid workpane edge settings: %s", LazyEval.of( settings::getName ) );
					settings.delete();
				}
			} catch( Exception exception ) {
				log.atWarn( exception ).log( "Error linking view: %s", LazyEval.of( view::getUid ), exception );
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
			edge.setEdge( Side.TOP, lookupEdge( area, settings.get( "t", "t" ) ) );
			edge.setEdge( Side.BOTTOM, lookupEdge( area, settings.get( "b", "b" ) ) );
		} else if( orientation == Orientation.HORIZONTAL ) {
			edge.setEdge( Side.LEFT, lookupEdge( area, settings.get( "l", "l" ) ) );
			edge.setEdge( Side.RIGHT, lookupEdge( area, settings.get( "r", "r" ) ) );
		}

		return true;
	}

	boolean linkView( Workarea area, WorkpaneView view, Settings settings ) {
		view.setEdge( Side.TOP, lookupEdge( area, settings.get( "t", "t" ) ) );
		view.setEdge( Side.LEFT, lookupEdge( area, settings.get( "l", "l" ) ) );
		view.setEdge( Side.RIGHT, lookupEdge( area, settings.get( "r", "r" ) ) );
		view.setEdge( Side.BOTTOM, lookupEdge( area, settings.get( "b", "b" ) ) );
		return true;
	}

	void linkArea( Workarea area, Set<WorkpaneEdge> edges, Set<WorkpaneView> views ) {
		area.restoreNodes( edges, views );
	}

	void linkToolsToViews() {
		try {
			// Map out all the tools to their respective views
			Map<WorkpaneView, Set<Tool>> viewToolMap = new HashMap<>();
			for( Tool tool : tools.values() ) {
				Settings settings = getProgram().getSettingsManager().getSettings( ProgramSettings.TOOL, tool.getUid() );
				WorkpaneView view = views.get( settings.get( UiFactory.PARENT_VIEW_ID ) );
				// TODO Remove in 1.9-SNAPSHOT
				if( view == null ) view = views.get( settings.get( UiFactory.PARENT_WORKPANEVIEW_ID ) );
				viewToolMap.computeIfAbsent( view, k -> new HashSet<>() ).add( tool );
			}

			// Now go through the views and link the tools
			for( WorkpaneView view : viewToolMap.keySet() ) {
				Workarea area = (Workarea)view.getWorkpane();

				// Get the tools for the view and order them
				List<Tool> toolList = new ArrayList<>( viewToolMap.get( view ) );
				toolList.sort( Comparator.comparing( Tool::getOrder ) );

				toolList.forEach( tool -> {
					try {
						area.addTool( tool, view, false );
						log.atDebug().log( "Tool linked: %s: %s", LazyEval.of( tool::getClass ), LazyEval.of( () -> tool.getResource().getUri() ) );
					} catch( Exception exception ) {
						errors.add( exception );
					}
				} );
			}
		} catch( Exception exception ) {
			errors.add( exception );
		}
	}

	void linkSpaces() {
		List<Workspace> spacesList = new ArrayList<>( spaces.values() );
		spacesList.sort( Comparator.comparing( Workspace::getOrder ) );

		for( Workspace workspace : spacesList ) {
			getProgram().getWorkspaceManager().addWorkspace( workspace );
		}
	}

	private List<String> getUiSettingsIds( String path ) {
		return getProgram().getSettingsManager().getSettings( path ).getNodes();
	}

	private List<Settings> getUiSettings( String path ) {
		return getUiSettingsIds( path ).stream().map( id -> getProgram().getSettingsManager().getSettings( path, id ) ).toList();
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

	private WorkpaneEdge lookupEdge( Workarea area, String id ) {
		if( area == null ) throw new NullPointerException( "Workarea cannot be null" );
		if( id == null ) throw new NullPointerException( "Edge id cannot be null" );

		WorkpaneEdge edge = edges.get( id );
		if( edge == null ) edge = area.getWallEdge( id.charAt( 0 ) );

		return edge;
	}

	private void doAwaitForSpaceLoad( long duration, TimeUnit unit ) throws InterruptedException, TimeoutException {
		spaceRestoreLock.lock();
		try {
			while( !spacesRestored ) {
				if( !spacesRestoredCondition.await( duration, unit ) ) throw new TimeoutException( "Timeout waiting for workspace restore" );
			}
		} finally {
			spaceRestoreLock.unlock();
		}
	}

	private void doAwaitForAssetLoad( long duration, TimeUnit unit ) throws InterruptedException, TimeoutException {
		try {
			assetLoadFuture.get( duration, unit );
		} catch( ExecutionException exception ) {
			log.atWarn( exception ).log();
		}
	}

	private void notifyUserOfErrors( List<Exception> exceptions ) {
		Set<String> messages = new HashSet<>();
		for( Exception exception : exceptions ) {
			log.atWarn( exception ).log();

			if( exception instanceof ToolInstantiationException toolException ) {
				messages.add( Rb.text( RbKey.PROGRAM, "tool-missing", toolException.getToolClass() ) );
			} else if( exception instanceof ResourceNotFoundException assetException ) {
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

	/**
	 * Copy the workpane settings to the workarea settings.
	 *
	 * @param settings The workarea settings.
	 * @deprecated Remove in 1.8
	 */
	// TODO Remove in 1.9-SNAPSHOT
	@Deprecated( since = "1.7", forRemoval = true )
	private void copyPaneSettings( Settings settings ) {
		Settings rootSettings = getProgram().getSettingsManager().getSettings( ProgramSettings.BASE );
		if( rootSettings.nodeExists( ProgramSettings.PANE ) ) {
			Settings paneSetting = getProgram().getSettingsManager().getSettings( ProgramSettings.PANE );
			String id = settings.getName();
			if( paneSetting.nodeExists( id ) ) {
				Settings paneSettings = paneSetting.getNode( id );
				settings.set( UiWorkareaFactory.VIEW_ACTIVE, paneSettings.get( UiWorkareaFactory.VIEW_ACTIVE ) );
				settings.set( UiWorkareaFactory.VIEW_DEFAULT, paneSettings.get( UiWorkareaFactory.VIEW_DEFAULT ) );
				settings.set( UiWorkareaFactory.VIEW_MAXIMIZED, paneSettings.get( UiWorkareaFactory.VIEW_MAXIMIZED ) );
				paneSettings.delete();
			}
		}
	}

}
