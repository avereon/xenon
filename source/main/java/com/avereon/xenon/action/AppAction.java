package com.avereon.xenon.action;

import com.avereon.xenon.Program;
import com.avereon.xenon.ProgramAction;
import javafx.event.ActionEvent;
import javafx.geometry.Point2D;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
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
		//Window parent = ((Node)event.getTarget()).getScene().getWindow();
		//Pane parent = FxUtil.findParentByClass( button, Pane.class );

		log.atConfig().log( "source=%s", button );

		Point2D anchor = button.localToScreen( button.getWidth(), 0 );
		//Point2D anchor = button.localToScreen( 0, button.getHeight() );

		// This worked, just way far left on the left monitor (where it should be)
		menu.show( button, anchor.getX(), anchor.getY() );

		//popup.getContent().addAll( menu );
		// TODO Show the program menu...to the right of the action button
	}

}
