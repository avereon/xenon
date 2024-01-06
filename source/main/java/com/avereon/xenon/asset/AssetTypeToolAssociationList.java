package com.avereon.xenon.asset;

import com.avereon.xenon.ProgramTool;
import com.avereon.xenon.UiFactory;
import com.avereon.xenon.XenonProgramProduct;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import lombok.Getter;

public class AssetTypeToolAssociationList extends VBox {

	private final GridPane items;

	@Getter
	private AssetType assetType;

	public AssetTypeToolAssociationList( XenonProgramProduct product) {
		this.items = new GridPane();
		this.items.setHgap( UiFactory.PAD );
		this.items.setVgap( UiFactory.PAD );
		getChildren().setAll( items );
	}

	public void setAssetType( AssetType assetType ) {
		this.assetType = assetType;
		update();
	}

	private void update() {
		items.getChildren().clear();
		if( assetType == null ) return;

		// Reload the asset type associations
		int row = 0;

		for( Class<? extends ProgramTool> tool : assetType.getRegisteredTools() ) {
			items.addRow( row++, new Label(tool.getSimpleName()) );
		}
	}

}
