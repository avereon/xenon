package com.avereon.xenon.index;

import com.avereon.index.*;
import com.avereon.result.Result;
import com.avereon.skill.Controllable;
import com.avereon.util.IoUtil;
import com.avereon.xenon.Program;
import lombok.CustomLog;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.Future;

@CustomLog
public class IndexService implements Controllable<IndexService> {

	private static final int FUZZY_SEARCH_THRESHOLD = 80;

	private final Program program;

	private final Indexer indexer;

	private final Path contentPath;

	public IndexService( Program program ) {
		this.program = program;

		Path indexPath = program.getDataFolder().resolve( "index" );
		contentPath = indexPath.resolve( "content" );

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
		return submit( Index.DEFAULT, document );
	}

	public <D extends Document> Result<Future<Result<Set<Hit>>>> submit( String index, D document ) {
		storeContent( document );
		return indexer.submit( index, document );
	}

	/**
	 * Search all indexes for the specified term.
	 *
	 * @param terms The terms to search for
	 * @return The resulting hit list
	 */
	public Result<List<Hit>> searchAll( String text, List<String> terms ) {
		Collection<Index> indexes = indexer.allIndexes();

		Search exactSearch = new FuzzySearch( 100 );
		IndexQuery exactQuery = IndexQuery.builder().terms( Set.of( text ) ).build();
		List<Hit> exactHits = Indexer.search( exactSearch, exactQuery, indexes ).get();

		Search fuzzySearch = new FuzzySearch( FUZZY_SEARCH_THRESHOLD );
		IndexQuery fuzzyQuery = IndexQuery.builder().terms( terms ).build();
		List<Hit> fuzzyHits = Indexer.search( fuzzySearch, fuzzyQuery, indexer.allIndexes() ).get();

		List<Hit> allHits = new ArrayList<>( exactHits.size() + fuzzyHits.size() );
		allHits.addAll( exactHits );
		allHits.addAll( fuzzyHits );

		return Result.of( allHits );
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

	public Document lookup( URI uri ) throws FileNotFoundException {
		return new Document( uri, "", "", new FileReader( getDocumentContentPath( uri ).toFile() ) );
	}

	/**
	 * Remove a search index.
	 *
	 * @param index The index to remove
	 */
	public void removeIndex( String index ) {
		indexer.removeIndex( index );
	}

	private void storeContent( Document document ) {
		if( !document.store() ) return;

		// TODO Do this work on IO threads

		Path path = getDocumentContentPath( document.uri() );
		try {
			Files.createDirectories( contentPath );
			try( FileWriter writer = new FileWriter( path.toFile() ) ) {
				IoUtil.copy( document.content(), writer );
			} catch( Exception exception ) {
				log.atWarn( exception );
			}
		} catch( Exception exception ) {
			log.atWarn( exception );
		}
	}

	private Path getDocumentContentPath( URI uri ) {
		return contentPath.resolve( documentId( uri ) + ".dat" );
	}

	private String documentId( URI uri ) {
		return UUID.nameUUIDFromBytes( uri.toString().getBytes( StandardCharsets.UTF_8 ) ).toString();
	}

}
