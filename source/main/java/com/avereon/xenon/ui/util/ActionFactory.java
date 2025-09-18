package com.avereon.xenon.ui.util;

import com.avereon.xenon.ActionProxy;
import com.avereon.xenon.Xenon;
import javafx.scene.control.Button;

public class ActionFactory {

	public static final String BUTTON_ID_PREFIX = "button-";

	public static Button createButton( Xenon program, ActionProxy action ) {
		Button button = new Button( action.getName() );
		button.setId( BUTTON_ID_PREFIX + action.getId() );
		button.setDisable( !action.isEnabled() );
		button.setGraphic( program.getIconLibrary().getIcon( action.getIcon() ) );

		action.enabledProperty().addListener( _ -> button.setDisable( !action.isEnabled() ) );
		action.iconProperty().addListener( _ -> button.setGraphic( program.getIconLibrary().getIcon( action.getIcon() ) ) );

		return button;
	}

}
