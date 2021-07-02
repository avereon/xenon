package com.avereon.xenon.action;

import com.avereon.product.Rb;
import com.avereon.util.Log;
import com.avereon.xenon.ProgramAction;
import com.avereon.xenon.Program;
import com.avereon.xenon.UiFactory;
import com.avereon.xenon.util.DialogUtil;
import com.avereon.xenon.workspace.Workarea;
import javafx.event.ActionEvent;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Stage;

import java.lang.System.Logger;
import java.util.Optional;

public class NewWorkareaAction extends ProgramAction {

	private static final Logger log = Log.get();

	public NewWorkareaAction( Program program ) {
		super( program );
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public void handle( ActionEvent event ) {
		Program program = getProgram();

		TextInputDialog dialog = new TextInputDialog();
		dialog.setTitle( Rb.text( "workarea", "workarea-new-title" ) );
		dialog.setHeaderText( Rb.text( "workarea", "workarea-new-message" ) );
		dialog.setContentText( Rb.text( "workarea", "workarea-new-prompt" ) );

		Stage stage = program.getWorkspaceManager().getActiveStage();
		Optional<String> result = DialogUtil.showAndWait( stage, dialog );

		result.ifPresent( this::createNewWorkarea );
	}

	private void createNewWorkarea( String name ) {
		UiFactory uiFactory = new UiFactory( getProgram() );
		try {
			Workarea workarea = uiFactory.newWorkarea();
			workarea.setName( name );
			getProgram().getWorkspaceManager().getActiveWorkspace().setActiveWorkarea( workarea );
		} catch( Exception exception ) {
			log.log( Log.ERROR,  "Error creating new workarea: " + name, exception );
		}
	}

}
