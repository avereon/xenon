package com.xeomar.xenon.workspace;

import com.xeomar.util.FileUtil;
import com.xeomar.xenon.util.LambdaTask;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;

import java.text.DecimalFormat;
import java.util.Set;
import java.util.Timer;
import java.util.concurrent.CopyOnWriteArraySet;

public class MemoryMonitor extends Pane {

	private static final int DEFAULT_POLL_INTERVAL = 2000;

	private static final String DIVIDER = "/";

	private static Set<MemoryMonitor> monitors;

	private static boolean showPercent;

	private static DecimalFormat percentFormat = new DecimalFormat( "#0" );

	private double allocatedPercent;

	private double usedPercent;

	private Rectangle memoryMax;

	private Rectangle memoryAllocated;

	private Rectangle memoryUsed;

	private Label label;

	static {
		monitors = new CopyOnWriteArraySet<>();
		Timer timer = new Timer( "Memory Monitor Timer", true );
		timer.schedule( LambdaTask.build( MemoryMonitor::update ), DEFAULT_POLL_INTERVAL, DEFAULT_POLL_INTERVAL );
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

		// If the memory monitor is clicked then call the garbage collector
		this.setOnMouseClicked( ( event ) -> Runtime.getRuntime().gc() );
	}

	public static boolean isShowPercent() {
		return showPercent;
	}

	public static void setShowPercent( boolean showPercent ) {
		MemoryMonitor.showPercent = showPercent;
	}

	public void close() {
		monitors.remove( this );
	}

	private static void update() {
		Runtime runtime = Runtime.getRuntime();
		long maximum = runtime.maxMemory();
		long allocated = runtime.totalMemory();
		long used = allocated - runtime.freeMemory();

		float allocatedPercent = (float)allocated / (float)maximum;
		float usedPercent = (float)used / (float)maximum;

		String usedSize = FileUtil.getHumanBinSize( used );
		String allocatedSize = FileUtil.getHumanBinSize( allocated );
		String maximumSize = FileUtil.getHumanBinSize( maximum );

		String text;
		if( isShowPercent() ) {
			text = percentFormat.format( usedPercent * 100 ) + "% " + DIVIDER + " " + percentFormat.format( allocatedPercent * 100 ) + "% " + DIVIDER + " " + maximumSize;
		} else {
			text = usedSize + " " + DIVIDER + " " + allocatedSize + " " + DIVIDER + " " + maximumSize;
		}

		for( MemoryMonitor monitor : monitors ) {
			Platform.runLater( () -> monitor.update( usedPercent, allocatedPercent, text ) );
		}
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

	private void update( double used, double allocated, String text ) {
		this.allocatedPercent = allocated;
		this.usedPercent = used;
		this.label.setText( text );
		requestLayout();
	}

}
