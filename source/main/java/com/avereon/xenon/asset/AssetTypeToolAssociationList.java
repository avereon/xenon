package com.avereon.xenon.asset;

import com.avereon.settings.Settings;
import com.avereon.xenon.*;
import javafx.geometry.HPos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import lombok.Getter;

public class AssetTypeToolAssociationList extends VBox {

	@Getter
	private final XenonProgramProduct product;

	private final GridPane items;

	@Getter
	private AssetType assetType;

	public AssetTypeToolAssociationList( XenonProgramProduct product ) {
		this.product = product;
		this.items = new GridPane();
		this.items.setHgap( UiFactory.PAD );
		this.items.setVgap( UiFactory.PAD );
		getChildren().setAll( items );
	}

	public Xenon getProgram() {
		return product.getProgram();
	}

	public void setAssetType( AssetType assetType ) {
		this.assetType = assetType;
		if( assetType != null ) {
			Settings settings = getProgram().getSettingsManager().getAssetTypeSettings( assetType ).getNode( "default" );
			settings.register( "tool", e -> this.update() );
		}
		update();
	}

	private void update() {
		items.getChildren().clear();
		if( assetType == null ) return;

		// Reload the asset type associations
		int row = 0;

		Class<? extends ProgramTool> defaultTool = getProgram().getToolManager().getDefaultTool( assetType );

		for( Class<? extends ProgramTool> tool : assetType.getRegisteredTools() ) {
			boolean isDefault = tool == defaultTool;

			// Tool label
			ToolRegistration registration = getProgram().getToolManager().getToolRegistration( tool );
			Label toolName = new Label( registration.getName() );
			toolName.getStyleClass().add( isDefault ? "asset-type-settings-default-tool" : "asset-type-settings-normal-tool" );
			GridPane.setHgrow( toolName, Priority.ALWAYS );

			// Default tool icon
			Label defaultIcon = new Label( null, getProgram().getIconLibrary().getIcon( "check" ) );
			GridPane.setHalignment( defaultIcon, HPos.CENTER );

			// Default tool button
			Button defaultToolButton = new Button( null, getProgram().getIconLibrary().getIcon( "up" ) );
			defaultToolButton.setOnAction( e -> this.setDefaultTool( tool ) );

			items.addRow( row++, toolName, isDefault ? defaultIcon : defaultToolButton );
		}
	}

	private void setDefaultTool( Class<? extends ProgramTool> tool ) {
		getProgram().getToolManager().setDefaultTool( assetType, tool );
	}

}
