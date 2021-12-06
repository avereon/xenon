package com.avereon.xenon.index;

import com.avereon.index.*;
import com.avereon.result.Result;
import com.avereon.skill.Controllable;
import com.avereon.xenon.Program;
import lombok.CustomLog;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

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

	public <D extends Document> Result<Future<Result<Set<Hit>>>> submit( String index, D document ) {
		return indexer.submit( index, document );
	}

	/**
	 * Search all indexes for the specified term.
	 *
	 * @param term The term to search for
	 * @return The resulting hit list
	 */
	public Result<List<Hit>> searchAll( String term ) {
		return Result.of( indexer
			.allIndexes()
			.parallelStream()
			.flatMap( i -> new FuzzySearch( 80 ).search( i, IndexQuery.builder().text( term ).build() ).get().stream() )
			.sorted( new FuzzySearch.HitSort() )
			.collect( Collectors.toList() ) );
	}

	/**
	 * Search a specific index for the specified term.
	 *
	 * @param term The term to search for
	 * @return The resulting hit list
	 */
	public Result<List<Hit>> search( String index, String term ) {
		return indexer
			.getIndex( index )
			.map( i -> new FuzzySearch( 80 ).search( i, IndexQuery.builder().text( term ).build() ) )
			.orElseThrow( () -> new IndexNotFoundException( "Default index missing" ) );
	}

	/**
	 * Remove a search index.
	 *
	 * @param index The index to remove
	 */
	public void removeIndex( String index ) {
		indexer.removeIndex( index );
	}

}
