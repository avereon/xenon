package com.avereon.xenon.workspace;

import com.avereon.event.EventHandler;
import com.avereon.xenon.Xenon;
import com.avereon.xenon.asset.type.ProgramTaskType;
import com.avereon.xenon.task.Task;
import com.avereon.xenon.task.TaskManager;
import com.avereon.xenon.task.TaskManagerEvent;
import javafx.scene.control.Label;
import javafx.scene.shape.Rectangle;

import java.util.ArrayList;
import java.util.List;

public class TaskMonitor extends AbstractMonitor {

	private final TaskManager taskManager;

	private final TaskWatcher taskWatcher;

	private final Label label;

	private final Rectangle tasks;

	private final Rectangle threads;

	private final List<Rectangle> bars;

	private boolean textVisible;

	private boolean showPercent;

	private int maxThreadCount;

	private int priorThreadCount;

	public TaskMonitor( Xenon program ) {
		this.taskManager = program.getTaskManager();
		this.taskWatcher = new TaskWatcher();

		getStyleClass().add( "task-monitor" );

		label = new Label();
		label.getStyleClass().add( "task-monitor-label" );

		bars = new ArrayList<>();

		tasks = new Rectangle();
		tasks.setManaged( false );
		tasks.getStyleClass().add( "task-monitor-tasks" );

		threads = new Rectangle();
		threads.setManaged( false );
		threads.getStyleClass().add( "task-monitor-threads" );

		getChildren().addAll( threads, tasks, label );
		update();

		// If the task monitor is clicked then open the task tool
		setOnMouseClicked( ( event ) -> program.getResourceManager().openAsset( ProgramTaskType.URI ) );
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

	@Override
	protected void start() {
		taskManager.getEventBus().register( TaskManagerEvent.ANY, taskWatcher );
	}

	@Override
	public void close() {
		taskManager.getEventBus().unregister( TaskManagerEvent.ANY, taskWatcher );
	}

	@Override
	protected void layoutChildren() {
		super.layoutChildren();

		double width = super.getWidth() - 1;
		double height = super.getHeight() - 1;
		double taskWidth = width / maxThreadCount;

		List<Task<?>> tasks = taskManager.getTasks();
		int taskCount = tasks.size();

		determineBars();
		for( int index = 0; index < maxThreadCount; index++ ) {
			Rectangle bar = bars.get( index );
			bar.setX( taskWidth * index );
			bar.setWidth( taskWidth );

			if( index < taskCount ) {
				Task<?> task = tasks.get( index );
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
	}

	protected void update() {
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

	private void determineBars() {
		if( maxThreadCount == priorThreadCount ) return;

		priorThreadCount = maxThreadCount;
		getChildren().remove( label );
		getChildren().removeAll( bars );
		bars.clear();
		for( int index = 0; index < maxThreadCount; index++ ) {
			Rectangle bar = new Rectangle();
			bar.setManaged( false );
			bar.getStyleClass().add( "task-monitor-progress" );
			bars.add( bar );
		}
		getChildren().addAll( bars );
		getChildren().add( label );
	}

	private class TaskWatcher implements EventHandler<TaskManagerEvent> {

		@Override
		public void handle( TaskManagerEvent event ) {
			requestUpdate();
		}

	}

}
