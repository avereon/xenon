package com.avereon.xenon.tool;

import com.avereon.index.Document;
import com.avereon.index.Hit;
import com.avereon.index.Terms;
import com.avereon.product.Rb;
import com.avereon.util.TextUtil;
import com.avereon.xenon.ProgramProduct;
import com.avereon.xenon.ProgramTool;
import com.avereon.xenon.RbKey;
import com.avereon.xenon.UiFactory;
import com.avereon.xenon.asset.Asset;
import com.avereon.xenon.asset.OpenAssetRequest;
import com.avereon.xenon.workpane.ToolException;
import com.avereon.xenon.workpane.Workpane;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import lombok.CustomLog;
import lombok.NonNull;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@CustomLog
public class SearchTool extends ProgramTool {

	private final TextField search;

	@Deprecated
	private final ListView<Hit> hitList;

	// NOTE This should eventually be a tree
	private final ListView<Document> docList;

	public SearchTool( ProgramProduct product, Asset asset ) {
		super( product, asset );
		setId( "tool-search" );

		search = new TextField();
		search.setPromptText( Rb.text( RbKey.PROMPT, "search" ) );

		hitList = new ListView<>();
		hitList.setCellFactory( new HitListCellFactory() );
		hitList.setPlaceholder( new Label( Rb.text( RbKey.WORKSPACE, "search-no-results" ) ) );
		VBox.setVgrow( hitList, Priority.ALWAYS );

		docList = new ListView<>();
		docList.setCellFactory( new DocListCellFactory() );
		docList.setPlaceholder( new Label( Rb.text( RbKey.WORKSPACE, "search-no-results" ) ) );

		ScrollPane scroller = new ScrollPane( docList );
		scroller.setFitToWidth( true );
		scroller.setFitToHeight( true );
		getChildren().add( scroller );

		VBox listContainer = new VBox(UiFactory.PAD, search, scroller );
		getChildren().add( listContainer );

		search.textProperty().addListener( ( p, o, n ) -> doSearchAll( n ) );
		search.setOnKeyPressed( e -> {
			switch( e.getCode() ) {
				case ESCAPE -> reset();
				case ENTER -> openDoc();
				case UP -> selectPreviousDoc();
				case DOWN -> selectNextDoc();
			}
		} );
		hitList.setOnMousePressed( e -> openHit() );
		docList.setOnMousePressed( e -> openDoc() );
	}

	@Override
	public Workpane.Placement getPlacement() {
		return Workpane.Placement.DOCK_RIGHT;
	}

