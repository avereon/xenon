package com.xeomar.xenon.workspace;

import com.xeomar.xenon.task.Task;
import com.xeomar.xenon.task.TaskEvent;
import com.xeomar.xenon.task.TaskListener;
import com.xeomar.xenon.task.TaskManager;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class TaskMonitor extends Pane {

	private static final double MINIMUM_WIDTH = 100;

	private static final String DIVIDER = "/";

	private static DecimalFormat percentFormat = new DecimalFormat( "#0" );

	private TaskManager taskManager;

	private Rectangle max;

	private Rectangle used;

	private Rectangle pool;

	private List<Rectangle> bars = new ArrayList<>();

	private Label label;

	private boolean textVisible;

	private boolean showPercent;

	private int maxThreadCount;

	private int priorThreadCount;

	private TaskWatcher taskWatcher;

	public TaskMonitor( TaskManager taskManager ) {
		this.taskManager = taskManager;
		getStyleClass().add( "task-monitor" );

		label = new Label();
		label.getStyleClass().add( "task-monitor-label" );

		max = new Rectangle();
		max.setManaged( false );
		max.getStyleClass().add( "task-monitor-max" );

		used = new Rectangle();
		used.setManaged( false );
		used.getStyleClass().add( "task-monitor-used" );

		pool = new Rectangle();
		pool.setManaged( false );
		pool.getStyleClass().add( "task-monitor-pool" );

		getChildren().addAll( max, used, pool, label );

		taskWatcher = new TaskWatcher();
		taskManager.addTaskListener( taskWatcher );
	}

	@Override
	protected double computeMinWidth( double height ) {
		return getInsets().getLeft() + MINIMUM_WIDTH + getInsets().getRight();
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
		taskManager.removeTaskListener( taskWatcher );
	}

	@Override
	protected void layoutChildren() {
		super.layoutChildren();

		double width = super.getWidth() - 1;
		double height = super.getHeight() - 1;
		double taskWidth = width / maxThreadCount;

		List<Task> tasks = taskManager.getTasks();
		int taskCount = tasks.size();

		determineBars();
		for( int index = 0; index < maxThreadCount; index++ ) {
			Rectangle bar = bars.get( index );
			bar.setX( taskWidth * index );
			bar.setWidth( taskWidth );

			if( index < taskCount ) {
				Task task = tasks.get( index );
				double percent = task.getPercent();
				double taskHeight = height * percent;
				bar.setY( height - taskHeight );
				bar.setHeight( taskHeight );
			} else {
				bar.setY( height );
				bar.setHeight( 0 );
			}
		}

		double poolX = width * Math.min( taskManager.getCurrentThreadCount() / (double)maxThreadCount, 1.0 );
		pool.setX( poolX );
		pool.setWidth( width - poolX );
		pool.setHeight( height );

		used.setWidth( width * Math.min( taskCount / (double)maxThreadCount, 1.0 ) );
		used.setHeight( height );

		max.setWidth( width );
		max.setHeight( height );
	}

	private void determineBars() {
		if( maxThreadCount == priorThreadCount ) return;

		priorThreadCount = maxThreadCount;
		getChildren().removeAll( bars );
		bars = new ArrayList<>();
		for( int index = 0; index < maxThreadCount; index++ ) {
			Rectangle bar = new Rectangle();
			bar.setManaged( false );
			bar.getStyleClass().add( "task-monitor-progress" );
			bars.add( bar );
		}
		getChildren().addAll( bars );
		getChildren().remove( label );
		getChildren().add( label );
	}

	private void update() {
		maxThreadCount = taskManager.getMaxThreadCount();
		int currentThreadCount = taskManager.getCurrentThreadCount();
		int taskCount = taskManager.getTasks().size();

		String text;
		if( isTextVisible() ) {
			if( isShowPercent() ) {
				double usedPercent = taskCount / (double)maxThreadCount;
				double allocatedPercent = currentThreadCount / (double)maxThreadCount;
				text = percentFormat.format( usedPercent * 100 ) + "% " + DIVIDER + " " + percentFormat.format( allocatedPercent * 100 ) + "% " + DIVIDER + " " + maxThreadCount;
			} else {
				text = taskCount + " " + DIVIDER + " " + currentThreadCount + " " + DIVIDER + " " + maxThreadCount;
			}
		} else {
			// Use a space character so the preferred size is calculated correctly
			text = " ";
		}

		this.label.setText( text );
		requestLayout();
	}

	private void requestUpdate() {
		update();
	}

	private class TaskWatcher implements TaskListener {

		@Override
		public void handleEvent( TaskEvent event ) {
			Platform.runLater( TaskMonitor.this::requestUpdate );
		}

	}

}
