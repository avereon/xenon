package com.avereon.xenon.tool.settings.editor;

import com.avereon.settings.SettingsEvent;
import com.avereon.util.DateUtil;
import com.avereon.xenon.BundleKey;
import com.avereon.xenon.Program;
import com.avereon.xenon.ProgramProduct;
import com.avereon.xenon.tool.settings.Setting;
import com.avereon.xenon.tool.settings.SettingEditor;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;

import java.util.Date;
import java.util.TimeZone;

public class UpdateSettingViewer extends SettingEditor {

	private Label lastUpdateCheckField;

	private Label nextUpdateCheckField;

	public UpdateSettingViewer( ProgramProduct product, Setting setting ) {
		super( product, setting );
	}

	@Override
	public void addComponents( GridPane pane, int row ) {
		Program program = product.getProgram();

		Label lastUpdateCheckLabel = new Label( program.getResourceBundle().getString( BundleKey.UPDATE, "product-update-check-last" ) );
		Label nextUpdateCheckLabel = new Label( program.getResourceBundle().getString( BundleKey.UPDATE, "product-update-check-next" ) );
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

		HBox container = new HBox();
		container.getChildren().addAll( lastUpdateCheckLabel, lastUpdateCheckField, spring, nextUpdateCheckLabel, nextUpdateCheckField );

		GridPane.setHgrow( container, Priority.ALWAYS );
		GridPane.setColumnSpan( container, row );
		pane.addRow( row, container );

		updateLabels();
	}

	@Override
	public void setDisable( boolean disable ) {
		//
	}

	@Override
	public void setVisible( boolean visible ) {
		lastUpdateCheckField.setVisible( visible );
		nextUpdateCheckField.setVisible( visible );
	}

	@Override
	public void handleEvent( SettingsEvent event ) {
		Platform.runLater( this::updateLabels );
	}

	private void updateLabels() {
		Program program = product.getProgram();
		String unknown = product.getResourceBundle().getString( BundleKey.UPDATE, "unknown" );
		String notScheduled = product.getResourceBundle().getString( BundleKey.UPDATE, "not-scheduled" );

		long lastUpdateCheck = program.getProductManager().getLastUpdateCheck();
		long nextUpdateCheck = program.getProductManager().getNextUpdateCheck();
		if( nextUpdateCheck < System.currentTimeMillis() ) nextUpdateCheck = 0;

		// Update the labels
		lastUpdateCheckField.setText( (lastUpdateCheck == 0 ? unknown : DateUtil.format( new Date( lastUpdateCheck ), DateUtil.DEFAULT_DATE_FORMAT, TimeZone.getDefault() )) );
		nextUpdateCheckField.setText( (nextUpdateCheck == 0 ? notScheduled : DateUtil.format( new Date( nextUpdateCheck ), DateUtil.DEFAULT_DATE_FORMAT, TimeZone.getDefault() )) );
	}

}
