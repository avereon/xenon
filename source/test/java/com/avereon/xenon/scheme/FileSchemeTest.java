package com.avereon.xenon.scheme;

import com.avereon.xenon.BasePartXenonTestCase;
import com.avereon.xenon.resource.Resource;
import com.avereon.xenon.resource.Codec;
import com.avereon.xenon.resource.MockCodec;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class FileSchemeTest extends BasePartXenonTestCase {

	@Test
	void testSave() throws Exception {
		FileScheme scheme = new FileScheme( getProgram() );

		Path path = Paths.get( System.getProperty( "user.dir" ) ).resolve( "target" ).resolve( "xenon-save-test.txt" );
		Files.createDirectories( path.getParent() );

		Resource resource = new Resource( path.toUri() );
		Codec codec = new MockCodec();
		scheme.save( resource, codec );

		assertTrue( Files.exists( path ) );
		Files.deleteIfExists( path );
	}

}