	// THREAD JavaFX Application Thread
	@Override
	protected void ready( OpenAssetRequest request ) throws ToolException {
		super.ready( request );
		setTitle( Rb.text( RbKey.TOOL, "search-name" ) );
		setGraphic( getProgram().getIconLibrary().getIcon( "search" ) );
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

	public void open( @NonNull Document document ) {
		getProgram().getAssetManager().openAsset( document.uri() );
		reset();
	}

	public Optional<Hit> getSelectedHit() {
		return Optional.ofNullable( hitList.getSelectionModel().getSelectedItem() );
	}

	public Optional<Document> getSelectedDoc() {
		return Optional.ofNullable( docList.getSelectionModel().getSelectedItem() );
	}

	public void selectPreviousHit() {
		if( hitList.getItems().isEmpty() || hitList.getSelectionModel().getSelectedItem() == hitList.getItems().get( 0 ) ) return;
		hitList.getSelectionModel().selectPrevious();
	}

	public void selectNextHit() {
		if( hitList.getItems().isEmpty() || hitList.getSelectionModel().getSelectedItem() == hitList.getItems().get( hitList.getItems().size() - 1 ) ) return;
		hitList.getSelectionModel().selectNext();
	}

	public void selectPreviousDoc() {
		if( docList.getItems().isEmpty() || docList.getSelectionModel().getSelectedItem() == docList.getItems().get( 0 ) ) return;
		docList.getSelectionModel().selectPrevious();
	}

	public void selectNextDoc() {
		if( docList.getItems().isEmpty() || docList.getSelectionModel().getSelectedItem() == docList.getItems().get( docList.getItems().size() - 1 ) ) return;
		docList.getSelectionModel().selectNext();
	}

	private void openHit() {
		this.getSelectedHit().ifPresent( this::open );
	}

	private void openDoc() {
		this.getSelectedDoc().ifPresent( this::open );
	}

	private void doSearchAll( String text ) {
		if( TextUtil.isEmpty( text ) ) {
			this.hideHits();
		} else {
			String query = text.toLowerCase();
			List<String> terms = Terms.split( query );
			getProgram().getIndexService().searchAll( query, terms ).ifPresentOrElse( this::showHits, this::hideHits );
		}

		if( TextUtil.isEmpty( text ) ) {
			this.hideDocs();
		} else {
			String query = text.toLowerCase();
			List<String> terms = Terms.split( query );
			getProgram().getIndexService().searchAll( query, terms ).ifPresentOrElse( this::showDocs, this::hideDocs );
		}
	}

	private void showHits( List<Hit> hits ) {
		hitList.getItems().setAll( hits );
		hitList.getSelectionModel().selectFirst();
	}

	private void hideHits() {
		hitList.getItems().clear();
	}

	private void showDocs( List<Hit> hits ) {

		Set<Document> docs = new HashSet<>();
		hits.forEach( h -> docs.add( h.document() ) );

		docList.getItems().setAll( docs );
		docList.getSelectionModel().selectFirst();
	}

	private void hideDocs() {
		docList.getItems().clear();
	}

	private class HitListCellFactory implements Callback<ListView<Hit>, ListCell<Hit>> {

		@Override
		public ListCell<Hit> call( ListView<Hit> hitListView ) {

			return new ListCell<>() {

				private final Label label;

				private final Label category;

				{
					label = new Label();
					HBox.setHgrow( label, Priority.ALWAYS );
					category = new Label();
					HBox box = new HBox( label, category );
					box.setSpacing( 2 * UiFactory.PAD );

					setContentDisplay( ContentDisplay.GRAPHIC_ONLY );
					setGraphic( box );
					setText( null );
				}

				@Override
				protected void updateItem( Hit item, boolean empty ) {
					super.updateItem( item, empty );
					if( item == null || empty ) {
						label.setGraphic( null );
						label.setText( null );
						category.setGraphic( null );
						category.setText( null );
					} else {
						String icon = switch( item.priority() ) {
							case Hit.TAG_PRIORITY -> "tag";
							case Hit.TITLE_PRIORITY -> "title";
							default -> "document";
						};
						label.setGraphic( getProgram().getIconLibrary().getIcon( item.document().icon() ) );
						label.setText( item.document().title() );
						category.setGraphic( getProgram().getIconLibrary().getIcon( icon ) );
						category.setText( item.word() );
					}
				}
			};

		}

	}

	private class DocListCellFactory implements Callback<ListView<Document>, ListCell<Document>> {

		@Override
		public ListCell<Document> call( ListView<Document> hitListView ) {

			return new ListCell<>() {

				private final Label label;

				private final Label category;

				{
					label = new Label();
					HBox.setHgrow( label, Priority.ALWAYS );
					category = new Label();
					HBox box = new HBox( label, category );
					box.setSpacing( 2 * UiFactory.PAD );

					setContentDisplay( ContentDisplay.GRAPHIC_ONLY );
					setGraphic( box );
					setText( null );
				}

				@Override
				protected void updateItem( Document item, boolean empty ) {
					super.updateItem( item, empty );
					if( item == null || empty ) {
						label.setGraphic( null );
						label.setText( null );
						category.setGraphic( null );
						category.setText( null );
					} else {
//						String icon = switch( item.priority() ) {
//							case Hit.TAG_PRIORITY -> "tag";
//							case Hit.TITLE_PRIORITY -> "title";
//							default -> "document";
//						};
						String icon = "document";
						label.setGraphic( getProgram().getIconLibrary().getIcon( item.icon() ) );
						label.setText( item.title() );
//						category.setGraphic( getProgram().getIconLibrary().getIcon( icon ) );
//						category.setText( item.word() );
					}
				}
			};

		}

	}

}
