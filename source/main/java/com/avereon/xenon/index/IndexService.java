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

@CustomLog
public class IndexService implements Controllable<IndexService> {

	private static final int FUZZY_SEARCH_THRESHOLD = 80;

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
	 * @param terms The terms to search for
	 * @return The resulting hit list
	 */
	public Result<List<Hit>> searchAll( List<String> terms ) {
		Search search = new FuzzySearch( FUZZY_SEARCH_THRESHOLD );
		IndexQuery query = IndexQuery.builder().terms( terms ).build();
		return Indexer.search( search, query, indexer.allIndexes() );
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
			.map( i -> new FuzzySearch( 80 ).search( i, IndexQuery.builder().term( term ).build() ) )
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
