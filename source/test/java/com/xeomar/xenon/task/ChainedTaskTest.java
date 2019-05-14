package com.xeomar.xenon.task;

import com.xeomar.xenon.update.DownloadTask;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.function.Supplier;

public class ChainedTaskTest {

	@Test
	public void testChainedTaskOrder() {
		// Some tasks need to be done in order in groups:

		// For example, checking for updates:
		// Start with a request to check for updates
		// - This might create a "check for updates" task
		// - Which in turn creates multiple download tasks for repository catalogs
		// - Then a task collects the downloaded catalogs and determines what product metadata to download
		// - Which in turn creates multiple download tasks for product metadata
		// - Then a task collects the downloaded metadata and and determines what updates are available

		// Another example, downloading and staging updates:
		// Start with a request to download and stage specific updates
		// - This will create a "download updates" task
		// - Which will create multiple download tasks for product artifacts
		// - Then a task collects the downloaded artifacts
		// - Which in turn creates multiple "store update" tasks
		// - A final tasks stores the update metadata in the update settings

		// There are three user experiences available in these scenarios since both
		// are based on user interaction with complex task chains. 1. The user will
		// see a "parent" task at the front of the queue that completes when all the
		// child tasks, which are placed in the queue after the parent, are
		// completed. This give a slight "out of order" feel. 2. The user will see
		// the the child tasks appear in the queue before the parent task, which
		// will be placed "behind" the children. 3. The parent task is placed at
		// the front of the queue but completes as the child tasks are placed in the
		// queue. This is more of a chained task feel but requires state to be
		// passed from one task to another.

		TaskChain chain = new TaskChain();
		chain.add( () -> {
			Set<Future> futures = new HashSet<>();
			futures.add( new DownloadTask( null, null ) );
			futures.add( new DownloadTask( null, null ) );
			futures.add( new DownloadTask( null, null ) );
			futures.add( new DownloadTask( null, null ) );
			futures.add( new DownloadTask( null, null ) );
			futures.add( new DownloadTask( null, null ) );
			return futures;
		} ).add( () -> Set.of( new Task<Double>() {
			public Double call() {
				return 2.2;
			}
		} ) ).add( () -> {
			Set<Future> futures = new HashSet<>();
			futures.add( new DownloadTask( null, null ) );
			futures.add( new DownloadTask( null, null ) );
			futures.add( new DownloadTask( null, null ) );
			futures.add( new DownloadTask( null, null ) );
			futures.add( new DownloadTask( null, null ) );
			futures.add( new DownloadTask( null, null ) );
			return futures;
		} ).add( () -> Set.of( new Task<String>() {
			public String call() {
				return "Hello World!";
			}
		} ) );

	}

	// NOTE Are Tasks their own Futures?
	static class TaskChain<V> extends Task<V> {

		private Supplier<Set<Future>> start;

		private List<Supplier<Set<Future>>> suppliers = new ArrayList<>();

		TaskChain() {}

		TaskChain( Supplier<Set<Future>> s ) {
			add( s );
		}

		TaskChain add( Supplier<Set<Future>> s ) {
			return this;
		}

		@Override
		public V call() throws Exception {
			return null;
		}

	}

}
