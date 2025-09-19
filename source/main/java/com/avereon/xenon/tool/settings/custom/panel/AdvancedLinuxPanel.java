package com.avereon.xenon.tool.settings.custom.panel;

import com.avereon.product.Rb;
import com.avereon.util.AppConfig;
import com.avereon.util.OperatingSystem;
import com.avereon.xenon.*;
import com.avereon.xenon.tool.settings.SettingsPanel;
import javafx.collections.FXCollections;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import lombok.CustomLog;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@CustomLog
public class AdvancedLinuxPanel extends SettingsPanel {

	public AdvancedLinuxPanel( XenonProgramProduct product ) {
		super( product );

		// Title
		addTitle( Rb.text( product, RbKey.SETTINGS, "advanced-linux" ) );

		// JVM Heap
		createJvmHeapGroup( product );

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
	private void createJvmHeapGroup( XenonProgramProduct product ) {
		TitledPane jvmMemoryPane = createGroupPane( Rb.text( product, RbKey.SETTINGS, "advanced-jvm-heap" ) );
		getChildren().add( jvmMemoryPane );
		GridPane jvmMemoryGrid = (GridPane)jvmMemoryPane.getContent();
		int row = 0;

		// Explanation
		Label jvmMemoryExplanation = createInfoArea( Rb.text( product, RbKey.SETTINGS, "advanced-jvm-heap-explanation" ) );
		jvmMemoryGrid.addRow( row++, jvmMemoryExplanation );

		jvmMemoryGrid.addRow( row++, createBlankLine() );

		// Automatic/Manual ComboBoxes
		String automaticText = Rb.text( product, RbKey.LABEL, "automatic" );
		String manualText = Rb.text( product, RbKey.LABEL, "manual" );
		ComboBox<String> jvmMemoryMinModeComboBox = new ComboBox<>( FXCollections.observableList( new ArrayList<>( List.of( automaticText, manualText ) ) ) );
		ComboBox<String> jvmMemoryMaxModeComboBox = new ComboBox<>( FXCollections.observableList( new ArrayList<>( List.of( automaticText, manualText ) ) ) );
		jvmMemoryMinModeComboBox.getSelectionModel().select( 0 );
		jvmMemoryMaxModeComboBox.getSelectionModel().select( 0 );

		// Memory Fields
		Label minMemoryLabel = new Label( Rb.text( product, RbKey.SETTINGS, "advanced-jvm-heap-min" ) );
		Label maxMemoryLabel = new Label( Rb.text( product, RbKey.SETTINGS, "advanced-jvm-heap-max" ) );
		TextField minMemoryField = new TextField( "" );
		TextField maxMemoryField = new TextField( "" );

		// Unit ComboBoxes
		ComboBox<String> minUnitComboBox = new ComboBox<>( FXCollections.observableList( new ArrayList<>( AppConfig.HEAP_UNITS ) ) );
		ComboBox<String> maxUnitComboBox = new ComboBox<>( FXCollections.observableList( new ArrayList<>( AppConfig.HEAP_UNITS ) ) );
		minUnitComboBox.getSelectionModel().select( 0 );
		maxUnitComboBox.getSelectionModel().select( 0 );

		// Setting grid pane
		GridPane memorySettings = new GridPane( UiFactory.PAD, UiFactory.PAD );
		memorySettings.addRow( 0, minMemoryLabel, jvmMemoryMinModeComboBox, minMemoryField, minUnitComboBox );
		memorySettings.addRow( 1, maxMemoryLabel, jvmMemoryMaxModeComboBox, maxMemoryField, maxUnitComboBox );
		GridPane.setColumnSpan( memorySettings, GridPane.REMAINING );
		jvmMemoryGrid.addRow( row++, memorySettings );

		jvmMemoryGrid.addRow( row++, createBlankLine() );

		String appName = getProgram().getCard().getName();
		Path appConfigPath = product.getProgram().getHomeFolder().resolve( "lib/app/" + appName + ".cfg" );
		if( Objects.equals( getProgram().getMode(), XenonMode.DEV ) ) appConfigPath = Path.of( "/opt/xenon/lib/app/" + appName + ".cfg" );
		AppConfig appConfig = AppConfig.of( appConfigPath );
		jvmMemoryGrid.addRow(
			row++, createActionButton(
				"save", "Save", () -> {
					try {
						appConfig.save();
					} catch( IOException exception ) {
						log.atError().withCause( exception ).log( "Failed to save application configuration", exception );
					}
				}
			)
		);

		// restart button
		jvmMemoryGrid.addRow( row++, createActionButton( "restart" ) );

		try {
			appConfig.load();

			if( appConfig.getJvmHeapMin() > 0 ) {
				jvmMemoryMinModeComboBox.getSelectionModel().select( manualText );
				minMemoryField.setText( String.valueOf( appConfig.getJvmHeapMin() ) );
				String unit = appConfig.getJvmHeapMinUnit().toUpperCase();
				if( unit.isEmpty() ) unit = AppConfig.HEAP_UNITS.getFirst();
				minUnitComboBox.getSelectionModel().select( unit );
			} else {
				jvmMemoryMinModeComboBox.getSelectionModel().select( automaticText );
			}

			if( appConfig.getJvmHeapMax() > 0 ) {
				jvmMemoryMaxModeComboBox.getSelectionModel().select( manualText );
				maxMemoryField.setText( String.valueOf( appConfig.getJvmHeapMax() ) );
				String unit = appConfig.getJvmHeapMaxUnit().toUpperCase();
				if( unit.isEmpty() ) unit = AppConfig.HEAP_UNITS.getFirst();
				maxUnitComboBox.getSelectionModel().select( unit );
			} else {
				jvmMemoryMaxModeComboBox.getSelectionModel().select( automaticText );
			}
		} catch( IOException e ) {
			log.atWarn().withCause( e ).log( "Failed to load app config" );
		}
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
