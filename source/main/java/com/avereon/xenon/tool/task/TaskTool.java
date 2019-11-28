package com.avereon.xenon.tool.task;

import com.avereon.util.LogUtil;
import com.avereon.xenon.*;
import com.avereon.xenon.resource.Resource;
import com.avereon.xenon.task.Task;
import com.avereon.xenon.task.TaskEvent;
import com.avereon.xenon.task.TaskListener;
import com.avereon.xenon.tool.ProgramTool;
import com.avereon.xenon.workarea.ToolException;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.tbee.javafx.scene.layout.MigPane;

import java.lang.invoke.MethodHandles;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

public class TaskTool extends ProgramTool {

	private static final Logger log = LogUtil.get( MethodHandles.lookup().lookupClass() );

	private TaskWatcher taskWatcher;

	private VBox taskPanes;

	private Map<Task, TaskPane> tasks;

	public TaskTool( ProgramProduct product, Resource resource ) {
		super( product, resource );

		setId( "tool-task" );
		setGraphic( ((Program)product).getIconLibrary().getIcon( "task" ) );
		setTitle( product.rb().text( "tool", "task-name" ) );

		tasks = new ConcurrentHashMap<>();
		taskWatcher = new TaskWatcher();

		Button startTask = new Button( "Random Test Task" );
		startTask.setOnAction( ( event ) -> startRandomTasks() );

		ScrollPane scroller = new ScrollPane( taskPanes = new VBox() );
		scroller.setFitToWidth( true );

		BorderPane layoutPane = new BorderPane();
		layoutPane.setPadding( new Insets( UiFactory.PAD ) );
		if( getProgram().getProfile() == Profile.DEV ) layoutPane.setTop( new HBox( startTask ) );
		layoutPane.setCenter( scroller );
		getChildren().add( layoutPane );
	}

	@Override
	protected void resourceReady( OpenToolRequestParameters parameters ) throws ToolException {
		super.resourceReady( parameters );
		getProgram().getTaskManager().addTaskListener( taskWatcher );
		Platform.runLater( this::init );
	}

	private void init() {
		getProgram().getTaskManager().getTasks().forEach( this::addTaskPane );
	}

	private void addTaskPane( Task task ) {
		if( task.isDone() || tasks.containsKey( task ) ) return;
		TaskPane pane = new TaskPane( task );
		taskPanes.getChildren().add( pane );
		tasks.put( task, pane );
	}

	private void clearTasks() {
		tasks.forEach( TaskTool.this::clearTaskIfDone );
	}

	private void clearTaskIfDone( Task task, TaskPane pane ) {
		if( !task.isDone() ) return;
		if( pane != null ) Platform.runLater( () -> taskPanes.getChildren().remove( pane ) );
		tasks.remove( task );
	}

	private void startRandomTasks() {
		long duration = 1000 + (long)(7000 * new Random().nextDouble());
		getProgram().getTaskManager().submit( new RandomTask( duration ) );
	}

	private static class RandomTask extends Task<Void> {

		// The delay between progress checks ~ 1000ms / 120hz;
		private static final long DELAY = 1000 / 120;

		RandomTask( long duration ) {
			super( "Random Task (" + duration + "ms)" );
			//setMinimum( 0 );
			setTotal( duration );
		}

		@Override
		public Void call() {
			long time = 0;

			//System.out.println( "Running random task ("+ getMaximum() +")");
			while( time < getTotal() ) {
				try {
					Thread.sleep( DELAY );
				} catch( InterruptedException exception ) {
					break;
				}
				time += DELAY;
				setProgress( time );
			}

			return null;
		}

	}

	private class TaskPane extends MigPane {

		private ProgressBar progress;

		TaskPane( Task task ) {
			progress = new ProgressBar();
			Label name = new Label( task.getName() );

			Button cancel = new Button();
			cancel.setGraphic( getProgram().getIconLibrary().getIcon( "close" ) );
			cancel.setOnAction( ( e ) -> task.cancel( true ) );

			add( progress, "w 100!" );
			add( name, "spany, pushx" );
			add( cancel, "pushy" );
		}

		void setProgress( double progress ) {
			this.progress.setProgress( progress );
		}

	}

	private class TaskWatcher implements TaskListener {

		@Override
		public void handleEvent( TaskEvent event ) {
			Task task = event.getTask();

			Platform.runLater( () -> {
				switch( event.getType() ) {
					case TASK_SUBMITTED: {
						addTaskPane( task );
						break;
					}
					case TASK_START:
					case TASK_PROGRESS: {
						TaskPane pane = tasks.get( task );
						if( pane != null ) {
							long total = task.getTotal();
							long progress = task.getProgress();
							pane.setProgress( (double)progress / (double)total );
						}
						break;
					}
				}
			} );

			TaskTool.this.clearTasks();
		}

	}

}
