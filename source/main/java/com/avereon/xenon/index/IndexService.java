package com.avereon.xenon.index;

import com.avereon.skill.Controllable;
import com.avereon.xenon.Program;
import lombok.CustomLog;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;

@CustomLog
public class IndexService implements Controllable<IndexService> {

	private final Program program;

	private IndexWriter writer;

	public IndexService( Program program ) {
		this.program = program;
	}

	@Override
	public boolean isRunning() {
		return writer != null;
	}

	@Override
	public IndexService start() {
		try {
			Directory dir = FSDirectory.open( program.getDataFolder().resolve( "index" ) );
			IndexWriterConfig iwc = new IndexWriterConfig();
			iwc.setOpenMode( IndexWriterConfig.OpenMode.CREATE_OR_APPEND );
			writer = new IndexWriter( dir, iwc );
		} catch( IOException exception ) {
			log.atError( exception ).log( "Unable to start indexing service" );
		}
		return this;
	}

	@Override
	public IndexService stop() {
		try {
			if( writer != null ) writer.close();
		} catch( IOException exception ) {
			log.atError( exception ).log( "Unable to start indexing service" );
		}
		return this;
	}

}
