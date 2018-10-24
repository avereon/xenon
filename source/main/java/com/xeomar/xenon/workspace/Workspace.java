package com.xeomar.xenon.workspace;

import com.xeomar.settings.Settings;
import com.xeomar.settings.SettingsEvent;
import com.xeomar.settings.SettingsListener;
import com.xeomar.util.Configurable;
import com.xeomar.util.LogUtil;
import com.xeomar.xenon.ExecMode;
import com.xeomar.xenon.Program;
import com.xeomar.xenon.ProgramSettings;
import com.xeomar.xenon.UiFactory;
import com.xeomar.xenon.event.WorkareaChangedEvent;
import com.xeomar.xenon.resource.ResourceException;
import com.xeomar.xenon.resource.type.ProgramTaskType;
import com.xeomar.xenon.util.ActionUtil;
import com.xeomar.xenon.util.Colors;
import com.xeomar.xenon.workarea.Workarea;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.stage.Stage;
import org.slf4j.Logger;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The workspace manages the menu bar, tool bar and workareas.
 */
public class Workspace implements Configurable {

	private static final Logger log = LogUtil.get( MethodHandles.lookup().lookupClass() );

	private Program program;

	private Stage stage;

	private Scene scene;

	private boolean active;

	private BorderPane layout;

	private Pane menubarContainer;

	private HBox toolbarContainer;

	private MenuBar menubar;

	private ToolBar toolbar;

	private StatusBar statusBar;

	private MemoryMonitor memoryMonitor;

	private TaskMonitor taskMonitor;

	private Pane workpaneContainer;

	private ComboBox<Workarea> workareaSelector;

	private ObservableList<Workarea> workareas;

	private Workarea activeWorkarea;

	private WorkareaPropertyWatcher activeWorkareaWatcher;

	private Settings settings;

	private Settings backgroundSettings;

	private SettingsListener backgroundSettingsHandler;

	private String id;

