package com.xeomar.xenon.workspace;

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

}
