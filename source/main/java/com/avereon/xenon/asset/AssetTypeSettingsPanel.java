package com.avereon.xenon.asset;

import com.avereon.xenon.tool.settings.SettingsPage;
import com.avereon.xenon.tool.settings.SettingsPagePanel;

public class AssetTypeSettingsPanel extends SettingsPagePanel {

	public AssetTypeSettingsPanel( SettingsPage page ) {
		super( page );
	}

	public AssetTypeSettingsPanel( SettingsPage page, boolean showTitle ) {
		super( page, showTitle );

		addTitle("MVS Asset Type Management");
	}
}
