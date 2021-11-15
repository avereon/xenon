package com.avereon.xenon.tool;

import com.avereon.index.Hit;
import com.avereon.product.Rb;
import com.avereon.util.TextUtil;
import com.avereon.xenon.BundleKey;
import com.avereon.xenon.ProgramProduct;
import com.avereon.xenon.ProgramTool;
import com.avereon.xenon.asset.Asset;
import com.avereon.xenon.asset.OpenAssetRequest;
import com.avereon.xenon.workpane.ToolException;
import com.avereon.xenon.workpane.Workpane;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import lombok.NonNull;

import java.util.List;
import java.util.Optional;

public class IndexSearchTool extends ProgramTool {

	private final TextField search;

	private final ListView<Hit> hitList;

	public IndexSearchTool( ProgramProduct product, Asset asset ) {
		super( product, asset );
		setId( "tool-index-search" );

		search = new TextField();
		search.setPromptText( Rb.text( BundleKey.PROMPT, "index-search" ) );

		hitList = new ListView<>();
		hitList.setCellFactory( new HitListCellFactory() );
		hitList.setPlaceholder( new Label( Rb.text( BundleKey.WORKSPACE, "search-no-results" ) ) );
		VBox.setVgrow( hitList, Priority.ALWAYS );

		getChildren().add( new VBox( search, hitList ) );

		search.textProperty().addListener( ( p, o, n ) -> doSearch( n ) );
		search.setOnKeyPressed( e -> {
			switch( e.getCode() ) {
				case ESCAPE -> reset();
				case ENTER -> openHit();
				case UP -> selectPreviousHit();
				case DOWN -> selectNextHit();
			}
		} );
		hitList.setOnMousePressed( e -> openHit() );
	}

	@Override
	public Workpane.Placement getPlacement() {
		return Workpane.Placement.DOCK_RIGHT;
	}

	@Override
	protected void ready( OpenAssetRequest request ) throws ToolException {
		super.ready( request );
		setTitle( Rb.text( BundleKey.TOOL, "index-search-name" ) );
		setGraphic( getProgram().getIconLibrary().getIcon( "index-search" ) );
		requestFocus();
	}

	@Override
	public void requestFocus() {
		search.requestFocus();
	}

	public void reset() {
		search.setText( "" );
		close();
	}

	public void open( @NonNull Hit hit ) {
		getProgram().getAssetManager().openAsset( hit.document().uri() );
		reset();
	}

	public Optional<Hit> getSelectedHit() {
		return Optional.ofNullable( hitList.getSelectionModel().getSelectedItem() );
	}

	public void selectPreviousHit() {
		if( hitList.getItems().isEmpty() || hitList.getSelectionModel().getSelectedItem() == hitList.getItems().get( 0 ) ) return;
		hitList.getSelectionModel().selectPrevious();
	}

	public void selectNextHit() {
		if( hitList.getItems().isEmpty() || hitList.getSelectionModel().getSelectedItem() == hitList.getItems().get( hitList.getItems().size() - 1 ) ) return;
		hitList.getSelectionModel().selectNext();
	}

	private void openHit() {
		this.getSelectedHit().ifPresent( this::open );
	}

	private void doSearch( String term ) {
		getProgram().getIndexService().search( term.toLowerCase() ).ifSuccess( hits -> {
			if( TextUtil.isEmpty( term ) ) {
				this.hideHits();
			} else {
				this.showHits( hits );
			}
		} );
	}

	private void showHits( List<Hit> hits ) {
		hitList.getItems().setAll( hits );
		hitList.getSelectionModel().selectFirst();
	}

	private void hideHits() {
		hitList.getItems().clear();
	}

	private static class HitListCellFactory implements Callback<ListView<Hit>, ListCell<Hit>> {

		@Override
		public ListCell<Hit> call( ListView<Hit> hitListView ) {

			return new ListCell<>() {

				@Override
				protected void updateItem( Hit item, boolean empty ) {
					super.updateItem( item, empty );
					if( item == null || empty ) {
						setText( null );
					} else {
						setText( item.context() );
					}
				}
			};

		}
	}

}
