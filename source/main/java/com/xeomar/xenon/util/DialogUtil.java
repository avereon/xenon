package com.xeomar.xenon.util;

import javafx.beans.value.ChangeListener;
import javafx.scene.control.Dialog;
import javafx.stage.Stage;

import java.util.Optional;

public class DialogUtil {

	public static void show( Stage owner, Dialog dialog ) {
		centerOnStage( owner, dialog );
		dialog.initOwner( owner );
		dialog.show();
	}

	public static <R> Optional<R> showAndWait( Stage owner, Dialog<R> dialog ) {
		centerOnStage( owner, dialog );
		dialog.initOwner( owner );
		return dialog.showAndWait();
	}

	private static void centerOnStage( Stage stage, Dialog dialog ) {
		// The following line is a workaround to dialogs showing with zero size on Linux
		dialog.setResizable( true );

		ChangeListener<Number> widthListener = ( observable, oldValue, newValue ) -> dialog.setX( stage.getX() + (stage.getWidth() - newValue.doubleValue()) / 2 );
		ChangeListener<Number> heightListener = ( observable, oldValue, newValue ) -> dialog.setY( stage.getY() + (stage.getHeight() - newValue.doubleValue()) / 2 );

		dialog.widthProperty().addListener( widthListener );
		dialog.heightProperty().addListener( heightListener );

		//Once the dialog is visible, remove the listeners
		dialog.setOnShown( event -> {
			dialog.widthProperty().removeListener( widthListener );
			dialog.heightProperty().removeListener( heightListener );
		} );
	}

}
