package com.xeomar.xenon.workspace;

import com.xeomar.util.FileUtil;
import com.xeomar.xenon.util.Lambda;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.shape.Rectangle;

import java.util.Set;
import java.util.Timer;
import java.util.concurrent.CopyOnWriteArraySet;

public class MemoryMonitor extends AbstractMonitor {

	private static Set<MemoryMonitor> monitors;

	private boolean textVisible;

	private boolean showPercent;

	private long used;

	private long allocated;

	private long maximum;

	private double allocatedPercent;

	private double usedPercent;

	private Rectangle memoryMax;

	private Rectangle memoryAllocated;

	private Rectangle memoryUsed;

	private Label label;

	static {
		monitors = new CopyOnWriteArraySet<>();
		Timer timer = new Timer( "Memory Monitor Timer", true );
		timer.schedule( Lambda.timerTask( MemoryMonitor::requestUpdate ), DEFAULT_POLL_INTERVAL, DEFAULT_POLL_INTERVAL );
	}

	public MemoryMonitor() {
		getStyleClass().setAll( "memory-monitor" );

		label = new Label();
		label.getStyleClass().add( "memory-monitor-label" );

		memoryMax = new Rectangle();
		memoryMax.setManaged( false );
		memoryMax.getStyleClass().add( "memory-monitor-max" );

		memoryAllocated = new Rectangle();
		memoryAllocated.setManaged( false );
		memoryAllocated.getStyleClass().add( "memory-monitor-allocated" );

		memoryUsed = new Rectangle();
		memoryUsed.setManaged( false );
		memoryUsed.getStyleClass().add( "memory-monitor-used" );

		getChildren().addAll( memoryMax, memoryAllocated, memoryUsed, label );

		monitors.add( this );
	}

	public boolean isTextVisible() {
		return textVisible;
	}

	public void setTextVisible( boolean visible ) {
		this.textVisible = visible;
		update();
	}

	public boolean isShowPercent() {
		return showPercent;
	}

	public void setShowPercent( boolean showPercent ) {
		this.showPercent = showPercent;
		update();
	}

	public void close() {
		monitors.remove( this );
	}

	@Override
	protected void layoutChildren() {
		super.layoutChildren();

		double width = super.getWidth() - 1;
		double height = super.getHeight() - 1;

		memoryMax.setWidth( width );
		memoryMax.setHeight( height );

		memoryAllocated.setWidth( width * allocatedPercent );
		memoryAllocated.setHeight( height );

		memoryUsed.setWidth( width * usedPercent );
		memoryUsed.setHeight( height );
	}

	private void update() {
		allocatedPercent = (float)allocated / (float)maximum;
		usedPercent = (float)used / (float)maximum;

		// Use a space character so the preferred size is calculated correctly
		StringBuilder text = new StringBuilder( " " );
		if( isTextVisible() ) {
			boolean isPercent = isShowPercent();

			String percentUsed = percentFormat.format( usedPercent * 100 ) + "%";
			String percentAllocated = percentFormat.format( allocatedPercent * 100 ) + "%";

			String usedSize = FileUtil.getHumanSizeBase2( used, true );
			String allocatedSize = FileUtil.getHumanSizeBase2( allocated, true );
			String maximumSize = FileUtil.getHumanSizeBase2( maximum, true );

			text.append( isPercent ? percentUsed : usedSize );
//			text.append( " " );
//			text.append( DIVIDER );
//			text.append( " " );
//			text.append( isPercent ? percentAllocated : allocatedSize );
			text.append( " " );
			text.append( DIVIDER );
			text.append( " " );
			text.append( maximumSize );
		}

		this.label.setText( text.toString() );
		requestLayout();
	}

	private void update( long maximum, long allocated, long used ) {
		this.maximum = maximum;
		this.allocated = allocated;
		this.used = used;
		update();
	}

	private static void requestUpdate() {
		Runtime runtime = Runtime.getRuntime();
		long maximum = runtime.maxMemory();
		long allocated = runtime.totalMemory();
		long used = allocated - runtime.freeMemory();

		for( MemoryMonitor monitor : monitors ) {
			Platform.runLater( () -> monitor.update( maximum, allocated, used ) );
		}
	}

}
