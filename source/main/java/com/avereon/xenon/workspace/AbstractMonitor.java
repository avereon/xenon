package com.avereon.xenon.workspace;

import javafx.application.Platform;
import javafx.scene.layout.StackPane;

import java.text.DecimalFormat;

public abstract class AbstractMonitor extends StackPane {

	protected static final double MINIMUM_WIDTH = 100;

	protected static final String DIVIDER = "/";

	protected static final int DEFAULT_POLL_INTERVAL = 2000;

	protected static DecimalFormat percentFormat = new DecimalFormat( "#0" );

	@Override
	protected double computeMinWidth( double height ) {
		return getInsets().getLeft() + MINIMUM_WIDTH + getInsets().getRight();
	}

	/**
	 * Request an update from any thread. This method requests the update be done
	 * on the FX Application thread.
	 */
	public void requestUpdate() {
		Platform.runLater( AbstractMonitor.this::update );
	}

	/**
	 * Update the monitor information. Should be called on the FX Application thread.
	 */
	protected abstract void update();

}
