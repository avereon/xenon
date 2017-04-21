package com.parallelsymmetry.essence.work;

import com.parallelsymmetry.essence.Program;
import com.parallelsymmetry.essence.action.NewWorkareaAction;
import com.parallelsymmetry.essence.event.WorkareaChangedEvent;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Side;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.apache.commons.configuration2.Configuration;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashSet;
import java.util.Set;

/**
 * The workspace manages the menu bar, tool bar and workareas.
 */
public class Workspace {

	private Program program;

	private Stage stage;

	private Scene scene;

	private boolean active;

	private Pane menubarContainer;

	private HBox toolbarContainer;

	private MenuBar menubar;

	private ToolBar toolbar;

	private ComboBox<Workarea> workareaSelector;

	//private Set<Workarea> workareas;
	ObservableList<Workarea> workareas;

	private Workarea activeWorkarea;

	private WorkareaPropertyWatcher activeWorkareaWatcher;

	private Configuration configuration;

	private String id;

	private Workarea event;

	public Workspace( Program program ) {
		this.program = program;

		workareas = FXCollections.observableArrayList();
		activeWorkareaWatcher = new WorkareaPropertyWatcher();

		stage = new Stage();

		// MENUBAR

		menubar = new MenuBar();

		Menu file = new Menu( "File" );
		file.getItems().add( new MenuItem( "A" ) );
		file.getItems().add( new MenuItem( "B" ) );
		file.getItems().add( new MenuItem( "C" ) );
		file.getItems().add( new MenuItem( "D" ) );
		Menu edit = new Menu( "Edit" );
		edit.getItems().add( new MenuItem( "A" ) );
		edit.getItems().add( new MenuItem( "B" ) );
		edit.getItems().add( new MenuItem( "C" ) );
		edit.getItems().add( new MenuItem( "D" ) );
		Menu view = new Menu( "View" );
		view.getItems().add( new MenuItem( "A" ) );
		view.getItems().add( new MenuItem( "B" ) );
		view.getItems().add( new MenuItem( "C" ) );
		view.getItems().add( new MenuItem( "D" ) );
		Menu help = new Menu( "Help" );
		help.getItems().add( new MenuItem( "A" ) );
		help.getItems().add( new MenuItem( "B" ) );
		help.getItems().add( new MenuItem( "C" ) );

		Menu spacer = new Menu( "" );
		spacer.setDisable( true );

		MenuItem newWorkareaMenuItem = new MenuItem( "New" );
		newWorkareaMenuItem.setOnAction( new NewWorkareaAction( program ) );

		ContextMenu workareaMenu = new ContextMenu();
		workareaMenu.getItems().add( newWorkareaMenuItem );
		workareaMenu.getItems().add( new SeparatorMenuItem() );
		workareaMenu.getItems().add( new MenuItem( "Rename" ) );
		workareaMenu.getItems().add( new SeparatorMenuItem() );
		workareaMenu.getItems().add( new MenuItem( "Close" ) );

		menubar.getMenus().addAll( file, edit, view, help );

		// TOOLBAR
		Button newButton = new Button( "N" );
		Button openButton = new Button( "O" );
		Button saveButton = new Button( "S" );

		// Toolbar spring
		Region spring = new Region();
		HBox.setHgrow( spring, Priority.ALWAYS );

		// Workarea label
		Background hoverBackground = new Background( new BackgroundFill( Color.RED, CornerRadii.EMPTY, Insets.EMPTY ) );

		Label workareaLabel = new Label( "Workarea: " );
		workareaLabel.setId( "workarea-label" );
		workareaLabel.setContextMenu( workareaMenu );
		workareaLabel.setOnMousePressed( (event -> workareaMenu.show( workareaLabel, Side.BOTTOM, 0, 0 )) );
		//		Background normalBackground = workareaLabel.getBackground();
		//		workareaLabel.setOnMouseEntered( (event -> workareaLabel.setBackground( hoverBackground )) );
		//		workareaLabel.setOnMouseExited( (event -> workareaLabel.setBackground( normalBackground )) );

		// Workarea selector
		workareaSelector = new ComboBox<>();
		workareaSelector.setItems( workareas );
		workareaSelector.valueProperty().addListener( ( value, oldValue, newValue ) -> setActiveWorkarea( newValue ) );

		toolbar = new ToolBar();
		toolbar.getItems().addAll( newButton, openButton, saveButton, spring, workareaLabel, workareaSelector );
	}

