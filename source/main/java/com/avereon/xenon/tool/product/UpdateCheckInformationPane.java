package com.avereon.xenon.tool.product;

import com.avereon.product.Rb;
import com.avereon.settings.SettingsEvent;
import com.avereon.xenon.RbKey;
import com.avereon.xenon.Xenon;
import com.avereon.xenon.product.ProductManager;
import com.avereon.zarra.javafx.Fx;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;

class UpdateCheckInformationPane extends HBox {

	private Xenon program;

	private Label lastUpdateCheckField;

	private Label nextUpdateCheckField;

	UpdateCheckInformationPane( Xenon program ) {
		this.program = program;
		Label lastUpdateCheckLabel = new Label( Rb.text( RbKey.UPDATE, "product-update-check-last" ) );
		Label nextUpdateCheckLabel = new Label( Rb.text( RbKey.UPDATE, "product-update-check-next" ) );
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

		Fx.run( () -> {
			lastUpdateCheckField.setText( lastUpdateCheck );
			nextUpdateCheckField.setText( nextUpdateCheck );
		} );
	}

}
