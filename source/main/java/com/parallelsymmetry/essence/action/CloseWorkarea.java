package com.parallelsymmetry.essence.action;

import com.parallelsymmetry.essence.Program;
import com.parallelsymmetry.essence.Action;
import com.parallelsymmetry.essence.workarea.Workarea;
import javafx.event.Event;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class CloseWorkarea extends Action {

	private static Logger log = LoggerFactory.getLogger( CloseWorkarea.class );

	public CloseWorkarea( Program program ) {
		super( program );
	}

	@Override
	public boolean isEnabled() {
		return program.getWorkspaceManager().getActiveWorkspace().getWorkareas().size() > 1;
	}

	@Override
	public void handle( Event event ) {
		Workarea workarea = program.getWorkspaceManager().getActiveWorkspace().getActiveWorkarea();

		Alert alert = new Alert( Alert.AlertType.CONFIRMATION );
		alert.initOwner( program.getWorkspaceManager().getActiveWorkspace().getStage() );
		alert.setTitle( program.getResourceBundle().getString( "workarea", "workarea.close.title" ) );
		alert.setHeaderText( program.getResourceBundle().getString( "workarea", "workarea.close.message" ) );
		alert.setContentText( program.getResourceBundle().getString( "workarea", "workarea.close.prompt", workarea.getName() ) );

		Optional<ButtonType> result = alert.showAndWait();
		if( result.get() == ButtonType.OK ) closeWorkarea( workarea );
	}

	private void closeWorkarea( Workarea workarea ) {
		program.getWorkspaceManager().getActiveWorkspace().removeWorkarea( workarea );
	}

}
