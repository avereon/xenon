package com.xeomar.xenon.update;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

public class CatalogCardTest {

	@Test
	public void testLoad() throws Exception {
		String data = "{\"timestamp\":\"1557457963562\",\"products\":[\"mouse\",\"xenon\"]}";
		RepoCard repo = new RepoCard();
		CatalogCard card = CatalogCard.load( repo, new ByteArrayInputStream( data.getBytes( StandardCharsets.UTF_8 ) ) );
	}

}
