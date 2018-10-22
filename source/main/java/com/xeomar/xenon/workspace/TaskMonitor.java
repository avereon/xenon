package com.xeomar.xenon.workspace;

import com.xeomar.xenon.task.Task;
import com.xeomar.xenon.task.TaskEvent;
import com.xeomar.xenon.task.TaskListener;
import com.xeomar.xenon.task.TaskManager;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;

import java.util.ArrayList;
import java.util.List;

public class TaskMonitor extends Pane {

	private static final double MINIMUM_WIDTH = 100;

	private TaskManager taskManager;

	private Rectangle max;

	private Rectangle pool;

	private List<Rectangle> bars = new ArrayList<>();

	private Label label;

	private TaskWatcher taskWatcher;

	private int priorThreadCount;

	public TaskMonitor( TaskManager taskManager ) {
		this.taskManager = taskManager;
		getStyleClass().add( "task-monitor" );

		label = new Label();
		label.getStyleClass().add( "task-monitor-label" );

		max = new Rectangle();
		max.getStyleClass().add( "task-monitor-max" );

		pool = new Rectangle();
		pool.getStyleClass().add( "task-monitor-pool" );

		getChildren().addAll( max, pool, label );

		taskWatcher = new TaskWatcher();
		taskManager.addTaskListener( taskWatcher );
	}

	public void close() {
		taskManager.removeTaskListener( taskWatcher );
	}

	@Override
	protected void layoutChildren() {
		super.layoutChildren();
		determineBars();

		double width = super.getWidth() - 1;
		double height = super.getHeight() - 1;

		double threadCount = taskManager.getMaxThreadCount();
		double taskWidth = width / threadCount;

		List<Task> tasks = taskManager.getTasks();
		int taskCount = tasks.size();

		for( int index = 0; index < threadCount; index++ ) {
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

		pool.setWidth( width * Math.min( taskManager.getCurrentThreadCount() / threadCount, 1.0 ) );
		pool.setHeight( height );

		max.setWidth( width );
		max.setHeight( height );
	}

	private void determineBars() {
		int count = taskManager.getMaxThreadCount();
		if( count != priorThreadCount ) {
			priorThreadCount = count;
			getChildren().removeAll( bars );
			bars = new ArrayList<>();
			for( int index = 0; index < count; index++ ) {
				Rectangle bar = new Rectangle();
				bar.getStyleClass().add( "task-monitor-used" );
				bars.add( new Rectangle() );
			}
			getChildren().addAll( bars );
		}
	}

	@Override
	protected double computeMinWidth( double height ) {
		return getInsets().getLeft() + MINIMUM_WIDTH + getInsets().getRight();
	}

	private class TaskWatcher implements TaskListener {

		@Override
		public void handleEvent( TaskEvent event ) {
			Platform.runLater( TaskMonitor.this::requestLayout );
		}

	}

}
