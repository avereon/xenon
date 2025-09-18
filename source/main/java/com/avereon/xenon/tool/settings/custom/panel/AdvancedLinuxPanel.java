package com.avereon.xenon.tool.settings.custom.panel;

import com.avereon.product.Rb;
import com.avereon.xenon.ProgramChecks;
import com.avereon.xenon.RbKey;
import com.avereon.xenon.XenonProgramProduct;
import com.avereon.xenon.tool.settings.SettingsPanel;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.GridPane;

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

		Label pkexecExplanation = new Label( Rb.text( product, RbKey.SETTINGS, "advanced-linux-pkexec-explanation" ) );
		pkexecExplanation.getStyleClass().addAll( "settings-infoarea" );
		GridPane.setColumnSpan( pkexecExplanation, GridPane.REMAINING );
		pkexecGrid.addRow( row++, pkexecExplanation );

		pkexecGrid.addRow( row++, createBlankLine() );

		boolean pkexecInstalled = ProgramChecks.isPkExecInstalled();
		String status = pkexecInstalled ? "success" : "warning";
		String installedString = Rb.text( product, RbKey.LABEL, pkexecInstalled ? "installed" : "not-installed" );
		Control pkexecStatus = createStatusLine( installedString, status );
		pkexecGrid.addRow( row++, pkexecStatus );

		if( !pkexecInstalled ) {
			pkexecGrid.addRow( row++, createBlankLine() );
			Label pkexecAssist = new Label( Rb.text( product, RbKey.SETTINGS, "advanced-linux-pkexec-assist", getProgram().getCard().getName() ) );
			pkexecAssist.getStyleClass().addAll( "settings-infoarea" );
			GridPane.setColumnSpan( pkexecAssist, GridPane.REMAINING );
			pkexecGrid.addRow( row++, pkexecAssist );

			pkexecGrid.addRow( row++, createBlankLine() );

			String debianInstall = "sudo apt install pkexec";
			String rpmInstall = "sudo yum install pkexec";
			String archInstall = "sudo pacman -S pkexec";

			TextField pkexecInstall = new TextField( debianInstall );
			pkexecInstall.setEditable( false );
			pkexecInstall.getStyleClass().addAll( "code" );
			//pkexecInstall.prefWidthProperty().bind( widthProperty() );
			GridPane.setColumnSpan( pkexecInstall, GridPane.REMAINING );
			pkexecGrid.addRow( row++, pkexecInstall );
			// TODO Would you like help installing pkexec?
		}

		// hidpi
		if( ProgramChecks.isHiDpiCapable() ) {
			// Create the HiDPI group pane
			TitledPane hidpiPane = createGroupPane( Rb.text( product, RbKey.SETTINGS, "advanced-linux-hidpi" ) );
			getChildren().add( hidpiPane );
			GridPane hidpiGrid = (GridPane)hidpiPane.getContent();
			row = 0;

			//			Screen primary = Screen.getPrimary();
			//			double dpi = primary.getDpi();
			//			double width = primary.getBounds().getWidth();
			//			double height = primary.getBounds().getHeight();
			//			double scale = primary.getOutputScaleX();

			Label hidpiExplanation = new Label( Rb.text( product, RbKey.SETTINGS, "advanced-linux-hidpi-explanation" ) );
			hidpiExplanation.getStyleClass().addAll( "settings-infoarea" );
			GridPane.setColumnSpan( hidpiExplanation, GridPane.REMAINING );
			hidpiGrid.addRow( row++, hidpiExplanation );

			hidpiGrid.addRow( row++, createBlankLine() );

			// TODO Status line
			boolean hidpiEnabled = ProgramChecks.isHiDpiEnabled();
			String hidpiStatus = hidpiEnabled ? "success" : "warning";
			Control hidpiStatusLine = createStatusLine( Rb.text( product, RbKey.LABEL, hidpiEnabled ? "enabled" : "disabled" ), hidpiStatus );
			hidpiGrid.addRow( row++, hidpiStatusLine );

			if( !hidpiEnabled ) {
				hidpiGrid.addRow( row++, createBlankLine() );
				Label hidpiAssist = new Label( Rb.text( product, RbKey.SETTINGS, "advanced-linux-hidpi-assist", getProgram().getCard().getName() ) );
				hidpiAssist.getStyleClass().addAll( "settings-infoarea" );
				GridPane.setColumnSpan( hidpiAssist, GridPane.REMAINING );
				hidpiGrid.addRow( row, hidpiAssist );
			}

			// TODO Would you like help installing configuring HiDPI?
		}

		// memory, or put in OS advanced common settings?
	}

}
