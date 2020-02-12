package com.avereon.xenon.tool;

import com.avereon.xenon.BundleKey;
import com.avereon.xenon.ProgramProduct;
import com.avereon.xenon.ProgramTool;
import com.avereon.xenon.asset.Asset;
import com.avereon.xenon.asset.AssetType;
import com.avereon.xenon.asset.OpenAssetRequest;
import com.avereon.xenon.compare.AssetTypeNameComparator;
import com.avereon.xenon.workpane.ToolException;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AssetTool extends ProgramTool {

	private AssetTypeView view;

	public AssetTool( ProgramProduct product, Asset asset ) {
		super( product, asset );
		setId( "tool-asset" );

		setTitle( product.rb().text( BundleKey.TOOL, "asset-name" ) );
		setGraphic( product.getProgram().getIconLibrary().getIcon( product.rb().text( BundleKey.TOOL, "asset-icon" ) ) );

		view = new AssetTypeView();

		ScrollPane scroller = new ScrollPane( view );
		scroller.setFitToHeight( true );
		scroller.setFitToWidth( true );

		getChildren().add( scroller );
	}

	@Override
	protected void assetReady( OpenAssetRequest request ) throws ToolException {
		view.update();
	}

	private class AssetTypeView extends FlowPane {

		private AssetTypeView() {
			setAlignment( Pos.CENTER );
		}

		public void update() {
			List<AssetType> types = new ArrayList<>( getProgram().getAssetManager().getAssetTypes() );
			types.sort( new AssetTypeNameComparator() );

			getChildren().clear();
			getChildren().addAll( types.stream().filter( AssetType::isUserType ).map( AssetTypeTile::new ).peek(tile->{
				tile.addEventFilter( MouseEvent.MOUSE_PRESSED, e -> {
					getProgram().getAssetManager().newAsset( tile.getAssetType() );
					AssetTool.this.close();
				} );
			}).collect( Collectors.toList() ) );
		}

	}

	private class AssetTypeTile extends VBox {

		private AssetType type;

		AssetTypeTile( AssetType type ) {
			this.type = type;
			getStyleClass().add( "asset-type-tile" );

			setAlignment( Pos.CENTER );

			Label name = new Label( type.getName() );
			name.setFont( Font.font( name.getFont().getName(), name.getFont().getSize() * 2 ) );

			Label description = new Label( type.getDescription() );
			description.setTextAlignment( TextAlignment.CENTER );
			description.setWrapText( true );

			getChildren().add( getProgram().getIconLibrary().getIcon( type.getIcon(), 64 ) );
			getChildren().add( name );
			getChildren().add( description );
		}

		AssetType getAssetType() {
			return type;
		}

	}

}