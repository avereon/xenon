package com.parallelsymmetry.essence;

import com.parallelsymmetry.essence.settings.WritableSettings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.stage.Stage;

public class WorkWindowBoundsWatcher {

	public WorkWindowBoundsWatcher( Stage stage, WritableSettings settings ) {
		// TODO Construct a settings path based on the WorkWindow id

		stage.xProperty().addListener( new ChangeListener<Number>() {

			@Override
			public void changed( ObservableValue<? extends Number> observableValue, Number oldStageX, Number newStageX ) {
				settings.put( "program/windows/0/x", String.valueOf( newStageX ) );
				System.out.println( "X: " + newStageX );
			}
		} );

		stage.yProperty().addListener( new ChangeListener<Number>() {

			@Override
			public void changed( ObservableValue<? extends Number> observableValue, Number oldStageY, Number newStageY ) {
				System.out.println( "Y: " + newStageY );
			}
		} );

		stage.widthProperty().addListener( new ChangeListener<Number>() {

			@Override
			public void changed( ObservableValue<? extends Number> observableValue, Number oldStageWidth, Number newStageWidth ) {
				System.out.println( "W: " + newStageWidth );
			}
		} );

		stage.heightProperty().addListener( new ChangeListener<Number>() {

			@Override
			public void changed( ObservableValue<? extends Number> observableValue, Number oldStageHeight, Number newStageHeight ) {
				System.out.println( "H: " + newStageHeight );
			}
		} );

	}

}
