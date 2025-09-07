package com.avereon.xenon.util;

import javafx.event.EventHandler;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.CustomLog;

import java.util.Optional;

@CustomLog
public class DialogUtil {

	public static <R> void show( Stage owner, Dialog<R> dialog ) {
		registerCenterOnStage( owner, dialog );
		dialog.show();
	}

	public static <R> Optional<R> showAndWait( Stage owner, Dialog<R> dialog ) {
		registerCenterOnStage( owner, dialog );
		return dialog.showAndWait();
	}

	private static <R> void registerCenterOnStage( Stage stage, Dialog<R> dialog ) {
		dialog.initOwner( stage );
		dialog.initModality( Modality.WINDOW_MODAL );
		// WORKAROUND To dialogs showing with zero size on Linux
		dialog.setResizable( true );

		final EventHandler<DialogEvent> shownHandler = _ -> {
			double x = stage.getX() + 0.5 * (stage.getWidth() - dialog.getWidth());
			double y = stage.getY() + 0.5 * (stage.getHeight() - dialog.getHeight());
			dialog.setX( x );
			dialog.setY( y );
			// TODO Need to remove the shown handler to prevent memory leaks
			dialog.setOnShown( null );
		};
		dialog.setOnShown( shownHandler );
	}

}
