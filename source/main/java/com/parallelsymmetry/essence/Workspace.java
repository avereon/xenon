package com.parallelsymmetry.essence;

import com.parallelsymmetry.essence.product.Product;
import com.parallelsymmetry.essence.work.Workarea;
import javafx.stage.Stage;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class Workspace {

	private Product product;

	private Stage stage;

	private Set<Workarea> workareas;

	private Workarea activeWorkarea;

	private WorkAreaPropertyWatcher activeWorkareaWatcher;

	public Workspace( Product product ) {
		this.product = product;

		stage = new Stage();
		workareas = new CopyOnWriteArraySet<>();
		activeWorkareaWatcher = new WorkAreaPropertyWatcher();
	}

	public Stage getStage() {
		return stage;
	}

	public Set<Workarea> getWorkareas() {
		return new HashSet<Workarea>( workareas );
	}

	public void addWorkArea( Workarea workarea ) {
		workareas.add( workarea );
	}

	public void removeWorkArea( Workarea workarea ) {
		// Handle the situation where the work area is active

		workareas.remove( workarea );

		// If needed set the active work area
	}

	public Workarea getActiveWorkarea() {
		return activeWorkarea;
	}

	public void setActiveWorkarea( Workarea workarea ) {
		// If the workarea is not already added, add it
		if( !workareas.contains( workarea )) addWorkArea( workarea );

		// Disconnect the old active work area
		activeWorkarea.removePropertyChangeListener( activeWorkareaWatcher );

		// Swap the work area on the stage
		activeWorkarea = workarea;

		// Connect the new active work area
		activeWorkarea.addPropertyChangeListener( activeWorkareaWatcher );

		setStageTitle( workarea.getName() );
	}

	private void setStageTitle( String name ) {
		stage.setTitle( name + " - " + product.getMetadata().getName() );
	}

	private class WorkAreaPropertyWatcher implements PropertyChangeListener {

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
