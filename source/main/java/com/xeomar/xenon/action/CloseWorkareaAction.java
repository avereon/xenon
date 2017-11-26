package com.xeomar.xenon.action;

import com.xeomar.util.LogUtil;
import com.xeomar.xenon.Action;
import com.xeomar.xenon.Program;
import com.xeomar.xenon.util.DialogUtil;
import com.xeomar.xenon.workarea.Workarea;
import javafx.event.Event;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import org.slf4j.Logger;

import java.util.Optional;

public class CloseWorkareaAction extends Action {

	private static Logger log = LogUtil.get( CloseWorkareaAction.class );

	public CloseWorkareaAction( Program program ) {
		super( program );
	}

	@Override
	public boolean isEnabled() {
		return getProgram().getWorkspaceManager().getActiveWorkspace().getWorkareas().size() > 1;
	}

	@Override
	public void handle( Event event ) {
		Program program = getProgram();
		Workarea workarea = program.getWorkspaceManager().getActiveWorkspace().getActiveWorkarea();

		Alert alert = new Alert( Alert.AlertType.CONFIRMATION );
		alert.setTitle( program.getResourceBundle().getString( "workarea", "workarea.close.title" ) );
		alert.setHeaderText( program.getResourceBundle().getString( "workarea", "workarea.close.message" ) );
		alert.setContentText( program.getResourceBundle().getString( "workarea", "workarea.close.prompt", workarea.getName() ) );

		Stage stage = program.getWorkspaceManager().getActiveWorkspace().getStage();
		Optional<ButtonType> result = DialogUtil.showAndWait( stage, alert );

		if( result.isPresent() && result.get() == ButtonType.OK ) closeWorkarea( workarea );
	}

	private void closeWorkarea( Workarea workarea ) {
		getProgram().getWorkspaceManager().getActiveWorkspace().removeWorkarea( workarea );
	}

}
