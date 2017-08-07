package com.xeomar.xenon.util;

import com.xeomar.xenon.BaseTestCase;
import org.junit.Test;

import java.io.File;
import java.net.URI;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

public class UriUtilTest extends BaseTestCase {

	@Test
	public void testResolveWithString() throws Exception {
		assertThat( UriUtil.resolve( "" ), is( new File( "" ).getCanonicalFile().toURI() ) );
		assertThat( UriUtil.resolve( "." ), is( new File( "." ).getCanonicalFile().toURI() ) );
		assertThat( UriUtil.resolve( "test" ), is( new File( "test" ).getCanonicalFile().toURI() ) );
		assertThat( UriUtil.resolve( "/test" ), is( new File( "/test" ).getCanonicalFile().toURI() ) );
		assertThat( UriUtil.resolve( "ssh://localhost" ), is( URI.create( "ssh://localhost" ) ) );
		assertThat( UriUtil.resolve( "program:about" ), is( URI.create( "program:about" ) ) );
	}

	@Test
	public void testResolveWithRelativeUri() {
		URI base = URI.create( "file:/test/folder/" );
		URI absolute = URI.create( "file:/test/folder/absolute" );
		URI relative = URI.create( "relative" );
		URI jar = URI.create( "jar:file:/test/folder%20with%20spaces/file.jar!/path/to/resource" );

		assertThat( UriUtil.resolve( null, null ), is( nullValue() ) );
		assertThat( UriUtil.resolve( base, null ), is( nullValue() ) );
		assertThat( UriUtil.resolve( null, relative ), is( URI.create( "relative" ) ) );
		assertThat( UriUtil.resolve( null, absolute ), is( URI.create( "file:/test/folder/absolute" ) ) );

		assertThat( UriUtil.resolve( base, absolute ), is( URI.create( "file:/test/folder/absolute" ) ) );
		assertThat( UriUtil.resolve( base, relative ), is( URI.create( "file:/test/folder/relative" ) ) );
		assertThat( UriUtil.resolve( absolute, relative ), is( URI.create( "file:/test/folder/relative" ) ) );

		assertThat( UriUtil.resolve( jar, relative ), is( URI.create( "jar:file:/test/folder%20with%20spaces/file.jar!/path/to/relative" ) ) );

		URI icon = URI.create( "http://www.parallelsymmetry.com/images/icons/escape.png" );
		assertThat( UriUtil.resolve( jar, icon ), is( icon ) );
	}

	@Test
	public void testGetParent() {
		URI absolute = URI.create( "file:/test/folder/absolute" );
		URI opaque = URI.create( "jar:" + absolute.toString() );
		URI doubleOpaque = URI.create( "double:jar:" + absolute.toString() );

		assertThat( UriUtil.getParent( absolute ).toString(), is( "file:/test/folder/" ) );
		assertThat( UriUtil.getParent( opaque ).toString(), is( "jar:file:/test/folder/" ) );
		assertThat( UriUtil.getParent( doubleOpaque ).toString(), is( "double:jar:file:/test/folder/" ) );
	}

	@Test
	public void testParseQueryWithUri() {
		assertThat( UriUtil.parseQuery( (URI)null ), is( nullValue() ) );

		URI uri = URI.create( "test:///path?attr1&attr2" );
		Map<String, String> parameters = UriUtil.parseQuery( uri );
		assertThat( parameters.get( "attr1" ), is( "true" ) );
		assertThat( parameters.get( "attr2" ), is( "true" ) );

		uri = URI.create( "test:///path?attr1=value1&attr2=value2" );
		parameters = UriUtil.parseQuery( uri );
		assertThat( parameters.get( "attr1" ), is( "value1" ) );
		assertThat( parameters.get( "attr2" ), is( "value2" ) );
	}

	@Test
	public void testParseQueryWithString() {
		assertThat( UriUtil.parseQuery( (String)null ), is( nullValue() ) );

		Map<String, String> parameters = UriUtil.parseQuery( "attr1&attr2" );
		assertThat( parameters.get( "attr1" ), is( "true" ) );
		assertThat( parameters.get( "attr2" ), is( "true" ) );

		parameters = UriUtil.parseQuery( "attr1=value1&attr2=value2" );
		assertThat( parameters.get( "attr1" ), is( "value1" ) );
		assertThat( parameters.get( "attr2" ), is( "value2" ) );
	}

}
