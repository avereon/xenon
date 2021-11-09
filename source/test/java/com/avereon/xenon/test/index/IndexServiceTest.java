package com.avereon.xenon.test.index;

import com.avereon.result.Result;
import com.avereon.index.Document;
import com.avereon.xenon.index.IndexService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class IndexServiceTest {

	private IndexService service;

	@BeforeEach
	void setup() {
		service = new IndexService( null );
	}

	@Test
	void testSubmit() {
		Document document = new Document();
		Result<?> result = service.submit( document );

		assertTrue( result.isPresent() );
		assertThat( result.get(), is( document ) );
	}

}
