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

	protected static final int LOW_THREAD_COUNT = 4;

	protected static final int HIGH_THREAD_COUNT = 32;

	protected static final int DEFAULT_MIN_THREAD_COUNT = Math.max( 4, PROCESSOR_COUNT / 4 );

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
		return (Task<T>)executorP1.submit( (Callable<T>)task );
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
		if( executorP1 != null ) executorP3.setCorePoolSize( getPriorityThreadCount( 1 ) );
		if( executorP2 != null ) executorP3.setCorePoolSize( getPriorityThreadCount( 2 ) );
		if( executorP3 != null ) executorP3.setCorePoolSize( getPriorityThreadCount( 3 ) );
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
		executorP3 = new TaskManagerExecutor( Task.Priority.LOW,
			getPriorityThreadCount( 3 ),
			THREAD_IDLE_SECONDS,
			TimeUnit.SECONDS,
			new LinkedBlockingQueue<>(),
			new TaskThreadFactory( this, group, Thread.MIN_PRIORITY )
		);
		executorP2 = new TaskManagerExecutor( Task.Priority.MEDIUM,
			getPriorityThreadCount( 2 ),
			THREAD_IDLE_SECONDS,
			TimeUnit.SECONDS,
			new LinkedBlockingQueue<>(),
			new TaskThreadFactory( this, group, Thread.MIN_PRIORITY + 1 ),
			new CascadingExecutionExceptionHandler( executorP3 )
		);
		executorP1 = new TaskManagerExecutor( Task.Priority.HIGH,
			getPriorityThreadCount( 1 ),
			THREAD_IDLE_SECONDS,
			TimeUnit.SECONDS,
			new LinkedBlockingQueue<>(),
			new TaskThreadFactory( this, group, Thread.NORM_PRIORITY ),
			new CascadingExecutionExceptionHandler( executorP2 )
		);
		return this;
	}

	private class CascadingExecutionExceptionHandler implements RejectedExecutionHandler {

		private TaskManagerExecutor backupExecutor;

		public CascadingExecutionExceptionHandler( TaskManagerExecutor backupExecutor ) {
			this.backupExecutor = backupExecutor;
		}

		@Override
		public void rejectedExecution( Runnable runnable, ThreadPoolExecutor executor ) {
			backupExecutor.submit( runnable, executor );
		}

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

	private int getPriorityThreadCount( int priority ) {
		switch( priority ) {
			case 1:
			case 3: {
				return maxThreadCount / 4;
			}
			default:
				return maxThreadCount / 2;
		}
	}

	private class TaskManagerExecutor extends ThreadPoolExecutor {

		private Task.Priority priority;

		private TaskManagerExecutor(
			Task.Priority priority, int poolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory
		) {
			super( poolSize, poolSize, keepAliveTime, unit, workQueue, threadFactory );
			init( priority );
		}

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
			init( priority );
		}

		private void init( Task.Priority priority ) {
			this.priority = priority;
			allowCoreThreadTimeOut( true );
		}

		public <T> Task<T> submit( Task<T> task ) {
			if( task.getPriority().ordinal() < priority.ordinal() ) throw new RejectedExecutionException( "Task priority too low" );
			return (Task<T>)super.submit( (Callable<T>)task );
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

}
