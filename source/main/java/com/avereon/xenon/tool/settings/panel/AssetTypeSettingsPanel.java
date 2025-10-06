package com.avereon.xenon.tool.settings.panel;

import com.avereon.product.Rb;
import com.avereon.xenon.RbKey;
import com.avereon.xenon.XenonProgramProduct;
import com.avereon.xenon.asset.ResourceType;
import com.avereon.xenon.asset.AssetTypeCodecAssociationList;
import com.avereon.xenon.asset.AssetTypeToolAssociationList;
import com.avereon.xenon.compare.AssetTypeNameComparator;
import com.avereon.xenon.tool.settings.SettingsPanel;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

import java.util.List;

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
public class AssetTypeSettingsPanel extends SettingsPanel {

	private static final String ASSET_TYPE_DEFAULT_TOOL = "/asset-types/{type-key}/default/tool";

	private final GridPane assetTypeGrid;

	private final Label assetTypesLabel;

	private final ComboBox<ResourceType> assetTypes;

	private final Label keyLabel;

	private final Label key;

	private final Label nameLabel;

	private final Label name;

	private final Label descriptionLabel;

	private final Label description;

	private final AssetTypeCodecAssociationList associations;

	private final AssetTypeToolAssociationList toolRegistrations;

	public AssetTypeSettingsPanel(XenonProgramProduct product ) {
		super( product );

		// Add the title to the panel
		addTitle( Rb.text( product, RbKey.SETTINGS, "asset-types" ) );

		// Create the asset type group pane
		TitledPane pane = createGroupPane( Rb.text( product, RbKey.SETTINGS, "asset-type" ) );
		getChildren().add( pane );
		assetTypeGrid = (GridPane)pane.getContent();
		int row = 0;

		// Asset type selector
		assetTypesLabel = new Label( Rb.text( product, RbKey.SETTINGS, "asset-type" ) + ":" );
		assetTypes = new ComboBox<>();
		assetTypes.getItems().setAll( getUserAssetTypes( product ) );
		GridPane.setColumnSpan( assetTypes, GridPane.REMAINING );
		assetTypeGrid.addRow( row++, assetTypesLabel, assetTypes );

		// Asset type name
		nameLabel = new Label( Rb.text( product, RbKey.LABEL, "name" ) );
		name = new Label();
		GridPane.setColumnSpan( name, GridPane.REMAINING );
		assetTypeGrid.addRow( row++, nameLabel, name );

		// Asset type description
		descriptionLabel = new Label( Rb.text( product, RbKey.LABEL, "description" ) );
		description = new Label();
		GridPane.setColumnSpan( description, GridPane.REMAINING );
		assetTypeGrid.addRow( row++, descriptionLabel, description );

		// Asset type key
		keyLabel = new Label( Rb.text( product, RbKey.LABEL, "key" ) );
		key = new Label();
		GridPane.setColumnSpan( key, GridPane.REMAINING );
		assetTypeGrid.addRow( row++, keyLabel, key );

		// Add a spacer row
		assetTypeGrid.addRow( row++ );

		// Create the codec associations group pane
		TitledPane codecAssocPane = createGroupPane( Rb.text( product, RbKey.SETTINGS, "asset-type-codec-associations" ) );
		getChildren().add( codecAssocPane );
		GridPane codecAssocGrid = (GridPane)codecAssocPane.getContent();
		row = 0;

		// Codec associations
		associations = new AssetTypeCodecAssociationList(product);
		associations.prefWidthProperty().bind( codecAssocGrid.widthProperty() );
		GridPane.setColumnSpan( associations, GridPane.REMAINING );
		GridPane.setHgrow( associations, Priority.ALWAYS );
		codecAssocGrid.addRow( row++, associations );

		// Create the codec associations group pane
		TitledPane toolAssocPane = createGroupPane( Rb.text( product, RbKey.SETTINGS, "asset-type-tool-associations" ) );
		getChildren().add( toolAssocPane );
		GridPane toolAssocGrid = (GridPane)toolAssocPane.getContent();
		row = 0;

		// Tool associations
		toolRegistrations = new AssetTypeToolAssociationList( product );
		toolRegistrations.prefWidthProperty().bind( toolAssocGrid.widthProperty() );
		GridPane.setColumnSpan( toolRegistrations, GridPane.REMAINING );
		GridPane.setHgrow( toolRegistrations, Priority.ALWAYS );
		toolAssocGrid.addRow( row++, toolRegistrations );

		assetTypes.valueProperty().addListener( ( p, o, n ) -> doUpdateFields( n.getKey() ) );
		assetTypes.getSelectionModel().select( 0 );
	}

	private void doUpdateFields( String typeKey ) {
		ResourceType type = getProduct().getProgram().getAssetManager().getAssetType( typeKey );

		key.setText( type == null ? "" : type.getKey() );
		name.setText( type == null ? "" : type.getName() );
		description.setText( type == null ? "" : type.getDescription() );

		associations.setResourceType( type );
		toolRegistrations.setResourceType( type );

//		if( type == null ) return;`
//
//		// The default tool
//		List<Class<? extends ProgramTool>> toolClasses = getProduct().getProgram().getToolManager().getRegisteredTools( type );
//		Class<? extends ProgramTool> toolClass = getProduct().getProgram().getToolManager().getDefaultTool( type );
//
//		type.getCodecs();
//		Codec defaultCodec = type.getDefaultCodec();
//		type.getIcon();
//
//		type.getCodecs().forEach( c -> {
//			//System.out.printf( "%s%n", c.getName() );
//			Arrays.stream( Codec.Pattern.values() ).forEach( p -> {
//				//System.out.printf( "  %s%n", p );
//				Set<String> supported = c.getSupported( p );
//				supported.forEach( s -> {
//					//System.out.printf( "    %s%n", s );
//				} );
//			} );
//		} );
//
//		//System.out.printf( "  %s%n", "TOOL" );
//		toolClasses.forEach( c -> {
//			boolean isDefault = toolClass == c;
//			//System.out.printf( "    %s %s%n", c.getName(), isDefault ? "*" : "" );
//		} );
//
//		//defaultCodec.getSupported( Codec.Pattern.EXTENSION );
	}

	private List<ResourceType> getUserAssetTypes( XenonProgramProduct product ) {
		return product.getProgram().getAssetManager().getAssetTypes().stream().filter( ResourceType::isUserType ).sorted( new AssetTypeNameComparator() ).toList();
	}

	private List<ResourceType> getAssetTypes( XenonProgramProduct product ) {
		return product.getProgram().getAssetManager().getAssetTypes().stream().sorted( new AssetTypeNameComparator() ).toList();
	}

}
