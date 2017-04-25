package com.parallelsymmetry.essence;

import com.parallelsymmetry.essence.event.ProgramStartedEvent;
import com.parallelsymmetry.essence.util.OperatingSystem;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
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

public class ProgramTest extends ProgramBaseTest {

	@Test
	public void testSomething() throws Exception {
		assertNotNull( program );

		// Wait for the program to start
		waitForEvent( ProgramStartedEvent.class, 10000 );

		String workareaName = program.getWorkspaceManager().getActiveWorkspace().getActiveWorkarea().getName();

		assertThat( program.getWorkspaceManager().getActiveWorkspace().getStage().getTitle(), Matchers.is( workareaName + " - " + metadata.getName() ) );
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
		int inception = Integer.parseInt( (String)xpath.evaluate( "/project/inceptionYear", pom, XPathConstants.STRING ) );
		String description = (String)xpath.evaluate( "/project/description", pom, XPathConstants.STRING );

		String timestampRegex = "[0-9][0-9][0-9][0-9]-[0-9][0-9]-[0-9][0-9] [0-9][0-9]:[0-9][0-9]:[0-9][0-9]";

		assertThat( metadata.getGroup(), is( groupId ) );
		assertThat( metadata.getArtifact(), is( artifactId ) );
		assertThat( metadata.getVersion(), is( version ) );
		Assert.assertTrue( "Incorrect timestamp format: " + metadata.getTimestamp(), metadata.getTimestamp().matches( timestampRegex ) );

		assertThat( metadata.getName(), is( name ) );
		assertThat( metadata.getIcon(), is( "http://www.parallelsymmetry.com/images/icons/essence.png" ) );
		// Provider is specified in parent pom
		//assertThat( metadata.getProvider(), is( provider ) );
		assertThat( metadata.getInception(), is( inception ) );

		assertThat( metadata.getSummary(), is( "Java application platform" ) );
		assertThat( metadata.getDescription(), is( description ) );
		assertThat( metadata.getCopyrightSummary(), is( "All rights reserved." ) );
		assertThat( metadata.getLicenseSummary(), is( name + " comes with ABSOLUTELY NO WARRANTY. This is open software, and you are welcome to redistribute it under certain conditions." ) );
	}

	@Test
	public void testProgramDataFolder() {
		assertThat( program.getProgramDataFolder(), is( OperatingSystem.getUserProgramDataFolder( metadata.getArtifact(), metadata.getName() ) ) );
	}

}
