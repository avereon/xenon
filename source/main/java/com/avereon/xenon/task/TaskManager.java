package com.avereon.xenon.task;

import com.avereon.util.Controllable;
import com.avereon.util.LogUtil;
import com.avereon.xenon.Program;
import com.avereon.xenon.util.ProgramEventBus;
import org.slf4j.Logger;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.*;

public class TaskManager implements Controllable<TaskManager> {

	private static final int PROCESSOR_COUNT = Runtime.getRuntime().availableProcessors();

	/**
	 * The lowest possible number of threads in the task manager
	 */
	protected static final int LOW_THREAD_COUNT = 6;

	/**
	 * The highest possible number of threads in the task manager
	 */
	protected static final int HIGH_THREAD_COUNT = PROCESSOR_COUNT * LOW_THREAD_COUNT;

	protected static final int DEFAULT_MIN_THREAD_COUNT = Math.max( LOW_THREAD_COUNT, PROCESSOR_COUNT / 4 );

	protected static final int DEFAULT_MAX_THREAD_COUNT = Math.max( DEFAULT_MIN_THREAD_COUNT, PROCESSOR_COUNT * 2 );

	static final int THREAD_IDLE_SECONDS = 2;

	private static final Logger log = LogUtil.get( MethodHandles.lookup().lookupClass() );

	private TaskManagerExecutor executorP1;

	private TaskManagerExecutor executorP2;

	private TaskManagerExecutor executorP3;

	private ThreadGroup group;

	private int maxThreadCount;

	private Map<Task<?>, Task<?>> taskMap;

	private Queue<Task<?>> taskQueue;

	private ProgramEventBus eventBus;

	public TaskManager() {
		taskMap = new ConcurrentHashMap<>();
		taskQueue = new ConcurrentLinkedQueue<>();
		group = new ThreadGroup( getClass().getName() );
		eventBus = new ProgramEventBus();
		maxThreadCount = DEFAULT_MAX_THREAD_COUNT;
	}

	public static void taskThreadCheck() {
		if( !TaskManager.isTaskThread() ) {
			StackTraceElement[] stack = Thread.currentThread().getStackTrace();

			String callingClass = stack[ 2 ].getClassName();
			String callingMethod = stack[ 2 ].getMethodName();
			String callingFile = stack[ 2 ].getFileName();
			int callingLine = stack[ 2 ].getLineNumber();

			String caller = callingClass + "." + callingMethod + "(" + callingFile + ":" + callingLine + ")";

			throw new RuntimeException( caller + " not called on task thread" );
		}
	}

	public <T> Task<T> submit( Task<T> task ) {
		Task<T> existing = getExisting( task );
		if( existing != null ) return existing;

		task.setState( Task.State.SCHEDULED );
		return executorP1.submit( task );
	}

	@SuppressWarnings( "unchecked" )
	private <T> Task<T> getExisting( Task<T> task ) {
		return (Task<T>)taskMap.get( task );
	}

	public long getTaskCount() {
		return getTasks().size();
	}

	public List<Task<?>> getTasks() {
		return new ArrayList<>( taskQueue );
	}

	public int getCurrentThreadCount() {
		int count = 0;
		count += executorP1 == null ? 0 : executorP1.getPoolSize();
		count += executorP2 == null ? 0 : executorP2.getPoolSize();
		count += executorP3 == null ? 0 : executorP3.getPoolSize();
		return count;
	}

	public int getMaxThreadCount() {
		int count = 0;
		count += executorP1 == null ? 0 : executorP1.getCorePoolSize();
		count += executorP2 == null ? 0 : executorP2.getCorePoolSize();
		count += executorP3 == null ? 0 : executorP3.getCorePoolSize();
		return count;
	}

	public void setMaxThreadCount( int count ) {
		maxThreadCount = Math.min( Math.max( LOW_THREAD_COUNT, count ), HIGH_THREAD_COUNT );
		if( executorP1 != null ) executorP1.setCorePoolSize( getPriorityThreadCount( Task.Priority.HIGH ) );
		if( executorP2 != null ) executorP2.setCorePoolSize( getPriorityThreadCount( Task.Priority.MEDIUM ) );
		if( executorP3 != null ) executorP3.setCorePoolSize( getPriorityThreadCount( Task.Priority.LOW ) );
	}

	public ProgramEventBus getEventBus() {
		return eventBus;
	}

	@Override
	public boolean isRunning() {
		return executorP1 != null && !executorP1.isTerminated() && executorP2 != null && !executorP2.isTerminated() && executorP3 != null && !executorP3.isTerminated();
	}

