package com.avereon.xenon.index;

import com.avereon.index.Document;
import com.avereon.index.Indexer;
import com.avereon.result.Result;
import com.avereon.skill.Controllable;
import com.avereon.xenon.Program;
import lombok.CustomLog;

import java.nio.file.Path;
import java.util.concurrent.Future;

@CustomLog
public class IndexService implements Controllable<IndexService> {

	private final Program program;

	private final Indexer indexer;

	public IndexService( Program program ) {
		this.program = program;

		Path indexPath = program.getDataFolder().resolve("index");

		this.indexer = new Indexer(indexPath);
	}

	@Override
	public boolean isRunning() {
		return indexer.isRunning();
	}

	@Override
	public IndexService start() {
		indexer.start();
		return this;
	}

	@Override
	public IndexService stop() {
		indexer.stop();
		return this;
	}

	public <D extends Document> Result<Future<?>> submit( D document ) {
		Result<Future<?>> result = indexer.submit( document );

		return result;
	}

}
