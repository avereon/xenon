package com.avereon.xenon.action;

import com.avereon.xenon.Action;
import com.avereon.xenon.Program;
import com.avereon.xenon.util.DialogUtil;
import com.avereon.xenon.workarea.Workarea;
import javafx.event.ActionEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

import java.util.Optional;

public class CloseWorkareaAction extends Action {

	public CloseWorkareaAction( Program program ) {
		super( program );
	}

	@Override
	public boolean isEnabled() {
		return getProgram().getWorkspaceManager().getActiveWorkspace().getWorkareas().size() > 1;
	}

	@Override
	public void handle( ActionEvent event ) {
		Program program = getProgram();
		Workarea workarea = program.getWorkspaceManager().getActiveWorkspace().getActiveWorkarea();

		Alert alert = new Alert( Alert.AlertType.CONFIRMATION );
		alert.setTitle( program.rb().text( "workarea", "workarea.close.title" ) );
		alert.setHeaderText( program.rb().text( "workarea", "workarea.close.message" ) );
		alert.setContentText( program.rb().text( "workarea", "workarea.close.prompt", workarea.getName() ) );

		Stage stage = program.getWorkspaceManager().getActiveStage();
		Optional<ButtonType> result = DialogUtil.showAndWait( stage, alert );

		if( result.isPresent() && result.get() == ButtonType.OK ) closeWorkarea( workarea );
	}

	private void closeWorkarea( Workarea workarea ) {
		getProgram().getWorkspaceManager().getActiveWorkspace().removeWorkarea( workarea );
	}

}
