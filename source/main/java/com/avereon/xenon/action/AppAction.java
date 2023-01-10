package com.avereon.xenon.action;

import com.avereon.xenon.Program;
import com.avereon.xenon.ProgramAction;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.stage.Window;
import lombok.CustomLog;

@CustomLog
public class AppAction extends ProgramAction {

	public AppAction( Program program ) {
		super( program );
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public void handle( ActionEvent event ) {
		ContextMenu menu = new ContextMenu();
		menu.getItems().addAll( new MenuItem( "Menu A" ), new MenuItem( "Menu B" ) );

		Button button = (Button)event.getSource();
		Window parent = ((Node)event.getTarget()).getScene().getWindow();

		log.atConfig().log( "source=%s parent=%s", button, parent );

		// This worked, just way far left on the left monitor (where it should be)
		menu.show( parent, 100, 100 );

		//popup.getContent().addAll( menu );
		// TODO Show the program menu...to the right of the action button
	}

}
