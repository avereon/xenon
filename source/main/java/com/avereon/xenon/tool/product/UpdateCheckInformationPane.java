package com.avereon.xenon.tool.product;

import com.avereon.event.EventHandler;
import com.avereon.settings.SettingsEvent;
import com.avereon.util.DateUtil;
import com.avereon.xenon.BundleKey;
import com.avereon.xenon.Program;
import com.avereon.xenon.product.ProductManager;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;

import java.util.Date;
import java.util.TimeZone;

class UpdateCheckInformationPane extends HBox implements EventHandler<SettingsEvent> {

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

		program.getProductManager().getSettings().addSettingsListener( this );
	}

	void updateInfo() {
		long lastUpdateCheck = program.getProductManager().getLastUpdateCheck();
		long nextUpdateCheck = program.getProductManager().getNextUpdateCheck();
		if( nextUpdateCheck < System.currentTimeMillis() ) nextUpdateCheck = 0;

		String unknown = program.rb().text( BundleKey.UPDATE, "unknown" );
		String notScheduled = program.rb().text( BundleKey.UPDATE, "not-scheduled" );
		String lastUpdateCheckText = lastUpdateCheck == 0 ? unknown : DateUtil.format( new Date( lastUpdateCheck ), DateUtil.DEFAULT_DATE_FORMAT, TimeZone.getDefault() );
		String nextUpdateCheckText = nextUpdateCheck == 0 ? notScheduled : DateUtil.format( new Date( nextUpdateCheck ), DateUtil.DEFAULT_DATE_FORMAT, TimeZone.getDefault() );

		Platform.runLater( () -> {
			lastUpdateCheckField.setText( lastUpdateCheckText );
			nextUpdateCheckField.setText( nextUpdateCheckText );
		} );
	}

	@Override
	public void handle( SettingsEvent event ) {
		if( event.getEventType() != SettingsEvent.CHANGED ) return;
		switch( event.getKey() ) {
			case ProductManager.LAST_CHECK_TIME:
			case ProductManager.NEXT_CHECK_TIME: {
				updateInfo();
			}
		}
	}

}
