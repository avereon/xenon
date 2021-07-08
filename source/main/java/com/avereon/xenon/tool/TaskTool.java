package com.avereon.xenon.tool;

import com.avereon.event.EventHandler;
import com.avereon.product.Rb;
import com.avereon.util.ThreadUtil;
import com.avereon.xenon.Profile;
import com.avereon.xenon.ProgramProduct;
import com.avereon.xenon.ProgramTool;
import com.avereon.xenon.UiFactory;
import com.avereon.xenon.asset.Asset;
import com.avereon.xenon.asset.OpenAssetRequest;
import com.avereon.xenon.task.Task;
import com.avereon.xenon.task.TaskChain;
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
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import lombok.CustomLog;

import java.util.Random;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

@CustomLog
public class TaskTool extends ProgramTool {

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

		ScrollPane scroller = new ScrollPane( taskPanes = new VBox( UiFactory.PAD ) );
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
		setTitle( Rb.text( "tool", "task-name" ) );
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
		getProgram().getTaskManager().submit( new RandomTask( fail ) );
	}

	private void startTaskChain( boolean fail ) {
		int index = 0;
		TaskChain
			.of( "Task " + index++, () -> increment( 0, false ) )
			.link( "Task " + index++, ( i ) -> increment( i, false ) )
			.link( "Task " + index++, ( i ) -> increment( i, fail ) )
			.link( "Task " + index++, ( i ) -> increment( i, false ) )
			.link( "Task " + index, ( i ) -> increment( i, false ) )
			.run( getProgram() );
	}

	private static Integer increment( Integer start, boolean fail ) {
		return new RandomTask( start, fail ).call();
	}

	private class TaskPane extends HBox {

		private final Task<?> task;

		private final ProgressBar progress;

		private TaskPane( Task<?> task ) {
			super( UiFactory.PAD );
			this.task = task;

			progress = new ProgressBar();
			Label name = new Label( task.getName() );
			name.setMaxWidth( Double.MAX_VALUE );
			HBox.setHgrow( name, Priority.ALWAYS );

			Button cancel = new Button();
			cancel.setGraphic( getProgram().getIconLibrary().getIcon( "close" ) );
			cancel.setOnAction( ( e ) -> task.cancel( true ) );

			getChildren().addAll( progress, name, cancel );
		}

		public Task<?> getTask() {
			return task;
		}

		private void setProgress( double progress ) {
			this.progress.setProgress( progress );
		}

	}

	private static class RandomTask extends Task<Integer> {

		private final Integer start;

		private final boolean fail;

		private RandomTask( boolean fail ) {
			this( null, fail );
		}

		private RandomTask( Integer start, boolean fail ) {
			Random random = new Random();
			long duration = 1000 + (long)(4000 * random.nextDouble());

			setName( "Random Task (" + duration + "ms)" );
			setTotal( duration );
			this.start = start;
			this.fail = fail;
		}

		@Override
		public Integer call() {
			long time = 0;
			while( !Thread.currentThread().isInterrupted() && time < getTotal() ) {
				ThreadUtil.pause( DELAY );
				setProgress( time += DELAY );
			}

			if( fail ) throw new RuntimeException( "Random task failure" );

			return start == null ? 1 : start + 1;
		}

	}

}
