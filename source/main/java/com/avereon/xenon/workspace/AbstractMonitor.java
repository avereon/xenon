package com.avereon.xenon.workspace;

import com.avereon.xenon.util.Lambda;
import com.avereon.zarra.javafx.Fx;
import javafx.scene.Group;
import javafx.scene.layout.StackPane;

import java.text.DecimalFormat;
import java.util.Set;
import java.util.Timer;
import java.util.concurrent.CopyOnWriteArraySet;

public abstract class AbstractMonitor extends StackPane {

	protected static final double MINIMUM_WIDTH = 100;

	protected static final String DIVIDER = "/";

	protected static final int DEFAULT_POLL_INTERVAL = 2000;

	protected static DecimalFormat percentFormat = new DecimalFormat( "#0" );

	private static Set<AbstractMonitor> monitors;

	private final Group monitorGroup;

	static {
		monitors = new CopyOnWriteArraySet<>();
		Timer timer = new Timer( "Monitor Timer", true );
		timer.schedule( Lambda.timerTask( AbstractMonitor::updateAll ), DEFAULT_POLL_INTERVAL, DEFAULT_POLL_INTERVAL );
	}

	public AbstractMonitor() {
		monitorGroup = new Group();
	}

	public Group getMonitorGroup() {
		return monitorGroup;
	}

	@Override
	protected double computeMinWidth( double height ) {
		return getInsets().getLeft() + MINIMUM_WIDTH + getInsets().getRight();
	}

	/**
	 * Request an update from any thread. This method requests the update be done
	 * on the FX Application thread.
	 */
	public void requestUpdate() {
		Fx.run( AbstractMonitor.this::update );
	}

	/**
	 * Update the monitor information. Should be called on the FX Application thread.
	 */
	protected abstract void update();

	protected void start() {
		monitors.add( this );
	}

	protected void close() {
		monitors.remove( this );
	}

	private static void updateAll() {
		for( AbstractMonitor monitor : monitors ) {
			monitor.requestUpdate();
		}
	}

}