	@Override
	public TaskManager start() {
		if( isRunning() ) return this;
		executorP3 = new TaskManagerExecutor(
			Task.Priority.LOW,
			getPriorityThreadCount( Task.Priority.LOW ),
			THREAD_IDLE_SECONDS,
			TimeUnit.SECONDS,
			new LinkedBlockingQueue<>(),
			new TaskThreadFactory( this, group, Thread.MIN_PRIORITY ),
			new ThreadPoolExecutor.AbortPolicy()
		);
		executorP2 = new TaskManagerExecutor(
			Task.Priority.MEDIUM,
			getPriorityThreadCount( Task.Priority.MEDIUM ),
			THREAD_IDLE_SECONDS,
			TimeUnit.SECONDS,
			new LinkedBlockingQueue<>(),
			new TaskThreadFactory( this, group, Thread.MIN_PRIORITY + 1 ),
			new CascadingExecutionExceptionHandler( executorP3 )
		);
		executorP1 = new TaskManagerExecutor(
			Task.Priority.HIGH,
			getPriorityThreadCount( Task.Priority.HIGH ),
			THREAD_IDLE_SECONDS,
			TimeUnit.SECONDS,
			new LinkedBlockingQueue<>(),
			new TaskThreadFactory( this, group, Thread.NORM_PRIORITY ),
			new CascadingExecutionExceptionHandler( executorP2 )
		);
		return this;
	}

	@Override
	public TaskManager stop() {
		executorP3 = shutdown( executorP3 );
		executorP2 = shutdown( executorP2 );
		executorP1 = shutdown( executorP1 );
		try {
			if( executorP3 != null ) executorP3.awaitTermination( Program.MANAGER_ACTION_SECONDS, TimeUnit.SECONDS );
			if( executorP2 != null ) executorP2.awaitTermination( Program.MANAGER_ACTION_SECONDS, TimeUnit.SECONDS );
			if( executorP1 != null ) executorP1.awaitTermination( Program.MANAGER_ACTION_SECONDS, TimeUnit.SECONDS );
		} catch( InterruptedException exception ) {
			// Intentionally ignore exception
		}
		return this;
	}

	private TaskManagerExecutor shutdown( ThreadPoolExecutor executor ) {
		if( executor == null ) return null;
		executor.shutdown();
		return null;
	}

	private static boolean isTaskThread() {
		return Thread.currentThread() instanceof TaskThread;
	}

	private int getPriorityThreadCount( Task.Priority priority ) {
		int h = Math.max( 1, maxThreadCount / 6 );
		int m = Math.max( 1, maxThreadCount / 3 );
		int l = Math.max( 1, maxThreadCount / 2 );
		switch( priority ) {
			case HIGH:
				return h;
			case MEDIUM:
				return m;
			default: {
				return l;
			}
		}
	}

	private class TaskManagerExecutor extends ThreadPoolExecutor {

		private Task.Priority priority;

		private TaskManagerExecutor(
			Task.Priority priority,
			int poolSize,
			long keepAliveTime,
			TimeUnit unit,
			BlockingQueue<Runnable> workQueue,
			ThreadFactory threadFactory,
			RejectedExecutionHandler rejectedExecutionHandler
		) {
			super( poolSize, poolSize, keepAliveTime, unit, workQueue, threadFactory, rejectedExecutionHandler );
			allowCoreThreadTimeOut( true );
			this.priority = priority;
		}

		public <T> Task<T> submit( Task<T> task ) {
			return (Task<T>)super.submit( (Callable<T>)task );
		}

		@Override
		public void execute( Runnable command ) {
			if( command instanceof Task && ((Task<?>)command).getPriority().ordinal() < priority.ordinal() ) {
				getRejectedExecutionHandler().rejectedExecution( command, this );
				return;
			} else if( priority != Task.Priority.MEDIUM ) {
				getRejectedExecutionHandler().rejectedExecution( command, this );
				return;
			}

			super.execute( command );
		}

		@Override
		protected <T> RunnableFuture<T> newTaskFor( Callable<T> callable ) {
			if( !(callable instanceof Task) ) callable = new TaskWrapper<>( callable );
			Task<T> task = (Task<T>)callable;

			task.getEventBus().parent( TaskManager.this.getEventBus() );
			task.getEventBus().dispatch( new TaskEvent( TaskManager.this, TaskEvent.SUBMITTED, task ) );
			task.setTaskManager( TaskManager.this );

			taskMap.put( task, task );
			taskQueue.add( task );
			return task;
		}

		@Override
		protected void afterExecute( Runnable runnable, Throwable throwable ) {
			if( runnable instanceof Task ) {
				Task<?> task = (Task<?>)runnable;
				taskQueue.remove( task );
				taskMap.remove( task );
			}
		}

	}

	private class CascadingExecutionExceptionHandler implements RejectedExecutionHandler {

		private TaskManagerExecutor backupExecutor;

		public CascadingExecutionExceptionHandler( TaskManagerExecutor backupExecutor ) {
			this.backupExecutor = backupExecutor;
		}

		@Override
		public void rejectedExecution( Runnable runnable, ThreadPoolExecutor executor ) {
			backupExecutor.submit( runnable, executor );
			log.trace( "Task cascaded to lower executor: " + runnable );
		}

	}

}
