package com.avereon.xenon;

import com.avereon.product.ProductCard;
import com.avereon.util.OperatingSystem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

public class ProgramCardTest extends ProgramTestCase {

	private ProductCard metadata;

	@BeforeEach
	public void setup() throws Exception {
		super.setup();
		metadata = getProgram().getCard();
	}

	@Test
	void testProgramMetadata() throws Exception {
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

		assertThat( metadata.getGroup() ).isEqualTo( groupId );
		assertThat( metadata.getArtifact() ).isEqualTo( artifactId );
		assertThat( metadata.getVersion() ).isEqualTo( version );
		assertThat( metadata.getTimestamp().matches( timestampRegex ) ).withFailMessage( "Incorrect timestamp format: " + metadata.getTimestamp() ).isTrue();

		assertThat( metadata.getName() ).isEqualTo( name );
		//assertThat( metadata.getIconUri()).isEqualTo( "program" ) ;
		assertThat( metadata.getProvider() ).isEqualTo( "Avereon" );
		assertThat( metadata.getInception() ).isEqualTo( inception );

		assertThat( metadata.getSummary() ).isEqualTo( "Modular application platform" );
		assertThat( metadata.getDescription() ).isEqualTo( description );
		assertThat( metadata.getCopyrightSummary() ).isEqualTo( "All rights reserved." );
		assertThat( metadata.getLicenseSummary() ).isEqualTo( name + " comes with ABSOLUTELY NO WARRANTY. This is open software, and you are welcome to redistribute it under certain conditions." );
	}

	@Test
	void testProgramDataFolder() {
		String suffix = "-" + XenonMode.TEST;
		Path programDataFolder = OperatingSystem.getUserProgramDataFolder( metadata.getArtifact() + suffix, metadata.getName() + suffix );
		assertThat( getProgram().getDataFolder() ).isEqualTo( programDataFolder );
	}

}
