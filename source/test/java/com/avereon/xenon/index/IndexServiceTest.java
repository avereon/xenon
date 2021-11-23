package com.avereon.xenon.index;

import com.avereon.index.Document;
import com.avereon.result.Result;
import com.avereon.xenon.ProgramTestCase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.StringReader;
import java.net.URI;
import java.util.Set;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;

public class IndexServiceTest extends ProgramTestCase {

	private IndexService service;

	@Override
	@BeforeEach
	protected void setup() throws Exception {
		super.setup();
		service = new IndexService( getProgram() ).start();
	}

	@AfterEach
	protected void teardown() {
		service.stop();
	}

	@Test
	void testSubmit() throws Exception {
		Document document = new Document( URI.create( "" ), "", new StringReader( "" ) );
		var result = service.submit( document );

		assertThat( result.get() ).isInstanceOf( Future.class );
		assertThat( result.isSuccessful() ).isTrue();
		assertThat( result.isPresent() ).isTrue();
		assertThat( result.get().get() ).isInstanceOf( Result.class );
		assertThat( result.get().get().isSuccessful() ).isTrue();
		assertThat( result.get().get().isPresent() ).isTrue();
		assertThat( result.get().get().get() ).isInstanceOf( Set.class );
	}

}
