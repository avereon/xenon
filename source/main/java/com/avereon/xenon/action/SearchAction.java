package com.avereon.xenon.action;

import com.avereon.xenon.ProgramAction;
import com.avereon.xenon.Xenon;
import com.avereon.xenon.asset.type.ProgramSearchType;
import javafx.event.ActionEvent;

public class SearchAction extends ProgramAction {

	public SearchAction( Xenon program ) {
		super( program );
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public void handle( ActionEvent event ) {
		// Open the index search tool
		getProgram().getResourceManager().openAsset( ProgramSearchType.URI );
	}

}

