package com.avereon.xenon.index;

import com.avereon.index.*;
import com.avereon.result.Result;
import com.avereon.skill.Controllable;
import com.avereon.xenon.Program;
import lombok.CustomLog;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Future;

@CustomLog
public class IndexService implements Controllable<IndexService> {

	private final Program program;

	private final Indexer indexer;

	public IndexService( Program program ) {
		this.program = program;

		Path indexPath = program.getDataFolder().resolve( "index" );

		this.indexer = new Indexer( indexPath );
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

	public <D extends Document> Result<Future<Result<Set<Hit>>>> submit( D document ) {
		return indexer.submit( document );
	}

	public <D extends Document> Result<Future<Result<Set<Hit>>>> submit( String scope, D document ) {
		return indexer.submit( scope, document );
	}

	public Result<List<Hit>> searchAll( String term ) {
		return Result.of( indexer
			.allIndexes()
			.parallelStream()
			.flatMap( i -> new FuzzySearch( 80 ).search( i, IndexQuery.builder().text( term ).build() ).stream() )
			.reduce( new ArrayList<>(), ( a, b ) -> {
				a.addAll( b );
				return a;
			} ) );
	}

	public Result<List<Hit>> searchAll( String index, String term ) {
		return indexer
			.getIndex( index )
			.map( i -> new FuzzySearch( 80 ).search( i, IndexQuery.builder().text( term ).build() ) )
			.orElseThrow( () -> new IndexNotFoundException( "Default index missing" ) );
	}

	public void removeIndex( String index ) {
		indexer.removeIndex( index );
	}

}
