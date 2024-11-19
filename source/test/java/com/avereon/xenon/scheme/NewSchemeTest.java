package com.avereon.xenon.scheme;

import com.avereon.xenon.BasePartXenonTestCase;
import com.avereon.xenon.asset.Asset;
import com.avereon.xenon.asset.MockAssetType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

public class NewSchemeTest extends BasePartXenonTestCase {

	private NewScheme scheme;

	@BeforeEach
	protected void setup() throws Exception {
		super.setup();
		scheme = new NewScheme( getProgram() );
	}

	@Test
	void getTemporaryPath() {
		// given
		Asset asset = new Asset( new MockAssetType( getProgram() ), NewScheme.uri() );
		Path expected = Path.of( NewScheme.NEW_ASSET_TEMP_STORAGE_FOLDER );
		expected = expected.resolve( asset.getUri().getSchemeSpecificPart() );
		expected = expected.resolve( NewScheme.NEW_ASSET_TEMP_STORAGE_CONTENT );

		// when
		Path path = scheme.getTemporaryPath( asset );

		// then
		assertThat( path.toString() ).endsWith( expected.toString() );
	}

}
