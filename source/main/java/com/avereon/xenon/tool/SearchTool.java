package com.avereon.xenon.tool;

import com.avereon.index.Document;
import com.avereon.index.Hit;
import com.avereon.index.Terms;
import com.avereon.product.Rb;
import com.avereon.util.TextUtil;
import com.avereon.xenon.ProgramTool;
import com.avereon.xenon.RbKey;
import com.avereon.xenon.UiFactory;
import com.avereon.xenon.XenonProgramProduct;
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

import java.util.List;
import java.util.Optional;

@CustomLog
public class SearchTool extends ProgramTool {

	private final TextField search;

	// TODO This should eventually be a tree
	private final ListView<Document> docList;

	public SearchTool( XenonProgramProduct product, Asset asset ) {
		super( product, asset );
		setId( "tool-search" );

		search = new TextField();
		search.setPromptText( Rb.text( RbKey.PROMPT, "search" ) );

		docList = new ListView<>();
		docList.setCellFactory( new DocListCellFactory() );
		docList.setPlaceholder( new Label( Rb.text( RbKey.WORKSPACE, "search-no-results" ) ) );

		ScrollPane scroller = new ScrollPane( docList );
		scroller.setFitToWidth( true );
		scroller.setFitToHeight( true );
		getChildren().add( scroller );

		VBox listContainer = new VBox( UiFactory.PAD, search, scroller );
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
		getProgram().getAssetManager().openAsset( hit.getDocument().uri() );
		reset();
	}

	public void open( @NonNull Document document ) {
		getProgram().getAssetManager().openAsset( document.uri() );
		reset();
	}

	public Optional<Document> getSelectedDoc() {
		return Optional.ofNullable( docList.getSelectionModel().getSelectedItem() );
	}

	public void selectPreviousDoc() {
		if( docList.getItems().isEmpty() || docList.getSelectionModel().getSelectedItem() == docList.getItems().get( 0 ) ) return;
		docList.getSelectionModel().selectPrevious();
	}

	public void selectNextDoc() {
		if( docList.getItems().isEmpty() || docList.getSelectionModel().getSelectedItem() == docList.getItems().get( docList.getItems().size() - 1 ) ) return;
		docList.getSelectionModel().selectNext();
	}

	private void openDoc() {
		this.getSelectedDoc().ifPresent( this::open );
	}

	private void doSearchAll( String text ) {
		if( TextUtil.isEmpty( text ) ) {
			this.hideDocs();
		} else {
			String query = text.toLowerCase();
			List<String> terms = Terms.split( query );
			getProgram().getIndexService().searchAll( query, terms ).ifPresentOrElse( this::showDocs, this::hideDocs );
		}
	}

	private void showDocs( List<Hit> hits ) {
		// NOTE Then incoming hits are already in hit order

		//		log.atConfig().log();
		//		hits.forEach( h -> log.atConfig().log( "hit={0} {1} {2} {3}", h.getPoints(), h.getPriority(), h.getCoordinates(), h.getDocument().title() ) );
		List<Document> docs = hits.stream().peek( h -> {
			//			if( h.getPoints() > h.getDocument().searchPoints() ) {
			//				h.getDocument().searchPoints( h.getPoints() );
			//			}
			h.getDocument().searchPoints( h.getPoints() );
			h.getDocument().searchPriority( h.getPriority() );
		} ).map( Hit::getDocument ).distinct().toList();

		docList.getItems().setAll( docs );
		docList.getSelectionModel().selectFirst();
	}

	private void hideDocs() {
		docList.getItems().clear();
	}

	// TODO Use to render tree cells
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
						String icon = switch( item.getPriority() ) {
							case Hit.TAG_PRIORITY -> "tag";
							case Hit.TITLE_PRIORITY -> "title";
							default -> "document";
						};
						label.setGraphic( getProgram().getIconLibrary().getIcon( item.getDocument().icon() ) );
						label.setText( item.getDocument().title() );
						category.setGraphic( getProgram().getIconLibrary().getIcon( icon ) );
						category.setText( item.getWord() );
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
						String icon = switch( item.searchPriority() ) {
							case Hit.TAG_PRIORITY -> "tag";
							case Hit.TITLE_PRIORITY -> "title";
							default -> "document";
						};
						label.setGraphic( getProgram().getIconLibrary().getIcon( item.icon() ) );
						label.setText( item.title() );
						category.setGraphic( getProgram().getIconLibrary().getIcon( icon ) );
						category.setText( String.valueOf( item.searchPoints() ) );
					}
				}
			};

		}

	}

}
