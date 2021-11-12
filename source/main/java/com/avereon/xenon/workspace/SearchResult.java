package com.avereon.xenon.workspace;

import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;

public class SearchResult extends BorderPane {

	private Label label;

	public SearchResult() {
		this( "" );
	}

	public SearchResult( String text ) {
		getStyleClass().addAll( "hit" );
		this.label = new Label( text );

		setCenter( label );
	}

}
