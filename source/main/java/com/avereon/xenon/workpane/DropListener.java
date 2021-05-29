package com.avereon.xenon.workpane;

import javafx.scene.input.TransferMode;

public interface DropListener {

	TransferMode[] COPY_ONLY = { TransferMode.COPY };

	TransferMode[] MOVE_ONLY = { TransferMode.MOVE };

	TransferMode[] getSupportedModes(Tool tool);

	void handleDrop( DropEvent event ) throws Exception;

}
