package com.avereon.xenon.workspace;

import com.avereon.xenon.task.Task;
import com.avereon.xenon.task.TaskEvent;
import com.avereon.xenon.task.TaskListener;
import com.avereon.xenon.task.TaskManager;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.shape.Rectangle;

import java.util.ArrayList;
import java.util.List;

public class TaskMonitor extends AbstractMonitor {

	private TaskManager taskManager;

	private Rectangle max;

	private Rectangle tasks;

	private Rectangle threads;

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

		tasks = new Rectangle();
		tasks.setManaged( false );
		tasks.getStyleClass().add( "task-monitor-tasks" );

		threads = new Rectangle();
		threads.setManaged( false );
		threads.getStyleClass().add( "task-monitor-threads" );

		getChildren().addAll( max, threads, tasks, label );

		taskWatcher = new TaskWatcher();
		taskManager.addTaskListener( taskWatcher );
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

		threads.setWidth( width * Math.min( taskManager.getCurrentThreadCount() / (double)maxThreadCount, 1.0 ) );
		threads.setHeight( height );

		this.tasks.setWidth( width * Math.min( taskCount / (double)maxThreadCount, 1.0 ) );
		this.tasks.setHeight( height );

		max.setWidth( width );
		max.setHeight( height );
	}

	private void determineBars() {
		if( maxThreadCount == priorThreadCount ) return;

		priorThreadCount = maxThreadCount;
		getChildren().remove( label );
		getChildren().removeAll( bars );
		bars = new ArrayList<>();
		for( int index = 0; index < maxThreadCount; index++ ) {
			Rectangle bar = new Rectangle();
			bar.setManaged( false );
			bar.getStyleClass().add( "task-monitor-progress" );
			bars.add( bar );
		}
		getChildren().addAll( bars );
		getChildren().add( label );
	}

	private void update() {
		maxThreadCount = taskManager.getMaxThreadCount();
		int currentThreadCount = taskManager.getCurrentThreadCount();
		int taskCount = taskManager.getTasks().size();

		// Use a space character so the preferred size is calculated correctly
		StringBuilder text = new StringBuilder( " " );

		if( isTextVisible() ) {
			boolean isPercent = isShowPercent();
			double usedPercent = taskCount / (double)maxThreadCount;
			double allocatedPercent = currentThreadCount / (double)maxThreadCount;

			String percentUsed = percentFormat.format( usedPercent * 100 ) + "%";
			String percentAllocated = percentFormat.format( allocatedPercent * 100 ) + "%";

			text.append( isPercent ? percentUsed : taskCount );
			//			text.append( " " );
			//			text.append( DIVIDER );
			//			text.append( " " );
			//			text.append( isPercent ? percentAllocated : currentThreadCount );
			text.append( " " );
			text.append( DIVIDER );
			text.append( " " );
			text.append( maxThreadCount );
		}

		this.label.setText( text.toString() );
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
