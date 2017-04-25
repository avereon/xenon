package com.parallelsymmetry.essence;

import com.parallelsymmetry.essence.product.ProductMetadata;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.File;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

@RunWith( JavaFxTestRunner.class )
public class ProgramTest {

	Program program = new Program();

	@Before
	public void setup() throws Exception {
		program.init();
	}

	@Test
	public void testProgramMetadata() throws Exception {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = dbf.newDocumentBuilder();
		Document pom = builder.parse( new File( "pom.xml" ) );

		XPath xpath = XPathFactory.newInstance().newXPath();
		String groupId = (String)xpath.evaluate( "/project/groupId", pom, XPathConstants.STRING );
		String artifactId = (String)xpath.evaluate( "/project/artifactId", pom, XPathConstants.STRING );
		String version = (String)xpath.evaluate( "/project/parent/version", pom, XPathConstants.STRING );

		String name = (String)xpath.evaluate( "/project/name", pom, XPathConstants.STRING );
		String provider = (String)xpath.evaluate( "/project/organization/name", pom, XPathConstants.STRING );
		int inception = Integer.parseInt((String)xpath.evaluate( "/project/inceptionYear", pom, XPathConstants.STRING ) );

		String description = (String)xpath.evaluate( "/project/description", pom, XPathConstants.STRING );

		ProductMetadata metadata = program.getMetadata();
		assertNotNull( metadata );
		assertThat( metadata.getGroup(), is( groupId ) );
		assertThat( metadata.getArtifact(), is( artifactId ) );
		assertThat( metadata.getVersion(), is( version ) );

		assertThat( metadata.getName(), is( name ) );
		assertThat( metadata.getProvider(), is( provider ) );
		assertThat( metadata.getInception(), is( inception ) );

		assertThat( metadata.getDescription(), is( description ) );
	}

}
