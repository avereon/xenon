package com.avereon.xenon.task;

import com.avereon.skill.Controllable;
import com.avereon.xenon.Xenon;
import com.avereon.zarra.event.FxEventHub;
import lombok.CustomLog;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.*;

@CustomLog
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

	protected static final int DEFAULT_MAX_THREAD_COUNT = Math.min( HIGH_THREAD_COUNT, Math.max( DEFAULT_MIN_THREAD_COUNT, PROCESSOR_COUNT * 2 ) );

	private static final int DEFAULT_THREAD_IDLE_TIMEOUT = 2000;

	int p1ThreadCount;

	int p2ThreadCount;

	int p3ThreadCount;

	private TaskManagerExecutor executorP1;

	private TaskManagerExecutor executorP2;

	private TaskManagerExecutor executorP3;

	private final ThreadGroup group;

	private final Map<Task<?>, Task<?>> taskMap;

	private final Queue<Task<?>> taskQueue;

	@Getter
	private final FxEventHub eventBus;

	public TaskManager() {
		taskMap = new ConcurrentHashMap<>();
		taskQueue = new ConcurrentLinkedQueue<>();
		group = new ThreadGroup( getClass().getName() );
		eventBus = new FxEventHub();
		setMaxThreadCount( DEFAULT_MAX_THREAD_COUNT );
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

		if( executorP1 == null ) return null;
		//if( executorP1 == null ) throw new IllegalStateException( "Task manager not running" );

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

	public ExecutorService getExecutor() {
		return new TaskExecutor( this );
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

	public TaskManager setMaxThreadCount( int count ) {
		redistributeThreadCounts( Math.min( Math.max( LOW_THREAD_COUNT, count ), HIGH_THREAD_COUNT ) );

		if( executorP3 != null ) executorP3.setCorePoolSize( p3ThreadCount );
		if( executorP2 != null ) executorP2.setCorePoolSize( p2ThreadCount );
		if( executorP1 != null ) executorP1.setCorePoolSize( p1ThreadCount );

		return this;
	}

	public int getThreadIdleTimeout() {
		return DEFAULT_THREAD_IDLE_TIMEOUT;
	}

	@Override
	public boolean isRunning() {
		return executorP1 != null && !executorP1.isTerminated() && executorP2 != null && !executorP2.isTerminated() && executorP3 != null && !executorP3.isTerminated();
	}

	@Override
	public TaskManager start() {
		if( isRunning() ) return this;
		LinkedBlockingQueue<Runnable> sharedQueue = new LinkedBlockingQueue<>();
		executorP3 = new TaskManagerExecutor( Task.Priority.LOW, p3ThreadCount, getThreadIdleTimeout(), TimeUnit.MILLISECONDS, sharedQueue, new TaskThreadFactory( this, group, Thread.MIN_PRIORITY ) );
		executorP2 = new TaskManagerExecutor( Task.Priority.MEDIUM,
			p2ThreadCount,
			getThreadIdleTimeout(),
			TimeUnit.MILLISECONDS,
			sharedQueue,
			new TaskThreadFactory( this, group, Thread.MIN_PRIORITY + 1 ),
			executorP3
		);
		executorP1 = new TaskManagerExecutor( Task.Priority.HIGH,
			p1ThreadCount,
			getThreadIdleTimeout(),
			TimeUnit.MILLISECONDS,
			sharedQueue,
			new TaskThreadFactory( this, group, Thread.NORM_PRIORITY ),
			executorP2
		);
		return this;
	}

	@Override
	public TaskManager stop() {
		executorP3 = shutdown( executorP3 );
		executorP2 = shutdown( executorP2 );
		executorP1 = shutdown( executorP1 );
		try {
			if( executorP3 != null ) executorP3.awaitTermination( Xenon.MANAGER_ACTION_SECONDS, TimeUnit.SECONDS );
			if( executorP2 != null ) executorP2.awaitTermination( Xenon.MANAGER_ACTION_SECONDS, TimeUnit.SECONDS );
			if( executorP1 != null ) executorP1.awaitTermination( Xenon.MANAGER_ACTION_SECONDS, TimeUnit.SECONDS );
		} catch( InterruptedException exception ) {
			log.atError( exception ).log( "Error waiting for executor termination" );
		}
		return this;
	}

	protected void taskFailed( Task<?> task, Throwable throwable ) {
		// No need to be noisy here
		//log.atWarn( throwable ).log( "Task failed" );
	}

	public int getP1ThreadCount() {
		return p1ThreadCount;
	}

	public void setP1ThreadCount( int p1ThreadCount ) {
		this.p1ThreadCount = p1ThreadCount;
	}

	public int getP2ThreadCount() {
		return p2ThreadCount;
	}

	public void setP2ThreadCount( int p2ThreadCount ) {
		this.p2ThreadCount = p2ThreadCount;
	}

	public int getP3ThreadCount() {
		return p3ThreadCount;
	}

	public void setP3ThreadCount( int p3ThreadCount ) {
		this.p3ThreadCount = p3ThreadCount;
	}

	private static boolean isTaskThread() {
		return Thread.currentThread() instanceof TaskThread;
	}

	private TaskManagerExecutor shutdown( ThreadPoolExecutor executor ) {
		if( executor == null ) return null;
		executor.shutdown();
		return null;
	}

	private void redistributeThreadCounts( int total ) {
		setP1ThreadCount( Math.max( 1, total / 6 ) );
		setP2ThreadCount( Math.max( 1, total / 3 ) );
		setP3ThreadCount( Math.max( 1, total / 2 ) );
	}

	private class TaskManagerExecutor extends ThreadPoolExecutor {

		private Task.Priority priority;

		private TaskManagerExecutor backup;

		private TaskManagerExecutor(
			Task.Priority priority, int poolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory
		) {
			super( poolSize, poolSize, keepAliveTime, unit, workQueue, threadFactory );
			init( priority );
		}

		private TaskManagerExecutor(
			Task.Priority priority, int poolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory, TaskManagerExecutor backup
		) {
			super( poolSize, poolSize, keepAliveTime, unit, workQueue, threadFactory, new CascadingExecutionExceptionHandler( backup ) );
			this.backup = backup;
			init( priority );
		}

		private void init( Task.Priority priority ) {
			allowCoreThreadTimeOut( true );
			this.priority = priority;
		}

		public <T> Task<T> submit( Task<T> task ) {
			if( backup != null ) {
				if( task.getPriority().ordinal() < priority.ordinal() ) return backup.submit( task );
				if( getCorePoolSize() - getActiveCount() < 1 ) return backup.submit( task );
			}
			return (Task<T>)super.submit( (Callable<T>)task );
		}

		@Override
		protected <T> RunnableFuture<T> newTaskFor( Callable<T> callable ) {
			if( !(callable instanceof Task) ) callable = new TaskWrapper<>( callable );
			Task<T> task = (Task<T>)callable;

			task.getEventBus().parent( TaskManager.this.getEventBus() );
			task.getEventBus().dispatch( new TaskEvent( TaskManager.this, TaskEvent.SUBMITTED, task ) );
			task.setTaskManager( TaskManager.this );
			task.setProcessedPriority( this.priority );

			taskMap.put( task, task );
			taskQueue.add( task );
			return task;
		}

		@Override
		protected void afterExecute( Runnable runnable, Throwable throwable ) {
			if( runnable instanceof Task<?> task ) {
				taskQueue.remove( task );
				taskMap.remove( task );
			}
		}

	}

	private class CascadingExecutionExceptionHandler implements RejectedExecutionHandler {

		private final TaskManagerExecutor backupExecutor;

		public CascadingExecutionExceptionHandler( TaskManagerExecutor backupExecutor ) {
			this.backupExecutor = backupExecutor;
		}

		@Override
		public void rejectedExecution( Runnable runnable, ThreadPoolExecutor executor ) {
			if( runnable instanceof Task ) {
				backupExecutor.submit( (Task<?>)runnable );
			} else {
				backupExecutor.submit( runnable );
			}
			log.atWarn().log( "Task cascaded to lower executor: %s", runnable );
		}

	}

}