	public Workspace( Program program ) {
		this.program = program;

		workareas = FXCollections.observableArrayList();
		activeWorkareaWatcher = new WorkareaPropertyWatcher();
		backgroundSettingsHandler = new BackgroundSettingsHandler();

		// FIXME Should this default setup be defined in config files or something else?

		// MENUBAR
		menubar = new MenuBar();
		// FIXME This does not work if there are two menu bars (like this program uses)
		menubar.setUseSystemMenuBar( true );

		Menu prog = ActionUtil.createMenu( program, "program" );
		prog.getItems().add( ActionUtil.createMenuItem( program, "settings" ) );
		prog.getItems().add( new SeparatorMenuItem() );
		prog.getItems().add( ActionUtil.createMenuItem( program, "exit" ) );

		Menu file = ActionUtil.createMenu( program, "file" );
		file.getItems().add( ActionUtil.createMenuItem( program, "new" ) );
		file.getItems().add( ActionUtil.createMenuItem( program, "open" ) );
		file.getItems().add( ActionUtil.createMenuItem( program, "save" ) );
		file.getItems().add( ActionUtil.createMenuItem( program, "save-as" ) );
		file.getItems().add( ActionUtil.createMenuItem( program, "copy-as" ) );
		file.getItems().add( ActionUtil.createMenuItem( program, "close" ) );

		Menu edit = ActionUtil.createMenu( program, "edit" );
		edit.getItems().add( ActionUtil.createMenuItem( program, "undo" ) );
		edit.getItems().add( ActionUtil.createMenuItem( program, "redo" ) );
		edit.getItems().add( new SeparatorMenuItem() );
		edit.getItems().add( ActionUtil.createMenuItem( program, "cut" ) );
		edit.getItems().add( ActionUtil.createMenuItem( program, "copy" ) );
		edit.getItems().add( ActionUtil.createMenuItem( program, "paste" ) );
		edit.getItems().add( ActionUtil.createMenuItem( program, "delete" ) );
		edit.getItems().add( new SeparatorMenuItem() );
		edit.getItems().add( ActionUtil.createMenuItem( program, "indent" ) );
		edit.getItems().add( ActionUtil.createMenuItem( program, "unindent" ) );

		Menu view = ActionUtil.createMenu( program, "view" );
		view.getItems().add( ActionUtil.createMenuItem( program, "workspace-new" ) );
		view.getItems().add( new SeparatorMenuItem() );
		view.getItems().add( ActionUtil.createMenuItem( program, "statusbar-show" ) );

		Menu help = ActionUtil.createMenu( program, "help" );
		help.getItems().add( ActionUtil.createMenuItem( program, "help-content" ) );
		help.getItems().add( new SeparatorMenuItem() );
		help.getItems().add( ActionUtil.createMenuItem( program, "welcome" ) );
		help.getItems().add( ActionUtil.createMenuItem( program, "task" ) );
		help.getItems().add( new SeparatorMenuItem() );
		help.getItems().add( ActionUtil.createMenuItem( program, "product" ) );
		help.getItems().add( ActionUtil.createMenuItem( program, "update" ) );
		help.getItems().add( ActionUtil.createMenuItem( program, "about" ) );

		Menu dev = ActionUtil.createMenu( program, "development" );
		dev.getItems().add( ActionUtil.createMenuItem( program, "restart" ) );
		dev.getItems().add( ActionUtil.createMenuItem( program, "test-update-found" ) );
		dev.setId( "menu-development" );

		menubar.getMenus().addAll( prog, file, edit, view, help );
		if( program.getExecMode() == ExecMode.DEV ) menubar.getMenus().add( dev );

		// Workarea menu

		Menu workareaMenu = ActionUtil.createMenu( program, "workarea" );
		workareaMenu.getItems().add( ActionUtil.createMenuItem( program, "workarea-new" ) );
		workareaMenu.getItems().add( new SeparatorMenuItem() );
		workareaMenu.getItems().add( ActionUtil.createMenuItem( program, "workarea-rename" ) );
		workareaMenu.getItems().add( new SeparatorMenuItem() );
		workareaMenu.getItems().add( ActionUtil.createMenuItem( program, "workarea-close" ) );

		MenuBar workareaMenuBar = new MenuBar();
		workareaMenuBar.getMenus().add( workareaMenu );
		workareaMenuBar.setBackground( Background.EMPTY );
		workareaMenuBar.setPadding( Insets.EMPTY );
		workareaMenuBar.setBorder( Border.EMPTY );

		// Workarea selector
		workareaSelector = new ComboBox<>();
		workareaSelector.setItems( workareas );
		workareaSelector.setButtonCell( new WorkareaPropertyCell() );
		workareaSelector.valueProperty().addListener( ( value, oldValue, newValue ) -> setActiveWorkarea( newValue ) );

		// TOOLBAR

		toolbar = new ToolBar();
		toolbar.getItems().add( ActionUtil.createToolBarButton( program, "new" ) );
		toolbar.getItems().add( ActionUtil.createToolBarButton( program, "open" ) );
		toolbar.getItems().add( ActionUtil.createToolBarButton( program, "save" ) );
		toolbar.getItems().add( new Separator() );
		toolbar.getItems().add( ActionUtil.createToolBarButton( program, "undo" ) );
		toolbar.getItems().add( ActionUtil.createToolBarButton( program, "redo" ) );
		toolbar.getItems().add( new Separator() );
		toolbar.getItems().add( ActionUtil.createToolBarButton( program, "cut" ) );
		toolbar.getItems().add( ActionUtil.createToolBarButton( program, "copy" ) );
		toolbar.getItems().add( ActionUtil.createToolBarButton( program, "paste" ) );

		toolbar.getItems().add( ActionUtil.createSpring() );

		toolbar.getItems().add( workareaMenuBar );
		toolbar.getItems().add( workareaSelector );

		// STATUS BAR
		statusBar = new StatusBar();

		// Task Monitor
		taskMonitor = new TaskMonitor( program.getTaskManager() );

		// If the task monitor is clicked then open the task tool
		taskMonitor.setOnMouseClicked( ( event ) -> {
			try {
				program.getResourceManager().open( ProgramTaskType.URI );
			} catch( ResourceException exception ) {
				log.error( "Error opening task tool", exception );
			}
		} );

		// Memory Monitor
		memoryMonitor = new MemoryMonitor();

		// If the memory monitor is clicked then call the garbage collector
		memoryMonitor.setOnMouseClicked( ( event ) -> Runtime.getRuntime().gc() );

		HBox leftStatusBarItems = new HBox();
		leftStatusBarItems.getStyleClass().addAll( "box" );

		HBox rightStatusBarItems = new HBox();
		rightStatusBarItems.getStyleClass().addAll( "box" );

		leftStatusBarItems.getChildren().addAll( statusBar );
		rightStatusBarItems.getChildren().addAll( taskMonitor, memoryMonitor );

		BorderPane statusBarContainer = new BorderPane();
		statusBarContainer.setLeft( leftStatusBarItems );
		statusBarContainer.setRight( rightStatusBarItems );
		statusBarContainer.getStyleClass().add( "status-bar" );

		// Workarea Container
		workpaneContainer = new StackPane();
		workpaneContainer.getStyleClass().add( "workspace" );

		VBox bars = new VBox();
		bars.getChildren().addAll( menubar, toolbar );

		layout = new BorderPane();
		layout.setTop( bars );
		layout.setCenter( workpaneContainer );
		layout.setBottom( statusBarContainer );

		// Create the stage
		stage = new Stage();
		Image image;
		stage.getIcons().addAll( program.getIconLibrary().getStageIcons( "program" ) );
		stage.setOnCloseRequest( event -> {
			event.consume();
			program.getWorkspaceManager().requestCloseWorkspace( this );
		} );
	}

