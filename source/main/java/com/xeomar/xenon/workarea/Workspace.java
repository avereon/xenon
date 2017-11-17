package com.xeomar.xenon.workarea;

import com.xeomar.settings.Settings;
import com.xeomar.util.Configurable;
import com.xeomar.xenon.Actions;
import com.xeomar.xenon.ExecMode;
import com.xeomar.xenon.Program;
import com.xeomar.xenon.UiManager;
import com.xeomar.xenon.event.WorkareaChangedEvent;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashSet;
import java.util.Set;

/**
 * The workspace manages the menu bar, tool bar and workareas.
 */
public class Workspace implements Configurable {

	private static final Logger log = LoggerFactory.getLogger( Workspace.class );

	private Program program;

	private Stage stage;

	private Scene scene;

	private boolean active;

	private BorderPane layout;

	private Pane menubarContainer;

	private HBox toolbarContainer;

	private MenuBar menubar;

	private ToolBar toolbar;

	private Pane workpaneContainer;

	private ComboBox<Workarea> workareaSelector;

	private ObservableList<Workarea> workareas;

	private Workarea activeWorkarea;

	private WorkareaPropertyWatcher activeWorkareaWatcher;

	private Settings settings;

	private String id;

	public Workspace( Program program ) {
		this.program = program;

		workareas = FXCollections.observableArrayList();
		activeWorkareaWatcher = new WorkareaPropertyWatcher();

		// FIXME Should this default setup be defined in config files or something else?

		// MENUBAR
		menubar = new MenuBar();

		Menu file = Actions.createMenu( program, "file" );
		file.getItems().add( Actions.createMenuItem( program, "new" ) );
		file.getItems().add( Actions.createMenuItem( program, "open" ) );
		file.getItems().add( Actions.createMenuItem( program, "save" ) );
		file.getItems().add( Actions.createMenuItem( program, "save-as" ) );
		file.getItems().add( Actions.createMenuItem( program, "copy-as" ) );
		file.getItems().add( Actions.createMenuItem( program, "close" ) );
		file.getItems().add( new SeparatorMenuItem() );
		file.getItems().add( Actions.createMenuItem( program, "exit" ) );

		Menu edit = Actions.createMenu( program, "edit" );
		edit.getItems().add( Actions.createMenuItem( program, "undo" ) );
		edit.getItems().add( Actions.createMenuItem( program, "redo" ) );
		edit.getItems().add( new SeparatorMenuItem() );
		edit.getItems().add( Actions.createMenuItem( program, "cut" ) );
		edit.getItems().add( Actions.createMenuItem( program, "copy" ) );
		edit.getItems().add( Actions.createMenuItem( program, "paste" ) );
		edit.getItems().add( Actions.createMenuItem( program, "delete" ) );
		edit.getItems().add( new SeparatorMenuItem() );
		edit.getItems().add( Actions.createMenuItem( program, "indent" ) );
		edit.getItems().add( Actions.createMenuItem( program, "unindent" ) );
		edit.getItems().add( new SeparatorMenuItem() );
		edit.getItems().add( Actions.createMenuItem( program, "settings" ) );

		Menu view = Actions.createMenu( program, "view" );
		view.getItems().add( Actions.createMenuItem( program, "workspace-new" ) );
		view.getItems().add( new SeparatorMenuItem() );
		view.getItems().add( Actions.createMenuItem( program, "statusbar-show" ) );

		Menu help = Actions.createMenu( program, "help" );
		help.getItems().add( Actions.createMenuItem( program, "welcome" ) );
		help.getItems().add( new SeparatorMenuItem() );
		help.getItems().add( Actions.createMenuItem( program, "help-content" ) );
		help.getItems().add( new SeparatorMenuItem() );
		help.getItems().add( Actions.createMenuItem( program, "update" ) );
		help.getItems().add( Actions.createMenuItem( program, "about" ) );

		Menu dev = Actions.createMenu( program, "development" );
		dev.getItems().add( Actions.createMenuItem( program, "restart" ) );

		if( program.getExecMode() == ExecMode.DEV ) {
			menubar.getMenus().addAll( file, edit, view, help, dev );
		} else {
			menubar.getMenus().addAll( file, edit, view, help );
		}

		// TOOLBAR

		Menu workareaMenu = Actions.createMenu( program, "workarea" );
		workareaMenu.getItems().add( Actions.createMenuItem( program, "workarea-new" ) );
		workareaMenu.getItems().add( new SeparatorMenuItem() );
		workareaMenu.getItems().add( Actions.createMenuItem( program, "workarea-rename" ) );
		workareaMenu.getItems().add( new SeparatorMenuItem() );
		workareaMenu.getItems().add( Actions.createMenuItem( program, "workarea-close" ) );

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

		toolbar = new ToolBar();
		toolbar.getItems().add( Actions.createToolBarButton( program, "new" ) );
		toolbar.getItems().add( Actions.createToolBarButton( program, "open" ) );
		toolbar.getItems().add( Actions.createToolBarButton( program, "save" ) );
		toolbar.getItems().add( new Separator() );
		toolbar.getItems().add( Actions.createToolBarButton( program, "undo" ) );
		toolbar.getItems().add( Actions.createToolBarButton( program, "redo" ) );
		//		toolbar.getItems().add( new Separator() );
		//		toolbar.getItems().add( Actions.createToolBarButton( program, "cut" ) );
		//		toolbar.getItems().add( Actions.createToolBarButton( program, "copy" ) );
		//		toolbar.getItems().add( Actions.createToolBarButton( program, "paste" ) );

		toolbar.getItems().add( Actions.createSpring() );

		toolbar.getItems().add( workareaMenuBar );
		toolbar.getItems().add( workareaSelector );

		// Workarea Container
		workpaneContainer = new StackPane();

		// TODO Remove the development background image
		// The following background image is for development purposes.
		Image image = new Image( getClass().getResourceAsStream( "/wallpaper.jpg" ) );
		BackgroundSize backgroundSize = new BackgroundSize( BackgroundSize.AUTO, BackgroundSize.AUTO, false, false, false, true );
		workpaneContainer.setBackground( new Background( new BackgroundImage( image, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER, backgroundSize ) ) );

		VBox bars = new VBox();
		bars.getChildren().addAll( menubar, toolbar );

		layout = new BorderPane();
		layout.setTop( bars );
		layout.setCenter( workpaneContainer );

		// Create the scene
		stage = new Stage();
		stage.getIcons().addAll( program.getIconLibrary().getIconImages( "program" ) );
		stage.setOnCloseRequest( event -> {
			program.getWorkspaceManager().requestCloseWorkspace( this );
			event.consume();
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

	@Override
	public void setSettings( Settings settings ) {
		if( this.settings != null ) return;

		this.settings = settings;
		id = settings.get( "id" );

		Double x = settings.getDouble( "x", null );
		Double y = settings.getDouble( "y", null );
		Double w = settings.getDouble( "w", UiManager.DEFAULT_WIDTH );
		Double h = settings.getDouble( "h", UiManager.DEFAULT_HEIGHT );

		// Due to differences in how FX handles stage size (width and height) on
		// different operating systems, the width and height from the scene, not the
		// stage, are used. This includes the listeners for the width and height
		// properties below.
		stage.setScene( scene = new Scene( layout, w, h ) );
		scene.getStylesheets().add( Program.STYLESHEET );

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

		stage.setMaximized( settings.getBoolean( "maximized", false ) );
		setActive( settings.getBoolean( "active", false ) );

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
	}

	@Override
	public Settings getSettings() {
		return settings;
	}

	private void setStageTitle( String name ) {
		stage.setTitle( name + " - " + program.getCard().getName() );
	}

	private Workarea determineNextActiveWorkarea() {
		int index = workareas.indexOf( getActiveWorkarea() );
		return workareas.get( index == 0 ? 1 : index - 1 );
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
