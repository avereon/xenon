package com.avereon.xenon.asset;

import com.avereon.product.Rb;
import com.avereon.settings.SettingsEvent;
import com.avereon.xenon.ProgramTool;
import com.avereon.xenon.RbKey;
import com.avereon.xenon.UiFactory;
import com.avereon.xenon.XenonProgramProduct;
import com.avereon.xenon.compare.AssetTypeNameComparator;
import com.avereon.xenon.tool.settings.SettingData;
import com.avereon.xenon.tool.settings.SettingEditor;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * <p>
 * This settings editor is used to manage the relationships between media types
 * (Asset Types) and other resources like tools.
 * </p>
 * <p>
 * The settings define the differences between the default configuration and
 * what the user chooses.
 * </p>
 *
 * @deprecated In favor of {@link AssetTypeSettingsPanel}
 */
@Deprecated
public class AssetTypeSettingEditor extends SettingEditor {

	private final Label assetTypesLabel;

	private final ComboBox<AssetType> assetTypes;

	private final Label keyLabel;

	private final Label key;

	private final Label nameLabel;

	private final Label name;

	private final Label descriptionLabel;

	private final Label description;

	private final Label matchesLabel;
	private final AssetTypeCodecAssociationList associations;

	private final Label toolsLabel;
	private final AssetTypeToolAssociationList toolRegistrations;

	private final GridPane assetTypeGrid;

	//private final GridPane assetTypeReference;

	public AssetTypeSettingEditor( XenonProgramProduct product, String rbKey, SettingData setting ) {
		super( product, rbKey, setting );

		int row = 0;
		assetTypeGrid = new GridPane();
		assetTypeGrid.setHgap( UiFactory.PAD );
		assetTypeGrid.setVgap( UiFactory.PAD );

		assetTypesLabel = new Label( Rb.text( product, RbKey.SETTINGS, "asset-types" ) + ":" );
		assetTypes = new ComboBox<>();
		assetTypes.getItems().setAll( getAssetTypes( product ) );
		GridPane.setColumnSpan( assetTypes, GridPane.REMAINING );
		assetTypeGrid.addRow( row++, assetTypesLabel, assetTypes );

		nameLabel = new Label( Rb.text( product, RbKey.LABEL, "name" ) );
		name = new Label();
		GridPane.setColumnSpan( name, GridPane.REMAINING );
		assetTypeGrid.addRow( row++, nameLabel, name );

		descriptionLabel = new Label( Rb.text( product, RbKey.LABEL, "description" ) );
		description = new Label();
		GridPane.setColumnSpan( description, GridPane.REMAINING );
		assetTypeGrid.addRow( row++, descriptionLabel, description );

		keyLabel = new Label( Rb.text( product, RbKey.LABEL, "key" ) );
		key = new Label();
		GridPane.setColumnSpan( key, GridPane.REMAINING );
		assetTypeGrid.addRow( row++, keyLabel, key );

		// Add a spacer row
		assetTypeGrid.addRow( row++ );

		matchesLabel = new Label( "Matches" );
		assetTypeGrid.addRow( row++, matchesLabel );

		associations = new AssetTypeCodecAssociationList(product);
		associations.prefWidthProperty().bind( assetTypeGrid.widthProperty() );
		GridPane.setColumnSpan( associations, GridPane.REMAINING );
		GridPane.setHgrow( associations, Priority.ALWAYS );
		assetTypeGrid.addRow( row++, associations );

		toolsLabel = new Label( "Tools" );
		assetTypeGrid.addRow( row++, toolsLabel );

		toolRegistrations = new AssetTypeToolAssociationList( product );
		toolRegistrations.prefWidthProperty().bind( assetTypeGrid.widthProperty() );
		GridPane.setColumnSpan( toolRegistrations, GridPane.REMAINING );
		GridPane.setHgrow( toolRegistrations, Priority.ALWAYS );
		assetTypeGrid.addRow( row++, toolRegistrations );

		// NEXT Continue work on AssetTypeSettingEditor

		assetTypes.valueProperty().addListener( ( p, o, n ) -> doUpdateFields( n.getKey() ) );
		assetTypes.getSelectionModel().select( 0 );
	}

	private void doUpdateFields( String typeKey ) {
		AssetType type = getProduct().getProgram().getAssetManager().getAssetType( typeKey );
		key.setText( type == null ? "" : type.getKey() );
		name.setText( type == null ? "" : type.getName() );
		description.setText( type == null ? "" : type.getDescription() );

		associations.setAssetType( type );
		toolRegistrations.setAssetType( type );

		if( type == null ) return;

		// The default tool
		List<Class<? extends ProgramTool>> toolClasses = getProduct().getProgram().getToolManager().getRegisteredTools( type );
		Class<? extends ProgramTool> toolClass = getProduct().getProgram().getToolManager().getDefaultTool( type );

		type.getCodecs();
		Codec defaultCodec = type.getDefaultCodec();
		type.getIcon();

		type.getCodecs().forEach( c -> {
			System.out.printf( "%s%n", c.getName() );
			Arrays.stream( Codec.Pattern.values() ).forEach( p -> {
				System.out.printf( "  %s%n", p );
				Set<String> supported = c.getSupported( p );
				supported.forEach( s -> {
					System.out.printf( "    %s%n", s );
				} );
			} );
		} );

		System.out.printf( "  %s%n", "TOOL" );
		toolClasses.forEach( c -> {
			boolean isDefault = toolClass == c;
			System.out.printf( "    %s %s%n", c.getName(), isDefault ? "*" : "" );
		} );

		//defaultCodec.getSupported( Codec.Pattern.EXTENSION );

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addComponents( GridPane pane, int row ) {
		//		GridPane grid = new GridPane();
		//
		//		int index = 0;
		////		grid.addRow( index++, key );
		////		grid.addRow( index++, name );
		//		grid.addRow( index++, assetTypeTable );

		GridPane.setColumnSpan( assetTypeGrid, GridPane.REMAINING );
		GridPane.setHgrow( assetTypeGrid, Priority.ALWAYS );
		pane.addRow( row, assetTypeGrid );

		//		GridPane.setColumnSpan( assetTypeReference, GridPane.REMAINING );
		//		GridPane.setRowSpan( assetTypeReference, GridPane.REMAINING );
		//		pane.addRow( row + 1, assetTypeReference );
	}

	@Override
	protected Set<Node> getComponents() {
		return Set.of( assetTypesLabel, assetTypes, keyLabel, key, nameLabel, name, descriptionLabel, description );
	}

	private List<AssetType> getUserAssetTypes( XenonProgramProduct product ) {
		return product.getProgram().getAssetManager().getAssetTypes().stream().filter( AssetType::isUserType ).sorted( new AssetTypeNameComparator() ).toList();
	}

	private List<AssetType> getAssetTypes( XenonProgramProduct product ) {
		return product.getProgram().getAssetManager().getAssetTypes().stream().sorted( new AssetTypeNameComparator() ).toList();
	}

	@Override
	protected void doSettingValueChanged( SettingsEvent event ) {
		doUpdateFields( String.valueOf( event.getNewValue() ) );
	}

}
