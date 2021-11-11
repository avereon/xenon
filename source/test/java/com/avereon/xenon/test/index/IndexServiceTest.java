package com.avereon.xenon.test.index;

import com.avereon.index.Document;
import com.avereon.result.Result;
import com.avereon.xenon.index.IndexService;
import com.avereon.xenon.test.ProgramTestCase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.StringReader;
import java.net.URI;
import java.util.Set;
import java.util.concurrent.Future;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
		Document document = new Document( URI.create( "" ), new StringReader( "" ) );
		var result = service.submit( document );

		assertThat( result.get(), instanceOf( Future.class ) );
		assertTrue( result.isSuccessful() );
		assertTrue( result.isPresent() );
		assertThat( result.get().get(), instanceOf( Result.class ) );
		assertTrue( result.get().get().isSuccessful() );
		assertTrue( result.get().get().isPresent() );
		assertThat( result.get().get().get(), instanceOf( Set.class ) );
	}

}
