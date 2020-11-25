package com.avereon.xenon.tool;

import com.avereon.event.EventHandler;
import com.avereon.util.Log;
import com.avereon.util.ThreadUtil;
import com.avereon.xenon.Profile;
import com.avereon.xenon.ProgramProduct;
import com.avereon.xenon.ProgramTool;
import com.avereon.xenon.UiFactory;
import com.avereon.xenon.asset.Asset;
import com.avereon.xenon.asset.OpenAssetRequest;
import com.avereon.xenon.task.NewTaskChain;
import com.avereon.xenon.task.Task;
import com.avereon.xenon.task.TaskEvent;
import com.avereon.zerra.javafx.Fx;
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
import java.util.concurrent.ExecutionException;

public class TaskTool extends ProgramTool {

	private static final System.Logger log = Log.get();

	// The delay between progress checks ~ 1000ms / 120hz;
	private static final long DELAY = 1000 / 120;

	private final Set<Task<?>> tasks;

	private final VBox taskPanes;

	private EventHandler<TaskEvent> taskManagerWatcher;

	public TaskTool( ProgramProduct product, Asset asset ) {
		super( product, asset );
		tasks = new CopyOnWriteArraySet<>();

		setId( "tool-task" );

		Button testTask = new Button( "Test Task" );
		testTask.setOnAction( ( event ) -> startRandomTask( false ) );

		Button failTask = new Button( "Fail Task" );
		failTask.setOnAction( ( event ) -> startRandomTask( true ) );

		Button testChain = new Button( "Test Chain" );
		testChain.setOnAction( ( event ) -> startTaskChain( false ) );

		Button failChain = new Button( "Fail Chain" );
		failChain.setOnAction( ( event ) -> startTaskChain( true ) );

		ScrollPane scroller = new ScrollPane( taskPanes = new VBox() );
		scroller.setFitToWidth( true );

		HBox buttonBox = new HBox( UiFactory.PAD, testTask, failTask, testChain, failChain );
		buttonBox.setAlignment( Pos.CENTER );

		BorderPane layoutPane = new BorderPane();
		layoutPane.setPadding( new Insets( UiFactory.PAD ) );
		if( Profile.DEV.equals( getProgram().getProfile() ) ) layoutPane.setTop( buttonBox );
		layoutPane.setCenter( scroller );
		getChildren().add( layoutPane );
	}

	@Override
	protected void ready( OpenAssetRequest request ) {
		setTitle( getProduct().rb().text( "tool", "task-name" ) );
		setGraphic( getProgram().getIconLibrary().getIcon( "task" ) );

		taskManagerWatcher = e -> Fx.run( () -> addTaskPane( e.getTask() ) );
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
			task.register( TaskEvent.PROGRESS, e -> Fx.run( () -> pane.setProgress( e.getTask().getPercent() ) ) );
			task.register( TaskEvent.FINISH, e -> Fx.run( () -> removeTaskPane( pane ) ) );
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

	private void startTaskChain( boolean fail ) {
		int index = 0;
		Task<Integer> result = new NewTaskChain<Integer>()
				.link("Task " + index++, (i) -> increment( i, false ) )
				.link("Task " + index++, (i) -> increment( i, false ) )
				.link("Task " + index++, (i) -> increment( i, false ) )
				.link("Task " + index++, (i) -> increment( i, false ) )
				.link("Task " + index++, (i) -> increment( i, false ) )
				.run( getProgram() );

		getProgram().getTaskManager().submit( Task.of( "Task Chain Result", () -> {
			try {
				System.out.println( "task result=" + result.get() );
			} catch( InterruptedException e ) {
				e.printStackTrace();
			} catch( ExecutionException e ) {
				e.printStackTrace();
			}
		} )  );
	}

	private int increment( Integer start, boolean fail ) {
		if( start == null ) start = 0;

		log.log( Log.WARN, "Task start value=" + start );

		Random random = new Random();
		long duration = 1000 + (long)(7000 * random.nextDouble());

		long time = 0;
		while( !Thread.currentThread().isInterrupted() && time < duration ) {
			ThreadUtil.pause( DELAY );
			time += DELAY;
		}

		log.log( Log.WARN, "Task complete " + duration );

		if( fail ) throw new RuntimeException( "Random task failure" );

		return ++start;
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

		private final boolean fail;

		private RandomTask( long duration, boolean fail ) {
			super( "Random Task (" + duration + "ms)" );
			setTotal( duration );
			this.fail = fail;
		}

		@Override
		public Void call() {
			long time = 0;

			while( !Thread.currentThread().isInterrupted() && time < getTotal() ) {
				ThreadUtil.pause( DELAY );
				time += DELAY;
				setProgress( time );
			}

			if( fail ) throw new RuntimeException( "Random task failure" );

			return null;
		}

	}

}
