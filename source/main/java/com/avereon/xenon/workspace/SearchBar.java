package com.avereon.xenon.workspace;

import com.avereon.index.Document;
import com.avereon.util.TextUtil;
import com.avereon.xenon.index.IndexService;
import com.avereon.zarra.javafx.Fx;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import lombok.CustomLog;

@CustomLog
public class SearchBar extends HBox {

	private final Workspace workspace;

	private final IndexService indexService;

	private final TextField search;

	public SearchBar( Workspace workspace, IndexService indexService ) {
		this.workspace = workspace;
		this.indexService = indexService;
		// FIXME GRRRR this is not working the way I like
		search = new TextField();

		getChildren().addAll( search );

		search.setOnKeyPressed( e -> {
			if( e.getCode() == KeyCode.ESCAPE ) Fx.run( () -> search.setText( "" ) );
			if( e.getCode() == KeyCode.ENTER ) openSelectedResult();
		} );
		search.textProperty().addListener( ( p, o, n ) -> doSearch( n ) );
	}

	TextField getSearchField() {
		return search;
	}

	private void doSearch( String term ) {
		indexService.search( term ).ifSuccess( hits -> {
			if( TextUtil.isEmpty( term ) ) {
				workspace.hideHits();
			} else {
				workspace.showHits( hits );
			}
		} );
	}

	private void openSelectedResult() {
		// NEXT Open selected document
		Document selected = null;
		log.atConfig().log( "open selected document=%s", selected );
	}

}
