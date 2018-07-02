package com.xeomar.xenon;

import com.xeomar.util.FileUtil;
import javafx.application.Platform;
import javafx.beans.value.WritableValue;
import javafx.css.StyleableProperty;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.AccessibleRole;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Labeled;
import javafx.scene.control.Skin;
import javafx.scene.control.skin.LabelSkin;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;

import java.text.DecimalFormat;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CopyOnWriteArraySet;

public class MemoryMonitor extends Pane {

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

	// IDEA updates every five seconds

	static {
		monitors = new CopyOnWriteArraySet<>();
		Timer timer = new Timer( "Memory Monitor Timer", true );
		TimerTask task = new TimerTask() {

			@Override
			public void run() {
				update();
			}

		};
		timer.schedule( task, 0, 2000 );
	}

	public MemoryMonitor() {
		getStyleClass().setAll( "memory-monitor" );

		label = new Label();
		label.getStyleClass().add( "memory-monitor-label" );

		memoryMax = new Rectangle();
		memoryMax.getStyleClass().add( "memory-monitor-max" );
		memoryMax.widthProperty().bind( label.widthProperty() );
		memoryMax.heightProperty().bind( label.heightProperty() );

		memoryAllocated = new Rectangle();
		memoryAllocated.getStyleClass().add( "memory-monitor-allocated" );
		memoryAllocated.heightProperty().bind( label.heightProperty() );

		memoryUsed = new Rectangle();
		memoryUsed.getStyleClass().add( "memory-monitor-used" );
		memoryUsed.heightProperty().bind( label.heightProperty() );

		getChildren().addAll( memoryMax, memoryAllocated, memoryUsed, label );

		monitors.add( this );
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

		String usedSize = FileUtil.getHumanSize( used );
		String allocatedSize = FileUtil.getHumanSize( allocated );
		String maximumSize = FileUtil.getHumanSize( maximum );

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

	public static boolean isShowPercent() {
		return showPercent;
	}

	public static void setShowPercent( boolean showPercent ) {
		MemoryMonitor.showPercent = showPercent;
	}

	@Override
	protected void layoutChildren() {
		super.layoutChildren();

		double width = super.getWidth();
//		memoryAllocated.setX( 0 );
//		memoryAllocated.setY( 0 );
//		memoryUsed.setX( 0 );
//		memoryUsed.setY( 0 );

		memoryAllocated.setWidth( width * allocatedPercent );
		memoryUsed.setWidth( width * usedPercent );
	}

	@Override
	protected void layoutInArea( Node child, double areaX, double areaY, double areaWidth, double areaHeight, double areaBaselineOffset, Insets margin, boolean fillWidth, boolean fillHeight, HPos halignment, VPos valignment ) {
		super.layoutInArea( child, areaX, areaY, areaWidth, areaHeight, areaBaselineOffset, margin, fillWidth, fillHeight, halignment, valignment );
	}

	private void update( double used, double allocated, String text ) {
		this.allocatedPercent = allocated;
		this.usedPercent = used;
		this.label.setText( text );
		requestLayout();
	}

}
