package com.xeomar.xenon;

import com.xeomar.util.OperatingSystem;
import com.xeomar.product.ProductCard;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.File;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class ProgramMetadataTest extends ProgramTestCase {

	private ProductCard metadata;

	@Before
	public void setup() throws Exception {
		super.setup();
		metadata = program.getCard();
	}

	@Test
	public void testProgramMetadata() throws Exception {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = dbf.newDocumentBuilder();
		Document pom = builder.parse( new File( "pom.xml" ) );

		XPath xpath = XPathFactory.newInstance().newXPath();
		String groupId = (String)xpath.evaluate( "/project/groupId", pom, XPathConstants.STRING );
		String artifactId = (String)xpath.evaluate( "/project/artifactId", pom, XPathConstants.STRING );
		String version = (String)xpath.evaluate( "/project/version", pom, XPathConstants.STRING );
		String name = (String)xpath.evaluate( "/project/name", pom, XPathConstants.STRING );
		// Provider is specified in the parent pom so the value from the child pom is not valuable
		int inception = Integer.parseInt( (String)xpath.evaluate( "/project/inceptionYear", pom, XPathConstants.STRING ) );
		String description = (String)xpath.evaluate( "/project/description", pom, XPathConstants.STRING );

		String timestampRegex = "[0-9][0-9][0-9][0-9]-[0-9][0-9]-[0-9][0-9] [0-9][0-9]:[0-9][0-9]:[0-9][0-9]";

		assertThat( metadata.getGroup(), is( groupId ) );
		assertThat( metadata.getArtifact(), is( artifactId ) );
		assertThat( metadata.getVersion(), is( version ) );
		assertTrue( "Incorrect timestamp format: " + metadata.getTimestamp(), metadata.getTimestamp().matches( timestampRegex ) );

		assertThat( metadata.getName(), is( name ) );
		assertThat( metadata.getIcon(), is( "http://www.xeomar.com/images/icons/xenon.png" ) );
		assertThat( metadata.getProvider(), is( "Xeomar" ) );
		assertThat( metadata.getInception(), is( inception ) );

		assertThat( metadata.getSummary(), is( "Java application platform" ) );
		assertThat( metadata.getDescription(), is( description ) );
		assertThat( metadata.getCopyrightSummary(), is( "All rights reserved" ) );
		assertThat( metadata.getLicenseSummary(), is( name + " comes with ABSOLUTELY NO WARRANTY. This is open software, and you are welcome to redistribute it under certain conditions." ) );
	}

	@Test
	public void testProgramDataFolder() throws Exception {
		String prefix = ExecMode.TEST.getPrefix();
		File programDataFolder = OperatingSystem.getUserProgramDataFolder( prefix + metadata.getArtifact(), prefix + metadata.getName() );
		assertThat( program.getDataFolder(), is( programDataFolder ) );
	}

}