	public Stage getStage() {
		return stage;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive( boolean active ) {
		this.active = active;
		settings.set( "active", active );
	}

	public Set<Workarea> getWorkareas() {
		return new HashSet<>( workareas );
	}

	public void addWorkarea( Workarea workarea ) {
		Workspace oldWorkspace = workarea.getWorkspace();
		if( oldWorkspace != null ) oldWorkspace.removeWorkarea( workarea );
		workareas.add( workarea );
		workarea.setWorkspace( this );
	}

	public void removeWorkarea( Workarea workarea ) {
		// If there is only one workarea, don't close it
		if( workareas.size() == 1 ) return;

		// Handle the situation where the workarea area is active
		if( workarea.isActive() ) setActiveWorkarea( determineNextActiveWorkarea() );

		workareas.remove( workarea );
		workarea.setWorkspace( null );
	}

	public Workarea getActiveWorkarea() {
		return activeWorkarea;
	}

	public void setActiveWorkarea( Workarea workarea ) {
		if( activeWorkarea == workarea ) return;

		// If the workarea is not already added, add it
		if( !workareas.contains( workarea ) ) addWorkarea( workarea );

		// Disconnect the old active workarea area
		if( activeWorkarea != null ) {
			activeWorkarea.removePropertyChangeListener( activeWorkareaWatcher );
			activeWorkarea.setActive( false );
			workpaneContainer.getChildren().remove( activeWorkarea.getWorkpane() );
		}

		// Swap the workarea area on the stage
		activeWorkarea = workarea;

		// Connect the new active workarea area
		if( activeWorkarea != null ) {
			workpaneContainer.getChildren().add( activeWorkarea.getWorkpane() );
			activeWorkarea.setActive( true );
			setStageTitle( activeWorkarea.getName() );
			workareaSelector.getSelectionModel().select( activeWorkarea );
			activeWorkarea.addPropertyChangeListener( activeWorkareaWatcher );

			// TODO Set the menu bar
			// TODO Set the tool bar
			// TODO Set the workpane
		}

		// Send a program event when active area changes
		program.fireEvent( new WorkareaChangedEvent( this, activeWorkarea ) );
	}

	public StatusBar getStatusBar() {
		return statusBar;
	}

	@Override
	public void setSettings( Settings settings ) {
		if( this.settings != null ) return;

		// The incoming settings are the workspace settings

		this.settings = settings;
		id = settings.get( "id" );

		Double x = settings.get( "x", Double.class, null );
		Double y = settings.get( "y", Double.class, null );
		Double w = settings.get( "w", Double.class, UiFactory.DEFAULT_WIDTH );
		Double h = settings.get( "h", Double.class, UiFactory.DEFAULT_HEIGHT );

		// Due to differences in how FX handles stage size (width and height) on
		// different operating systems, the width and height from the scene, not the
		// stage, are used. This includes the listeners for the width and height
		// properties below.
		stage.setScene( scene = new Scene( layout, w, h ) );
		scene.getStylesheets().add( Program.STYLESHEET );
		stage.sizeToScene();

		// Position the stage if x and y are specified
		// If not specified the stage is centered on the screen
		if( x != null ) stage.setX( x );
		if( y != null ) stage.setY( y );

		// On Linux, setWidth() and setHeight() incorrectly do not take the stage
		// window decorations into account. The way to deal with this is to watch
		// the scene size and set the scene size on creation.
		// Do not use the following.
		// if( w != null ) stage.setWidth( w );
		// if( h != null ) stage.setHeight( h );

		stage.setMaximized( settings.get( "maximized", Boolean.class, false ) );
		setActive( settings.get( "active", Boolean.class, false ) );

		// Add the property listeners
		stage.maximizedProperty().addListener( ( observableValue, oldValue, newValue ) -> {
			if( stage.isShowing() ) settings.set( "maximized", newValue );
		} );
		stage.xProperty().addListener( ( observableValue, oldValue, newValue ) -> {
			if( !stage.isMaximized() ) settings.set( "x", newValue );
		} );
		stage.yProperty().addListener( ( observableValue, oldValue, newValue ) -> {
			if( !stage.isMaximized() ) settings.set( "y", newValue );
		} );
		scene.widthProperty().addListener( ( observableValue, oldValue, newValue ) -> {
			if( !stage.isMaximized() ) settings.set( "w", newValue );
		} );
		scene.heightProperty().addListener( ( observableValue, oldValue, newValue ) -> {
			if( !stage.isMaximized() ) settings.set( "h", newValue );
		} );

		backgroundSettings = program.getSettingsManager().getSettings( ProgramSettings.PROGRAM );
		backgroundSettings.removeSettingsListener( backgroundSettingsHandler );
		updateBackgroundFromSettings( backgroundSettings );
		backgroundSettings.addSettingsListener( backgroundSettingsHandler );
	}

	private void updateBackgroundFromSettings( Settings settings ) {
		boolean backDirection = "0".equals( settings.get( "workspace-scenery-back-direction", "0" ) );
		Color backColor1 = Colors.web( settings.get( "workspace-scenery-back-color1", "#80a0c0ff" ) );
		Color backColor2 = Colors.web( settings.get( "workspace-scenery-back-color2", "#ffffffff" ) );
		LinearGradient back;
		Stop[] stops = new Stop[]{ new Stop( 0, backColor1 ), new Stop( 1, backColor2 ) };
		if( backDirection ) {
			back = new LinearGradient( 0, 0, 1, 0, true, CycleMethod.NO_CYCLE, stops );
		} else {
			back = new LinearGradient( 0, 0, 0, 1, true, CycleMethod.NO_CYCLE, stops );
		}

		// NEXT Add image fill
		boolean imageEnabled = Boolean.parseBoolean( settings.get( "workspace-scenery-image-enabled", "false" ) );
		ImagePattern image = null;

		boolean tintEnabled = Boolean.parseBoolean( settings.get( "workspace-scenery-tint-enabled", "false" ) );
		boolean tintDirection = "0".equals( settings.get( "workspace-scenery-tint-direction", "0" ) );
		Color tintColor1 = Colors.web( settings.get( "workspace-scenery-tint-color1", "#ffffff80" ) );
		Color tintColor2 = Colors.web( settings.get( "workspace-scenery-tint-color2", "#ffffff80" ) );
		LinearGradient tint;
		Stop[] tintStops = new Stop[]{ new Stop( 0, tintColor1 ), new Stop( 1, tintColor2 ) };
		if( tintDirection ) {
			tint = new LinearGradient( 0, 0, 1, 0, true, CycleMethod.NO_CYCLE, tintStops );
		} else {
			tint = new LinearGradient( 0, 0, 0, 1, true, CycleMethod.NO_CYCLE, tintStops );
		}

		List<BackgroundFill> fills = new ArrayList<>();
		fills.add( new BackgroundFill( back, CornerRadii.EMPTY, Insets.EMPTY ) );
		if( imageEnabled ) fills.add( new BackgroundFill( image, CornerRadii.EMPTY, Insets.EMPTY ) );
		if( tintEnabled ) fills.add( new BackgroundFill( tint, CornerRadii.EMPTY, Insets.EMPTY ) );

		workpaneContainer.setBackground( new Background( fills, null ) );
	}

	@Override
	public Settings getSettings() {
		return settings;
	}

	public void close() {
		memoryMonitor.close();
		taskMonitor.close();
		getStage().close();
	}

	private void setStageTitle( String name ) {
		stage.setTitle( name + " - " + program.getCard().getName() );
	}

	private Workarea determineNextActiveWorkarea() {
		int index = workareas.indexOf( getActiveWorkarea() );
		return workareas.get( index == 0 ? 1 : index - 1 );
	}

	private class BackgroundSettingsHandler implements SettingsListener {

		@Override
		public void handleEvent( SettingsEvent event ) {
			if( event.getType() != SettingsEvent.Type.CHANGED ) return;

			updateBackgroundFromSettings( backgroundSettings );
		}

	}

	private class WorkareaPropertyWatcher implements PropertyChangeListener {

		@Override
		public void propertyChange( PropertyChangeEvent event ) {
			switch( event.getPropertyName() ) {
				case "name": {
					//workareaSelector.setValue( getActiveWorkarea() );
					setStageTitle( event.getNewValue().toString() );
					break;
				}
			}
		}

	}

	public static class WorkareaPropertyCell extends ListCell<Workarea> {

		@Override
		protected void updateItem( Workarea item, boolean empty ) {
			super.updateItem( item, empty );
			textProperty().unbind();
			if( item != null && !empty ) textProperty().bind( item.getNameValue() );
		}

	}

}
