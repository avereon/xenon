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

	private final Label key;

	private final Label name;

	public AssetTypeSettingEditor( ProgramProduct product, String rbKey, SettingData setting ) {
		super( product, rbKey, setting );

		key = new Label();
		name = new Label();

		doUpdateFields(setting.getSettings().get( getKey(), String.class, "" ));
	}

	private void doUpdateFields( String typeKey ) {
		AssetType type = getProduct().getProgram().getAssetManager().getAssetType( typeKey );
		key.setText( type == null ? "" : type.getKey() );
		name.setText( type == null ? "" : type.getName() );

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addComponents( GridPane pane, int row ) {
		GridPane grid = new GridPane();

		int index = 0;
		grid.addRow( index++, label, key, name );

		GridPane.setColumnSpan( grid, GridPane.REMAINING );

		pane.addRow( row, grid );
	}

	@Override
	protected Set<Node> getComponents() {
		return Set.of( label, key, name );
	}

	@Override
	protected void doSettingValueChanged( SettingsEvent event ) {
		doUpdateFields( String.valueOf( event.getNewValue() ) );
	}
}
