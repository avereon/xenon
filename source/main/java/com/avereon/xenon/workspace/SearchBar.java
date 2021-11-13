package com.avereon.xenon.workspace;

import com.avereon.index.Hit;
import com.avereon.product.Rb;
import com.avereon.util.TextUtil;
import com.avereon.xenon.BundleKey;
import com.avereon.xenon.index.IndexService;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import lombok.CustomLog;
import lombok.NonNull;

@CustomLog
public class SearchBar extends HBox {

	private final Workspace workspace;

	private final IndexService indexService;

	private final TextField search;

	private Hit topHit;

	public SearchBar( Workspace workspace, IndexService indexService ) {
		getStyleClass().add( "workspace-search" );

		this.workspace = workspace;
		this.indexService = indexService;
		search = new TextField();
		search.setPromptText( Rb.text( BundleKey.PROMPT, "index-search" ) );
		HBox.setHgrow( search, Priority.ALWAYS );

		getChildren().addAll( search );

		search.textProperty().addListener( ( p, o, n ) -> doSearch( n ) );
		search.setOnKeyPressed( e -> {
			if( e.getCode() == KeyCode.ESCAPE ) reset();
			if( e.getCode() == KeyCode.ENTER ) openTopHit();
		} );
	}

	public void reset() {
		search.setText( "" );
	}

	public void open( @NonNull Hit hit ) {
		workspace.getProgram().getAssetManager().openAsset( hit.document().uri() );
		reset();
	}

	private void openTopHit() {
		if( topHit == null ) return;
		open( topHit );
	}

	private void doSearch( String term ) {
		indexService.search( term ).ifSuccess( hits -> {
			topHit = null;
			if( TextUtil.isEmpty( term ) ) {
				workspace.hideHits();
			} else {
				workspace.showHits( hits );
				hits.stream().findFirst().ifPresent( h -> topHit = h );
			}
		} );
	}

}
