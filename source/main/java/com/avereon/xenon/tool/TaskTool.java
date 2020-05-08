package com.avereon.xenon.tool;

import com.avereon.event.EventHandler;
import com.avereon.util.Log;
import com.avereon.util.ThreadUtil;
import com.avereon.xenon.*;
import com.avereon.xenon.asset.Asset;
import com.avereon.xenon.asset.OpenAssetRequest;
import com.avereon.xenon.task.Task;
import com.avereon.xenon.task.TaskEvent;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.tbee.javafx.scene.layout.MigPane;

import java.util.Random;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class TaskTool extends ProgramTool {

	private static final System.Logger log = Log.get();

	private final Set<Task<?>> tasks;

	private final VBox taskPanes;

	private EventHandler<TaskEvent> taskManagerWatcher;

	public TaskTool( ProgramProduct product, Asset asset ) {
		super( product, asset );
		tasks = new CopyOnWriteArraySet<>();

		setId( "tool-task" );
		setGraphic( ((Program)product).getIconLibrary().getIcon( "task" ) );
		setTitle( product.rb().text( "tool", "task-name" ) );

		Button testTask = new Button( "Test Task" );
		testTask.setOnAction( ( event ) -> startRandomTask( false ) );

		Button failTask = new Button( "Fail Task" );
		failTask.setOnAction( ( event ) -> startRandomTask( true ) );

		ScrollPane scroller = new ScrollPane( taskPanes = new VBox() );
		scroller.setFitToWidth( true );

		HBox buttonBox = new HBox( UiFactory.PAD, testTask, failTask );
		buttonBox.setAlignment( Pos.CENTER );

		BorderPane layoutPane = new BorderPane();
		layoutPane.setPadding( new Insets( UiFactory.PAD ) );
		if( Profile.DEV.equals( getProgram().getProfile() ) ) layoutPane.setTop( buttonBox );
		layoutPane.setCenter( scroller );
		getChildren().add( layoutPane );
	}

	@Override
	protected void ready( OpenAssetRequest request ) {
		taskManagerWatcher = e -> Platform.runLater( () -> addTaskPane( e.getTask() ) );
		getProgram().getTaskManager().getEventBus().register( TaskEvent.SUBMITTED, taskManagerWatcher );
	}

	@Override
	protected void allocate() {
		getProgram().getTaskManager().getTasks().forEach( this::addTaskPane );
	}

	@Override
	protected void deallocate() {
		getProgram().getTaskManager().getEventBus().unregister( TaskEvent.SUBMITTED, taskManagerWatcher );
	}

	private void addTaskPane( Task<?> task ) {
		synchronized( tasks ) {
			if( tasks.contains( task ) ) return;
			tasks.add( task );
			TaskPane pane = new TaskPane( task );
			task.register( TaskEvent.PROGRESS, e -> Platform.runLater( () -> pane.setProgress( e.getTask().getPercent() ) ) );
			task.register( TaskEvent.FINISH, e -> Platform.runLater( () -> removeTaskPane( pane ) ) );
			if( !task.isDone() ) taskPanes.getChildren().add( pane );
		}
	}

	private void removeTaskPane( TaskPane pane ) {
		taskPanes.getChildren().remove( pane );
		tasks.remove( pane.getTask() );
	}

	private void startRandomTask( boolean fail ) {
		Random random = new Random();
		long duration = 1000 + (long)(7000 * random.nextDouble());
		getProgram().getTaskManager().submit( new RandomTask( duration, fail ) );
	}

	private class TaskPane extends MigPane {

		private final Task<?> task;

		private final ProgressBar progress;

		private TaskPane( Task<?> task ) {
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

		private final boolean fail;

		private RandomTask( long duration, boolean fail ) {
			super( "Random Task (" + duration + "ms)" );
			setTotal( duration );
			this.fail = fail;
		}

		@Override
		public Void call() {
			long time = 0;

			while( time < getTotal() ) {
				ThreadUtil.pause( DELAY );
				time += DELAY;
				setProgress( time );
			}

			if( fail ) throw new RuntimeException( "Random task failure" );

			return null;
		}

	}

}
