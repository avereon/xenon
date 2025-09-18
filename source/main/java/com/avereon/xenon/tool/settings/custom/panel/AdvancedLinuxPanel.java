package com.avereon.xenon.tool.settings.custom.panel;

import com.avereon.product.Rb;
import com.avereon.util.OperatingSystem;
import com.avereon.xenon.ProgramChecks;
import com.avereon.xenon.RbKey;
import com.avereon.xenon.XenonProgramProduct;
import com.avereon.xenon.tool.settings.SettingsPanel;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

public class AdvancedLinuxPanel extends SettingsPanel {

	public AdvancedLinuxPanel( XenonProgramProduct product ) {
		super( product );

		// Title
		addTitle( Rb.text( product, RbKey.SETTINGS, "advanced-linux" ) );

		// JVM Memory setup?, or put in OS advanced common settings?
		createJvmMemoryGroup( product );

		// HiDPI
		if( ProgramChecks.isHiDpiCapable() ) createHiDpiGroup( product );

		// PkExec
		if( OperatingSystem.isLinux() ) createPkExecGroup( product );
	}

	/**
	 * Create the JVM Memory group pane
	 *
	 * @param product The product
	 */
	private void createJvmMemoryGroup( XenonProgramProduct product ) {
		TitledPane jvmMemoryPane = createGroupPane( Rb.text( product, RbKey.SETTINGS, "advanced-jvm-memory" ) );
		getChildren().add( jvmMemoryPane );
		GridPane jvmMemoryGrid = (GridPane)jvmMemoryPane.getContent();
		int row = 0;

		// Explanation
		Label jvmMemoryExplanation = createInfoArea( Rb.text( product, RbKey.SETTINGS, "advanced-jvm-memory-explanation" ) );
		jvmMemoryGrid.addRow( row++, jvmMemoryExplanation );

		jvmMemoryGrid.addRow( row++, createBlankLine() );

		// NEXT Use a small grid pane to organize the settings?
		// minimum setting
		// maximum setting
		// FIXME How do the settings get saved before the restart?

		jvmMemoryGrid.addRow( row++, createBlankLine() );

		// restart button
		jvmMemoryGrid.addRow( row++, createActionButton( "restart" ) );
	}

	/**
	 * Create the HiDPI group pane
	 *
	 * @param product The product
	 */
	private void createHiDpiGroup( XenonProgramProduct product ) {
		TitledPane hidpiPane = createGroupPane( Rb.text( product, RbKey.SETTINGS, "advanced-linux-hidpi" ) );
		getChildren().add( hidpiPane );
		GridPane hidpiGrid = (GridPane)hidpiPane.getContent();
		int row = 0;

		//			Screen primary = Screen.getPrimary();
		//			double dpi = primary.getDpi();
		//			double width = primary.getBounds().getWidth();
		//			double height = primary.getBounds().getHeight();
		//			double scale = primary.getOutputScaleX();

		// Explanation
		Label hidpiExplanation = createInfoArea( Rb.text( product, RbKey.SETTINGS, "advanced-linux-hidpi-explanation" ) );
		hidpiGrid.addRow( row++, hidpiExplanation );

		hidpiGrid.addRow( row++, createBlankLine() );

		// Status line
		boolean hidpiEnabled = ProgramChecks.isHiDpiEnabled();
		String hidpiStatus = hidpiEnabled ? "success" : "warning";
		Control hidpiStatusLine = createStatusLine( Rb.text( product, RbKey.LABEL, hidpiEnabled ? "enabled" : "disabled" ), hidpiStatus );
		hidpiGrid.addRow( row++, hidpiStatusLine );

		// TODO Add the ability to ignore this issue

		// Assist message
		if( !hidpiEnabled ) {
			hidpiGrid.addRow( row++, createBlankLine() );
			Label hidpiAssist = new Label( Rb.text( product, RbKey.SETTINGS, "advanced-linux-hidpi-assist", getProgram().getCard().getName() ) );
			hidpiAssist.getStyleClass().addAll( "settings-infoarea" );
			GridPane.setColumnSpan( hidpiAssist, GridPane.REMAINING );
			hidpiGrid.addRow( row++, hidpiAssist );

			hidpiGrid.addRow( row++, createBlankLine() );

			String gsettings = "gsettings set org.gnome.desktop.interface scaling-factor 2";

			TextField configuration = new TextField( gsettings );
			configuration.setEditable( false );
			configuration.getStyleClass().addAll( "code" );
			GridPane.setColumnSpan( configuration, GridPane.REMAINING );
			hidpiGrid.addRow( row++, configuration );
		}

	}

	/**
	 * Create the PkExec group pane
	 *
	 * @param product The product
	 */
	private void createPkExecGroup( XenonProgramProduct product ) {
		TitledPane pkexecPane = createGroupPane( Rb.text( product, RbKey.SETTINGS, "advanced-linux-pkexec" ) );
		getChildren().add( pkexecPane );
		GridPane pkexecGrid = (GridPane)pkexecPane.getContent();
		int row = 0;

		// Explanation
		Label pkexecExplanation = new Label( Rb.text( product, RbKey.SETTINGS, "advanced-linux-pkexec-explanation" ) );
		pkexecExplanation.getStyleClass().addAll( "settings-infoarea" );
		GridPane.setColumnSpan( pkexecExplanation, GridPane.REMAINING );
		pkexecGrid.addRow( row++, pkexecExplanation );

		pkexecGrid.addRow( row++, createBlankLine() );

		// Status line
		boolean pkexecInstalled = ProgramChecks.isPkExecInstalled();
		String status = pkexecInstalled ? "success" : "warning";
		String installedString = Rb.text( product, RbKey.LABEL, pkexecInstalled ? "installed" : "not-installed" );
		Control pkexecStatus = createStatusLine( installedString, status );
		pkexecGrid.addRow( row++, pkexecStatus );

		// TODO Add the ability to ignore this issue

		// Assist message
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

			TextField installation = new TextField( debianInstall );
			installation.setEditable( false );
			installation.getStyleClass().addAll( "code" );
			GridPane.setColumnSpan( installation, GridPane.REMAINING );
			pkexecGrid.addRow( row++, installation );
		}
	}

}
