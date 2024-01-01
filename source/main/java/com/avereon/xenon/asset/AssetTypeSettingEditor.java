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
 */
public class AssetTypeSettingEditor extends SettingEditor {

	private final Label assetTypesLabel;

	private final ComboBox<AssetType> assetTypes;

	private final Label keyLabel;

	private final Label key;

	private final Label nameLabel;

	private final Label name;

	private final Label descriptionLabel;

	private final Label description;

	private final GridPane assetTypeGrid;

	private final GridPane assetTypeReference;

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

		// NEXT Continue working on the AssetTypeSettingsEditor

		//		// Maybe a table with three columns: asset type, associations, tools
		//		assetTypeTable = new TableView<>( FXCollections.observableArrayList( types ) );
		//		TableColumn<AssetType, String> assetTypeName = new TableColumn<>( Rb.text( RbKey.SETTINGS, "asset-type" ) );
		//		TableColumn<AssetType, List<Codec.Association>> assetTypeAssociations = new TableColumn<>( Rb.text( RbKey.SETTINGS, "associations" ) );
		//		TableColumn<AssetType, List<String>> assetTypeTools = new TableColumn<>( Rb.text( RbKey.SETTINGS, "tools" ) );
		//
		//		assetTypeName.setCellValueFactory( p -> new SimpleStringProperty( p.getValue().getName() ) );
		//		assetTypeAssociations.setCellValueFactory( p -> {
		//			AssetType type = p.getValue();
		//				List<Codec.Association> associations = new ArrayList<>(type.getAssociations());
		//			return new SimpleObjectProperty<>(associations);
		//		});
		//		assetTypeTools.setCellValueFactory( p -> {
		//			AssetType type = p.getValue();
		//			List<Class<? extends ProgramTool>> toolClasses = getProduct().getProgram().getToolManager().getRegisteredTools( type );
		//			return new SimpleObjectProperty<>(toolClasses.stream().map( Class::getSimpleName ).toList());
		//		} );
		//
		//		assetTypeTable.getColumns().setAll( assetTypeName, assetTypeAssociations, assetTypeTools );

		int assetTypeGridIndex = 0;
		assetTypeReference = new GridPane();
		assetTypeReference.setHgap( UiFactory.PAD );
		assetTypeReference.setVgap( UiFactory.PAD );

		// FIXME This was just a quick and dirty way to show the asset type associations
		// Get a sorted list of the asset types
		List<AssetType> types = getProduct().getProgram().getAssetManager().getAssetTypes().stream().sorted().toList();
		for( AssetType type : types ) {
			Label name = new Label( type.getName() );

			int assocGridIndex = 0;
			GridPane assocGrid = new GridPane();
			for( Codec.Association association : type.getAssociations() ) {
				assocGrid.addRow( assocGridIndex++, new Label( association.pattern().name() ), new Label( association.value() ) );
			}

			int toolGridIndex = 0;
			GridPane toolGrid = new GridPane();
			List<Class<? extends ProgramTool>> toolClasses = getProduct().getProgram().getToolManager().getRegisteredTools( type );
			for( Class<? extends ProgramTool> toolClass : toolClasses ) {
				toolGrid.addRow( toolGridIndex++, new Label( toolClass.getSimpleName() ) );
			}

			assetTypeReference.addRow( assetTypeGridIndex++, name, assocGrid, toolGrid );
		}

		assetTypes.valueProperty().addListener( (p,o,n) -> doUpdateFields( n.getKey() ) );
		assetTypes.getSelectionModel().select( 0 );

		//doUpdateFields( setting.getSettings().get( getKey(), String.class, "" ) );
	}

	private void doUpdateFields( String typeKey ) {
		AssetType type = getProduct().getProgram().getAssetManager().getAssetType( typeKey );
		key.setText( type == null ? "" : type.getKey() );
		name.setText( type == null ? "" : type.getName() );
		description.setText( type == null ? "" : type.getDescription() );

		if( type == null ) return;

		// What about tool associations?
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
			boolean d = toolClass == c;
			System.out.printf( "    %s %s%n", c.getName(), d ? "*" : "" );
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
		//GridPane.setRowSpan( assetTypeGrid, GridPane.REMAINING );
		pane.addRow( row, assetTypeGrid );

		GridPane.setColumnSpan( assetTypeReference, GridPane.REMAINING );
		GridPane.setRowSpan( assetTypeReference, GridPane.REMAINING );
		pane.addRow( row + 1, assetTypeReference );
	}

	@Override
	protected Set<Node> getComponents() {
		return Set.of( assetTypesLabel, assetTypes, keyLabel, key, nameLabel, name, descriptionLabel, description );
	}

	private List<AssetType> getAssetTypes( XenonProgramProduct product ) {
		return product.getProgram().getAssetManager().getAssetTypes().stream().filter( AssetType::isUserType ).sorted( new AssetTypeNameComparator() ).toList();
	}

	@Override
	protected void doSettingValueChanged( SettingsEvent event ) {
		doUpdateFields( String.valueOf( event.getNewValue() ) );
	}
}
