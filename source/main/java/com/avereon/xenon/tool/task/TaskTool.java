package com.avereon.xenon.tool.task;

import com.avereon.util.Log;
import com.avereon.xenon.Profile;
import com.avereon.xenon.Program;
import com.avereon.xenon.ProgramProduct;
import com.avereon.xenon.UiFactory;
import com.avereon.xenon.asset.Asset;
import com.avereon.xenon.task.Task;
import com.avereon.xenon.task.TaskEvent;
import com.avereon.xenon.tool.ProgramTool;
import com.avereon.xenon.workpane.ToolException;
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
import java.util.Random;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class TaskTool extends ProgramTool {

	private static final Logger log = Log.get( MethodHandles.lookup().lookupClass() );

	private final Set<Task<?>> tasks;

	private VBox taskPanes;

	public TaskTool( ProgramProduct product, Asset asset ) {
		super( product, asset );
		tasks = new CopyOnWriteArraySet<>();

		setId( "tool-task" );
		setGraphic( ((Program)product).getIconLibrary().getIcon( "task" ) );
		setTitle( product.rb().text( "tool", "task-name" ) );

		Button startTask = new Button( "Random Test Task" );
		startTask.setOnAction( ( event ) -> startRandomTask() );

		ScrollPane scroller = new ScrollPane( taskPanes = new VBox() );
		scroller.setFitToWidth( true );

		BorderPane layoutPane = new BorderPane();
		layoutPane.setPadding( new Insets( UiFactory.PAD ) );
		if( Profile.DEV.equals( getProgram().getProfile() ) ) layoutPane.setTop( new HBox( startTask ) );
		layoutPane.setCenter( scroller );
		getChildren().add( layoutPane );
	}

	@Override
	protected void allocate() throws ToolException {
		super.allocate();
		getProgram().getTaskManager().getEventBus().register( TaskEvent.SUBMITTED, e -> Platform.runLater( () -> addTaskPane( e.getTask() ) ) );
		getProgram().getTaskManager().getTasks().forEach( this::addTaskPane );
	}

	private void addTaskPane( Task<?> task ) {
		synchronized( tasks ) {
			if( tasks.contains( task ) ) return;
			tasks.add( task );
			TaskPane pane = new TaskPane( task );
			task.getEventBus().register( TaskEvent.FINISH, e -> Platform.runLater( () -> removeTaskPane( pane ) ) );
			task.getEventBus().register( TaskEvent.PROGRESS, e -> Platform.runLater( () -> pane.setProgress( e.getTask().getPercent() ) ) );
			taskPanes.getChildren().add( pane );
			if( task.isDone() ) removeTaskPane( pane );
		}
	}

	private void removeTaskPane( TaskPane pane ) {
		taskPanes.getChildren().remove( pane );
		tasks.remove( pane.getTask() );
	}

	private void startRandomTask() {
		long duration = 1000 + (long)(7000 * new Random().nextDouble());
		getProgram().getTaskManager().submit( new RandomTask( duration ) );
	}

	private class TaskPane extends MigPane {

		private Task<?> task;

		private ProgressBar progress;

		TaskPane( Task<?> task ) {
			this.task = task;
			progress = new ProgressBar();
			Label name = new Label( task.getName() );

			Button cancel = new Button();
			cancel.setGraphic( getProgram().getIconLibrary().getIcon( "close" ) );
			cancel.setOnAction( ( e ) -> task.cancel( true ) );

			add( progress, "w 100!" );
			add( name, "spany, pushx" );
			add( cancel, "pushy" );
		}

		public Task<?> getTask() {
			return task;
		}

		private void setProgress( double progress ) {
			this.progress.setProgress( progress );
		}

	}

	private static class RandomTask extends Task<Void> {

		// The delay between progress checks ~ 1000ms / 120hz;
		private static final long DELAY = 1000 / 120;

		private RandomTask( long duration ) {
			super( "Random Task (" + duration + "ms)" );
			//setMinimum( 0 );
			setTotal( duration );
		}

		@Override
		public Void call() {
			long time = 0;

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

}
