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

public class ResourceTypeToolAssociationList extends VBox {

	@Getter
	private final XenonProgramProduct product;

	private final GridPane items;

	@Getter
	private ResourceType resourceType;

	public ResourceTypeToolAssociationList( XenonProgramProduct product ) {
		this.product = product;
		this.items = new GridPane();
		this.items.setHgap( UiFactory.PAD );
		this.items.setVgap( UiFactory.PAD );
		getChildren().setAll( items );
	}

	public Xenon getProgram() {
		return product.getProgram();
	}

	public void setResourceType( ResourceType resourceType ) {
		this.resourceType = resourceType;
		if( resourceType != null ) {
			Settings settings = getProgram().getSettingsManager().getAssetTypeSettings( resourceType ).getNode( "default" );
			settings.register( "tool", e -> this.update() );
		}
		update();
	}

	private void update() {
		items.getChildren().clear();
		if( resourceType == null ) return;

		// Reload the asset type associations
		int row = 0;

		Class<? extends ProgramTool> defaultTool = getProgram().getToolManager().getDefaultTool( resourceType );

		for( Class<? extends ProgramTool> tool : resourceType.getRegisteredTools() ) {
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
		getProgram().getToolManager().setDefaultTool( resourceType, tool );
	}

}
