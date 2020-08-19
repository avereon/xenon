package com.avereon.xenon.workpane;

import javafx.scene.input.TransferMode;

public interface DropListener {

	TransferMode[] getSupportedModes();

	void handleDrop( DropEvent event ) throws Exception;

}
