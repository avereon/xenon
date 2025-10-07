package com.avereon.xenon.tool;

import com.avereon.product.Rb;
import com.avereon.xenon.RbKey;
import com.avereon.xenon.XenonProgramProduct;
import com.avereon.xenon.ProgramTool;
import com.avereon.xenon.asset.Resource;
import com.avereon.xenon.asset.ResourceType;
import com.avereon.xenon.asset.OpenAssetRequest;
import com.avereon.xenon.compare.AssetTypeNameComparator;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import lombok.CustomLog;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@CustomLog
public class NewAssetTool extends ProgramTool {

	private final AssetTypeView view;

	public NewAssetTool( XenonProgramProduct product, Resource resource ) {
		super( product, resource );
		setId( "tool-asset-new" );

		view = new AssetTypeView();

		ScrollPane scroller = new ScrollPane( view );
		scroller.setFitToHeight( true );
		scroller.setFitToWidth( true );

		getChildren().add( scroller );
	}

	@Override
	protected void ready( OpenAssetRequest request ) {
		setTitle( Rb.text( RbKey.TOOL, "asset-name" ) );
		setGraphic( getProgram().getIconLibrary().getIcon( Rb.text( RbKey.TOOL, "asset-icon" ) ) );
	}

	@Override
	protected void open( OpenAssetRequest request ) {
		view.update();
	}

	private class AssetTypeView extends FlowPane {

		private AssetTypeView() {
			setAlignment( Pos.CENTER );
		}

		public void update() {
			List<ResourceType> types = new ArrayList<>( getProgram().getResourceManager().getAssetTypes() );
			types.sort( new AssetTypeNameComparator() );

			getChildren().clear();
			getChildren().addAll( types
				.stream()
				.filter( ResourceType::isUserType )
				.map( AssetTypeTile::new )
				.peek( tile -> tile.addEventFilter( MouseEvent.MOUSE_PRESSED, e -> {
					getProgram().getResourceManager().newAsset( tile.getAssetType() );
					NewAssetTool.this.close();
				} ) )
				.collect( Collectors.toList() ) );
		}

	}

	private class AssetTypeTile extends VBox {

		private final ResourceType type;

		AssetTypeTile( ResourceType type ) {
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

		ResourceType getAssetType() {
			return type;
		}

	}

}
