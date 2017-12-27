package com.xeomar.xenon.tool.task;

import com.xeomar.xenon.Program;
import com.xeomar.xenon.ProgramProduct;
import com.xeomar.xenon.UiManager;
import com.xeomar.xenon.resource.Resource;
import com.xeomar.xenon.task.Task;
import com.xeomar.xenon.task.TaskEvent;
import com.xeomar.xenon.task.TaskListener;
import com.xeomar.xenon.tool.ProgramTool;
import com.xeomar.xenon.workarea.ToolException;
import com.xeomar.xenon.workarea.ToolParameters;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tbee.javafx.scene.layout.MigPane;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TaskTool extends ProgramTool {

	private static final Logger log = LoggerFactory.getLogger( TaskTool.class );

	private TaskWatcher taskWatcher;

	private VBox taskPanes;

	private Map<Task, TaskPane> tasks;

	public TaskTool( ProgramProduct product, Resource resource ) {
		super( product, resource );

		setId( "tool-task" );
		setGraphic( ((Program)product).getIconLibrary().getIcon( "task" ) );
		setTitle( product.getResourceBundle().getString( "tool", "task-name" ) );

		tasks = new ConcurrentHashMap<>();
		taskWatcher = new TaskWatcher();

		// TODO Should probably wrap this in a scroll pane
		taskPanes = new VBox();

		BorderPane layoutPane = new BorderPane();
		layoutPane.setPadding( new Insets( UiManager.PAD ) );
		//layoutPane.setTop( summaryProgress );
		layoutPane.setCenter( taskPanes );
		getChildren().add( layoutPane );
	}

	@Override
	protected void resourceReady( ToolParameters parameters ) throws ToolException {
		super.resourceReady( parameters );
		getProgram().getTaskManager().addTaskListener( taskWatcher );
	}

	private class TaskPane extends MigPane {

		private Task task;

		private ProgressBar progress;

		private Label name;

		public TaskPane( Task task ) {
			progress = new ProgressBar();
			name = new Label( task.getName() );

			add( progress );
			add( name, "spany, pushy" );
		}

		public Task getTask() {
			return task;
		}

		public void setProgress( double progress ) {
			this.progress.setProgress( progress );
		}

	}

	private class TaskWatcher implements TaskListener {

		@Override
		public void handleEvent( TaskEvent event ) {
			log.debug( "Task event: " + event.getType() );

			Task task = event.getTask();

			Platform.runLater( () -> {
				switch( event.getType() ) {
					case TASK_SUBMITTED: {
						TaskPane pane = new TaskPane( task );
						tasks.put( task, pane );
						taskPanes.getChildren().add( pane );
						break;
					}
					case TASK_START:
					case TASK_PROGRESS: {
						TaskPane pane = tasks.get( task );
						if( pane != null ) {
							long total = task.getMaximum() - task.getMinimum();
							long progress = task.getProgress();
							pane.setProgress( (double)progress / (double)total );
						}
						break;
					}
					case TASK_FINISH: {
						TaskPane pane = tasks.get( task );
						if( pane != null ) taskPanes.getChildren().remove( pane );
						break;
					}
				}
			} );

		}

	}

}
