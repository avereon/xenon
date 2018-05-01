package com.xeomar.xenon.action;

import com.xeomar.util.LogUtil;
import com.xeomar.xenon.Action;
import com.xeomar.xenon.Program;
import com.xeomar.xenon.UiFactory;
import com.xeomar.xenon.util.DialogUtil;
import com.xeomar.xenon.workarea.Workarea;
import javafx.event.ActionEvent;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Stage;
import org.slf4j.Logger;

import java.lang.invoke.MethodHandles;
import java.util.Optional;

public class NewWorkareaAction extends Action<ActionEvent> {

	private static final Logger log = LogUtil.get( MethodHandles.lookup().lookupClass() );

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
		dialog.setTitle( program.getResourceBundle().getString( "workarea", "workarea.new.title" ) );
		dialog.setHeaderText( program.getResourceBundle().getString( "workarea", "workarea.new.message" ) );
		dialog.setContentText( program.getResourceBundle().getString( "workarea", "workarea.new.prompt" ) );

		Stage stage = program.getWorkspaceManager().getActiveWorkspace().getStage();
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
			log.error( "Error creating new workarea: " + name, exception );
		}
	}

}
