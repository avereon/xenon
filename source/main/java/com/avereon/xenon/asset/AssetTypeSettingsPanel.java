package com.avereon.xenon.asset;

import com.avereon.xenon.tool.settings.SettingsPage;
import com.avereon.xenon.tool.settings.SettingsPagePanel;
import javafx.scene.control.TitledPane;

/**
 * <p>
 * This settings panel is used to manage the relationships between media types
 * (Asset Types) and other resources like tools.
 * </p>
 * <p>
 * The settings define the differences between the default configuration and
 * what the user chooses.
 * </p>
 */
public class AssetTypeSettingsPanel extends SettingsPagePanel {

	public AssetTypeSettingsPanel( SettingsPage page ) {
		super( page );


		// TODO Get title from RB
		addTitle("MVS Asset Type Management");

		// TODO Get group name from RB
		TitledPane pane = createGroupPane( "Group Name" );
		getChildren().add( pane );

		// Add things to the group content

	}

}
