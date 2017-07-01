package com.parallelsymmetry.essence.action;

import com.parallelsymmetry.essence.Action;
import com.parallelsymmetry.essence.LogUtil;
import com.parallelsymmetry.essence.Program;
import com.parallelsymmetry.essence.UiFactory;
import com.parallelsymmetry.essence.workarea.Workarea;
import javafx.event.ActionEvent;
import javafx.scene.control.TextInputDialog;
import org.slf4j.Logger;

import java.util.Optional;

public class NewWorkareaAction extends Action<ActionEvent> {

	private static Logger log = LogUtil.get( NewWorkareaAction.class );

	public NewWorkareaAction( Program program ) {
		super( program );
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public void handle( ActionEvent event ) {
		TextInputDialog dialog = new TextInputDialog();
		dialog.initOwner( program.getWorkspaceManager().getActiveWorkspace().getStage() );
		dialog.setTitle( program.getResourceBundle().getString( "workarea", "workarea.new.title" ) );
		dialog.setHeaderText( program.getResourceBundle().getString( "workarea", "workarea.new.message" ) );
		dialog.setContentText( program.getResourceBundle().getString( "workarea", "workarea.new.prompt" ) );

		Optional<String> result = dialog.showAndWait();
		result.ifPresent( this::createNewWorkarea );
	}

	private void createNewWorkarea( String name ) {
		UiFactory uiFactory = new UiFactory( program );
		try {
			Workarea workarea = uiFactory.newWorkarea();
			workarea.setName( name );
			program.getWorkspaceManager().getActiveWorkspace().setActiveWorkarea( workarea );
		} catch( Exception exception ) {
			log.error( "Error creating new workarea: " + name, exception );
		}
	}

}
