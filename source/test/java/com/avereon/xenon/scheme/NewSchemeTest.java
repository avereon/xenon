package com.avereon.xenon.scheme;

import com.avereon.xenon.BasePartXenonTestCase;
import com.avereon.xenon.asset.Resource;
import com.avereon.xenon.asset.ResourceType;
import com.avereon.xenon.asset.Codec;
import com.avereon.xenon.asset.MockResourceType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith( MockitoExtension.class )
public class NewSchemeTest extends BasePartXenonTestCase {

	private NewScheme scheme;

	@Mock
	private Codec codec;

	@Mock
	private ResourceType resourceType;

	private Resource resource;

	@BeforeEach
	protected void setup() throws Exception {
		super.setup();
		scheme = new NewScheme( getProgram() );
		resource = new Resource( resourceType, NewScheme.uri() );

		lenient().when( resourceType.getDefaultCodec() ).thenReturn( codec );
	}

	@Test
	void canLoad() throws Exception {
		assertThat( scheme.canLoad( resource ) ).isTrue();
	}

	@Test
	void canSave() throws Exception {
		assertThat( scheme.canSave( resource ) ).isTrue();
	}

	@Test
	void load() throws Exception {
		// given
		Path path = scheme.getTemporaryPath( resource );
		Files.createDirectories( path.getParent() );
		Files.createFile( path );
		path.toFile().deleteOnExit();

		// when
		scheme.load( resource, codec );

		// then
		verify( codec, times( 1 ) ).load( eq( resource ), any( InputStream.class ) );
	}

	@Test
	void loadWithoutContent() throws Exception {
		// when
		scheme.load( resource, codec );

		// then
		verify( codec, times( 0 ) ).load( eq( resource ), any( InputStream.class ) );
	}

	@Test
	void save() throws Exception {
		// when
		scheme.save( resource, codec );

		// then
		verify( codec, times( 1 ) ).save( eq( resource ), any( OutputStream.class ) );
	}

	@Test
	void getTemporaryPath() {
		// given
		Resource resource = new Resource( new MockResourceType( getProgram() ), NewScheme.uri() );
		Path expected = Path.of( NewScheme.NEW_ASSET_TEMP_STORAGE_FOLDER );
		expected = expected.resolve( resource.getUri().getSchemeSpecificPart() );
		expected = expected.resolve( NewScheme.NEW_ASSET_TEMP_STORAGE_CONTENT );

		// when
		Path path = scheme.getTemporaryPath( resource );

		// then
		assertThat( path.toString() ).endsWith( expected.toString() );
	}

}
