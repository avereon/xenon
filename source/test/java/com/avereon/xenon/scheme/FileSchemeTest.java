package com.avereon.xenon.scheme;

import com.avereon.xenon.BasePartXenonTestCase;
import com.avereon.xenon.asset.Asset;
import com.avereon.xenon.asset.Codec;
import com.avereon.xenon.asset.MockCodec;
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

		Asset asset = new Asset( path.toUri() );
		Codec codec = new MockCodec();
		scheme.save( asset, codec );

		assertTrue( Files.exists( path ) );
		Files.deleteIfExists( path );
	}

}
