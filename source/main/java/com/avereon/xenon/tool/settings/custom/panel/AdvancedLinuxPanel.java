package com.avereon.xenon.tool.settings.custom.panel;

import com.avereon.product.Rb;
import com.avereon.xenon.ProgramChecks;
import com.avereon.xenon.RbKey;
import com.avereon.xenon.XenonProgramProduct;
import com.avereon.xenon.tool.settings.SettingsPanel;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Screen;

import java.nio.file.Files;
import java.nio.file.Paths;

public class AdvancedLinuxPanel extends SettingsPanel {

	public AdvancedLinuxPanel( XenonProgramProduct product ) {
		super( product );

		// Add the title to the panel
		addTitle( Rb.text( product, RbKey.SETTINGS, "advanced-linux" ) );

		// Create the PkExec group pane
		TitledPane pkexecPane = createGroupPane( Rb.text( product, RbKey.SETTINGS, "advanced-linux-pkexec" ) );
		getChildren().add( pkexecPane );
		GridPane pkexecGrid = (GridPane)pkexecPane.getContent();
		int row = 0;

		String installedString = Rb.text( product, RbKey.LABEL, "installed" );
		String notInstalledString = Rb.text( product, RbKey.LABEL, "not-installed" );
		boolean isPkExecInstalled = isPkExecInstalled();
		String pkexecInstalledString = isPkExecInstalled ? installedString : notInstalledString;

		Label pkexecExplanation = new Label( Rb.text( product, RbKey.SETTINGS, "advanced-linux-pkexec-explanation", pkexecInstalledString ) );
		pkexecExplanation.getStyleClass().addAll( "settings-infoarea" );
		GridPane.setColumnSpan( pkexecExplanation, GridPane.REMAINING );
		pkexecGrid.addRow( row++, pkexecExplanation );

		if( !isPkExecInstalled ) {
			Label pkexecAssist = new Label( Rb.text( product, RbKey.SETTINGS, "advanced-linux-pkexec-assist", getProgram().getCard().getName() ) );
			pkexecAssist.getStyleClass().addAll( "settings-infoarea" );
			GridPane.setColumnSpan( pkexecAssist, GridPane.REMAINING );
			pkexecGrid.addRow( row, pkexecAssist );

			// TODO Would you like help installing pkexec?
		}

		// hidpi
		if( ProgramChecks.isHiDpiCapable() ) {
			// Create the HiDPI group pane
			TitledPane hidpiPane = createGroupPane( Rb.text( product, RbKey.SETTINGS, "advanced-linux-hidpi" ) );
			getChildren().add( hidpiPane );
			GridPane hidpiGrid = (GridPane)hidpiPane.getContent();
			row = 0;

			Screen primary = Screen.getPrimary();
			double dpi = primary.getDpi();
			double width = primary.getBounds().getWidth();
			double height = primary.getBounds().getHeight();
			double scale = primary.getOutputScaleX();

			Label hidpiExplanation = new Label( Rb.text( product, RbKey.SETTINGS, "advanced-linux-hidpi-explanation", width, height, dpi, scale ) );
			hidpiExplanation.getStyleClass().addAll( "settings-infoarea" );
			GridPane.setColumnSpan( hidpiExplanation, GridPane.REMAINING );
			hidpiGrid.addRow( row++, hidpiExplanation );

			if( !ProgramChecks.isHiDpiEnabled() ) {
				Label hidpiAssist = new Label( Rb.text( product, RbKey.SETTINGS, "advanced-linux-hidpi-assist", getProgram().getCard().getName() ) );
				hidpiAssist.getStyleClass().addAll( "settings-infoarea" );
				GridPane.setColumnSpan( hidpiAssist, GridPane.REMAINING );
				hidpiGrid.addRow( row, hidpiAssist );
			}

			// TODO Would you like help installing configuring HiDPI?
		}

		// memory, or put in OS advanced common settings?
	}

	private boolean isPkExecInstalled() {
		return Files.exists( Paths.get( "/usr/bin/pkexec" ) );
	}

}
