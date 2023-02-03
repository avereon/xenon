package com.avereon.xenon.asset;

import com.avereon.settings.SettingsEvent;
import com.avereon.xenon.ProgramProduct;
import com.avereon.xenon.tool.settings.SettingData;
import com.avereon.xenon.tool.settings.SettingEditor;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;

import java.util.Set;

public class AssetTypeSettingEditor extends SettingEditor {

	private final Label label = new Label( "Asset Type Setting Editor" );

	public AssetTypeSettingEditor( ProgramProduct product, String rbKey, SettingData setting ) {
		super( product, rbKey, setting );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addComponents( GridPane pane, int row ) {
		pane.addRow( row, label );
	}

	@Override
	protected Set<Node> getComponents() {
		return Set.of( label );
	}

	@Override
	protected void doSettingValueChanged( SettingsEvent event ) {

	}
}
