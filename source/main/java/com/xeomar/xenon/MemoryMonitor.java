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
import javafx.scene.shape.Rectangle;

import java.text.DecimalFormat;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CopyOnWriteArraySet;

public class MemoryMonitor extends Labeled {

	private static final String DIVIDER = "/";

	private static Set<MemoryMonitor> monitors;

	private static Timer timer;

	private static boolean showPercent;

	private static DecimalFormat percentFormat = new DecimalFormat( "#0" );

	private double allocatedPercent;

	private double usedPercent;

	private Rectangle memoryAllocated;

	private Rectangle memoryUsed;

	// IDEA updates every five seconds

	static {
		monitors = new CopyOnWriteArraySet<>();
		timer = new Timer( "Memory Monitor Timer", true );
		TimerTask task = new TimerTask() {

			@Override
			public void run() {
				update();
			}

		};
		timer.schedule( task, 0, 2000 );
	}

	public MemoryMonitor() {
		initialize();
	}

	private void initialize() {
		getStyleClass().setAll( "memory-monitor" );
		setAccessibleRole( AccessibleRole.TEXT );
		// MemoryMonitors are not focus traversable, unlike most other UI Controls.
		// focusTraversable is styleable through css. Calling setFocusTraversable
		// makes it look to css like the user set the value and css will not
		// override. Initializing focusTraversable by calling set on the
		// CssMetaData ensures that css will be able to override the value.
		((StyleableProperty<Boolean>)(WritableValue<Boolean>)focusTraversableProperty()).applyStyle( null, Boolean.FALSE );

		memoryAllocated = new Rectangle();
		memoryAllocated.getStyleClass().add( "memory-monitor-allocated" );

		memoryUsed = new Rectangle();
		memoryUsed.getStyleClass().add( "memory-monitor-used" );

		getChildren().addAll( memoryAllocated, memoryUsed );

		monitors.add( this );
	}

	/** {@inheritDoc} */
	@Override
	protected Skin<?> createDefaultSkin() {
		return new MemoryMonitorSkin( this );
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

		double width = super.getPrefWidth();
		double height = super.getPrefHeight();

		System.out.println( "Memory monitor: " + width + " x " + height );

		memoryAllocated.setWidth( 400 );
		memoryAllocated.setHeight( 20 );

		memoryUsed.setWidth( 200 );
		memoryUsed.setHeight( 20 );
	}

	@Override
	protected void layoutInArea( Node child, double areaX, double areaY, double areaWidth, double areaHeight, double areaBaselineOffset, Insets margin, boolean fillWidth, boolean fillHeight, HPos halignment, VPos valignment ) {
		super.layoutInArea( child, areaX, areaY, areaWidth, areaHeight, areaBaselineOffset, margin, fillWidth, fillHeight, halignment, valignment );
	}

	private void update( double used, double allocated, String text ) {
		this.allocatedPercent = allocated;
		this.usedPercent = used;
		setText( text );
		requestLayout();
	}

}