	private void selectWorkarea( ActionEvent event  ) {

	}

	public String getId() {
		return id;
	}

	public Stage getStage() {
		return stage;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive( boolean active ) {
		this.active = active;
		configuration.setProperty( "active", active );
	}

	public Set<Workarea> getWorkareas() {
		return new HashSet<Workarea>( workareas );
	}

	public void addWorkArea( Workarea workarea ) {
		Workspace oldWorkspace = workarea.getWorkspace();
		if( oldWorkspace != null ) oldWorkspace.removeWorkArea( workarea );
		workareas.add( workarea );
		workarea.setWorkspace( this );
	}

	public void removeWorkArea( Workarea workarea ) {
		// Handle the situation where the work area is active

		workareas.remove( workarea );
		workarea.setWorkspace( null );

		// If needed set the active work area
	}

	public Workarea getActiveWorkarea() {
		return activeWorkarea;
	}

	public void setActiveWorkarea( Workarea workarea ) {
		if( activeWorkarea == workarea ) return;

		// If the workarea is not already added, add it
		if( !workareas.contains( workarea ) ) addWorkArea( workarea );

		// Disconnect the old active work area
		if( activeWorkarea != null ) {
			activeWorkarea.removePropertyChangeListener( activeWorkareaWatcher );
			activeWorkarea.setActive( false );
		}

		// Swap the work area on the stage
		activeWorkarea = workarea;

		// Connect the new active work area
		if( activeWorkarea != null ) {
			activeWorkarea.setActive( true );
			setStageTitle( activeWorkarea.getName() );
			workareaSelector.getSelectionModel().select( activeWorkarea );
			activeWorkarea.addPropertyChangeListener( activeWorkareaWatcher );

			// TODO Set the menu bar
			// TODO Set the tool bar
			// TODO Set the workpane
		}

		// Send a program event when active area changes
		program.fireEvent( new WorkareaChangedEvent( this, activeWorkarea) );
	}

	public void setConfiguration( Configuration configuration ) {
		if( this.configuration != null ) return;

		this.configuration = configuration;
		id = configuration.getString( "id" );

		Double x = configuration.getDouble( "x", null );
		Double y = configuration.getDouble( "y", null );
		Double w = configuration.getDouble( "w" );
		Double h = configuration.getDouble( "h" );

		VBox pane = new VBox();
		pane.getChildren().addAll( menubar, toolbar );

		// Create the scene using the width and height
		stage.setScene( scene = new Scene( pane, w, h ) );
		stage.centerOnScreen();

		// Position the stage if x and y are specified
		if( x != null ) stage.setX( x );
		if( y != null ) stage.setY( y );

		stage.setMaximized( configuration.getBoolean( "maximized", false ) );
		setActive( configuration.getBoolean( "active", false ) );

		// Add the property listeners
		stage.maximizedProperty().addListener( ( observableValue, oldValue, newValue ) -> {
			configuration.setProperty( "maximized", newValue );
		} );
		stage.xProperty().addListener( ( observableValue, oldValue, newValue ) -> {
			if( !stage.isMaximized() ) configuration.setProperty( "x", newValue );
		} );
		stage.yProperty().addListener( ( observableValue, oldValue, newValue ) -> {
			if( !stage.isMaximized() ) configuration.setProperty( "y", newValue );
		} );
		scene.widthProperty().addListener( ( observableValue, oldValue, newValue ) -> {
			if( !stage.isMaximized() ) configuration.setProperty( "w", newValue );
		} );
		scene.heightProperty().addListener( ( observableValue, oldValue, newValue ) -> {
			if( !stage.isMaximized() ) configuration.setProperty( "h", newValue );
		} );
	}

	private void setStageTitle( String name ) {
		stage.setTitle( name + " - " + program.getMetadata().getName() );
	}

	private class WorkareaPropertyWatcher implements PropertyChangeListener {

		@Override
		public void propertyChange( PropertyChangeEvent event ) {
			switch( event.getPropertyName() ) {
				case "name": {
					setStageTitle( event.getNewValue().toString() );
					break;
				}
			}
		}

	}

}
