package com.avereon.xenon.action;

import com.avereon.xenon.ProgramAction;
import com.avereon.xenon.Xenon;
import com.avereon.xenon.asset.type.ProgramNoticeType;
import javafx.event.ActionEvent;

public class NoticeAction extends ProgramAction {

	public NoticeAction( Xenon program ) {
		super( program );
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public void handle( ActionEvent event ) {
		// Open the notice tool
		getProgram().getAssetManager().openAsset( ProgramNoticeType.URI );
	}

}
