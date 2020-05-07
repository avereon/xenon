package com.avereon.xenon.tool.product;

import com.avereon.settings.SettingsEvent;
import com.avereon.xenon.BundleKey;
import com.avereon.xenon.Program;
import com.avereon.xenon.product.ProductManager;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;

class UpdateCheckInformationPane extends HBox {

	private Program program;

	private Label lastUpdateCheckField;

	private Label nextUpdateCheckField;

	UpdateCheckInformationPane( Program program ) {
		this.program = program;
		Label lastUpdateCheckLabel = new Label( program.rb().text( BundleKey.UPDATE, "product-update-check-last" ) );
		Label nextUpdateCheckLabel = new Label( program.rb().text( BundleKey.UPDATE, "product-update-check-next" ) );
		lastUpdateCheckLabel.setId( "product-update-check-last-prompt" );
		nextUpdateCheckLabel.setId( "product-update-check-next-prompt" );
		lastUpdateCheckLabel.getStyleClass().add( "prompt" );
		nextUpdateCheckLabel.getStyleClass().add( "prompt" );

		lastUpdateCheckField = new Label();
		nextUpdateCheckField = new Label();
		lastUpdateCheckField.setId( "product-update-check-last-field" );
		nextUpdateCheckField.setId( "product-update-check-next-field" );

		lastUpdateCheckLabel.setLabelFor( lastUpdateCheckField );
		nextUpdateCheckLabel.setLabelFor( nextUpdateCheckField );

		Pane spring = new Pane();
		HBox.setHgrow( spring, Priority.ALWAYS );
		getChildren().addAll( lastUpdateCheckLabel, lastUpdateCheckField, spring, nextUpdateCheckLabel, nextUpdateCheckField );

		program.getProductManager().getSettings().register( SettingsEvent.CHANGED, e -> {
			switch( e.getKey() ) {
				case ProductManager.LAST_CHECK_TIME:
				case ProductManager.NEXT_CHECK_TIME: {
					updateInfo();
				}
			}
		} );
	}

	void updateInfo() {
		String lastUpdateCheck = program.getProductManager().getLastUpdateCheckText();
		String nextUpdateCheck = program.getProductManager().getNextUpdateCheckText();

		Platform.runLater( () -> {
			lastUpdateCheckField.setText( lastUpdateCheck );
			nextUpdateCheckField.setText( nextUpdateCheck );
		} );
	}

}
