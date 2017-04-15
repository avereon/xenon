package com.parallelsymmetry.essence.work;

import com.parallelsymmetry.essence.product.Product;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.apache.commons.configuration2.Configuration;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class Workspace {

	private Product product;

	private Stage stage;

	private Scene scene;

	private boolean active;

	private Set<Workarea> workareas;

	private Workarea activeWorkarea;

	private WorkareaPropertyWatcher activeWorkareaWatcher;

	private Configuration configuration;

	private String id;

	public Workspace( Product product ) {
		this.product = product;

		workareas = new CopyOnWriteArraySet<>();
		activeWorkareaWatcher = new WorkareaPropertyWatcher();

		stage = new Stage();
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
			activeWorkarea.addPropertyChangeListener( activeWorkareaWatcher );
			setStageTitle( activeWorkarea.getName() );
		}
	}

	public void setConfiguration( Configuration configuration ) {
		if( this.configuration != null ) return;

		this.configuration = configuration;
		id = configuration.getString( "id" );

		Double x = configuration.getDouble( "x", null );
		Double y = configuration.getDouble( "y", null );
		Double w = configuration.getDouble( "w" );
		Double h = configuration.getDouble( "h" );

		// Create the scene using the width and height
		stage.setScene( scene = new Scene( new Pane(), w, h ) );
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
		stage.setTitle( name + " - " + product.getMetadata().getName() );
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
